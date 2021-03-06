package com.sd.lib.looper.impl;

import android.os.Handler;
import android.os.Looper;

import com.sd.lib.looper.FLooper;

public class FSimpleLooper implements FLooper
{
    private final Handler mHandler;
    private Runnable mLoopRunnable;
    /**
     * 循环触发间隔，默认300毫秒
     */
    private long mInterval = 300;
    private boolean mIsStarted = false;

    private long mNextLoopTime;

    private OnStateChangeCallback mOnStateChangeCallback;

    public FSimpleLooper()
    {
        this(null);
    }

    public FSimpleLooper(Looper looper)
    {
        if (looper == null)
            looper = Looper.getMainLooper();

        mHandler = new Handler(looper);
    }

    private final Runnable mHandlerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            loopIfNeed();
        }
    };

    private synchronized void loopIfNeed()
    {
        if (mIsStarted)
        {
            if (onLoop())
            {
                sendMessageDelayed(getInterval());
            } else
            {
                stop();
            }
        }
    }

    /**
     * 循环回调
     *
     * @return true-继续循环，false-停止循环
     */
    protected boolean onLoop()
    {
        mLoopRunnable.run();
        return true;
    }

    private void sendMessageDelayed(long delay)
    {
        if (delay < 0)
            delay = 0;

        mHandler.postDelayed(mHandlerRunnable, delay);
        mNextLoopTime = System.currentTimeMillis() + delay;
    }

    @Override
    public void setOnStateChangeCallback(OnStateChangeCallback callback)
    {
        mOnStateChangeCallback = callback;
    }

    @Override
    public final boolean isStarted()
    {
        return mIsStarted;
    }

    @Override
    public long getInterval()
    {
        return mInterval;
    }

    @Override
    public synchronized void setInterval(long interval)
    {
        if (interval <= 0)
            throw new IllegalArgumentException("interval is out of range (interval > 0).");

        mInterval = interval;
    }

    @Override
    public long getNextLoopTime()
    {
        return mNextLoopTime;
    }

    @Override
    public final void start(Runnable runnable)
    {
        startDelayed(runnable, 0);
    }

    @Override
    public synchronized final void startDelayed(Runnable runnable, long delayMillis)
    {
        if (runnable == null)
            throw new NullPointerException("runnable is null");

        stop();

        mLoopRunnable = runnable;
        sendMessageDelayed(delayMillis);
        setStarted(true);
    }

    @Override
    public synchronized final void stop()
    {
        if (mIsStarted)
        {
            mHandler.removeCallbacks(mHandlerRunnable);
            mNextLoopTime = 0;
            setStarted(false);
        }
    }

    private void setStarted(boolean started)
    {
        if (mIsStarted != started)
        {
            mIsStarted = started;
            onStateChanged(started);
            if (mOnStateChangeCallback != null)
                mOnStateChangeCallback.onStateChanged(started);
        }
    }

    /**
     * 循环是否开始状态变化回调
     *
     * @param started
     */
    protected void onStateChanged(boolean started)
    {
    }
}
