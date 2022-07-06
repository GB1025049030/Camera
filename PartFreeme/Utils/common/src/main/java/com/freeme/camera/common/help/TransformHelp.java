package com.freeme.camera.common.help;

import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TransformHelp<T, V> {
    private static final String TAG = "TransformHelp";
    private static final int BUFFER_SIZE = 2;

    private final ConcurrentLinkedQueue<V> mTargetQueue;
    private final Flowable<T> mFlowable;
    private final Subscriber<T> mSubscriber;
    private final Callback<T, V> mCallback;

    private Subscription mSubscription;
    private FlowableEmitter<T> mFlowableEmitter;

    public TransformHelp(Callback<T, V> callback) {
        this(callback, BUFFER_SIZE);
    }

    public TransformHelp(Callback<T, V> callback, int bufferSize) {
        this.mCallback = callback;
        this.mTargetQueue = new ConcurrentLinkedQueue<>();
        this.mFlowable = Flowable.create(emitter -> mFlowableEmitter = emitter, BackpressureStrategy.LATEST);
        this.mSubscriber = new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                mSubscription = s;
                Log.d(TAG, "onSubscribe: ");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T t) {
                if (t == null) return;
                doData(t);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        };
    }

    private void doData(T t) {
        if (mCallback == null || t == null) return;
        V v = mCallback.getResult(t);
        if (v != null) {
            offer(v);
        }
    }

    private void offer(V v) {
        if (mTargetQueue == null || v == null) return;
        mCallback.onResultChange(mTargetQueue.offer(v));
    }

    public void start() {
        if (mFlowable != null && mSubscriber != null) {
            mFlowable.subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.io()).subscribe(mSubscriber);
        }
    }

    public void stop() {
        if (mSubscription != null) {
            mSubscription.cancel();
        }
        if (mTargetQueue != null) {
            mTargetQueue.clear();
        }
    }

    public void setData(T data) {
        if (mFlowableEmitter == null || data == null) return;
        mFlowableEmitter.onNext(data);
    }

    public boolean isEmpty() {
        Log.d(TAG, "isEmpty: isEmpty() = " + (mTargetQueue == null || mTargetQueue.peek() == null));
        return mTargetQueue == null || mTargetQueue.peek() == null;
    }

    public V getData() {
        if (isEmpty()) return null;
        return mTargetQueue.poll();
    }

    public int size() {
        if (isEmpty()) return 0;
        return mTargetQueue.size();
    }

    public interface Callback<T, V> {
        V getResult(T t);
        void onResultChange(boolean state);
    }
}
