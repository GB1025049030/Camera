package com.android.ex.camera2.portability;

import android.os.Handler;
import android.os.HandlerThread;

import com.android.ex.camera2.portability.debug.Log;

import java.util.LinkedList;
import java.util.Queue;

public class SprdDispatchThread  extends DispatchThread{
    private static final Log.Tag TAG = new Log.Tag("SprdDispThread");

    public SprdDispatchThread(Handler cameraHandler, HandlerThread cameraHandlerThread) {
        super(cameraHandler, cameraHandlerThread);
    }

    public boolean removeJob(Runnable job){
        Log.i(TAG,"removeJob");
        synchronized (mJobQueue) {
            if (mJobQueue.contains(job)) {
                return mJobQueue.remove(job);
            }
        }
        return false;
    }

    public boolean hasRunnable(Runnable job) {
        if (job == null)
            return false;
        synchronized (mJobQueue) {
            return (mJobQueue.contains(job));
        }
    }
}
