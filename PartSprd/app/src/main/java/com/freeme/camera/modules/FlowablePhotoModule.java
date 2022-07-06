package com.freeme.camera.modules;

import com.android.camera.CameraModule;
import com.android.camera.app.AppController;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class FlowablePhotoModule extends CameraModule {

    protected Subscription mSubscription;
    protected FlowableEmitter<FlowablePhotoBean> mFlowableEmitter;
    protected Flowable<FlowablePhotoBean> mFlowable;
    protected Subscriber<FlowablePhotoBean> mSubscriber;

    public FlowablePhotoModule(AppController app) {
        super(app);
        setupFlowable();
    }

    protected void setupFlowable() {
        mFlowable = Flowable.create(emitter -> mFlowableEmitter = emitter, BackpressureStrategy.BUFFER);
        mSubscriber = new Subscriber<FlowablePhotoBean>() {
            @Override
            public void onSubscribe(Subscription s) {
                mSubscription = s;
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(FlowablePhotoBean bytes) {
                onPictureTakenStart();
                onPictureTaken(bytes);
                onPictureTakenComplete();
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

    protected void doPictureTaken(FlowablePhotoBean info) {
        mFlowableEmitter.onNext(info);
    }

    protected abstract void onPictureTaken(FlowablePhotoBean info);

    @Override
    public void resume() {
        if (mFlowable != null && mSubscriber != null) {
            mFlowable.subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.io()).subscribe(mSubscriber);
        }
    }

    @Override
    public void pause() {
        if (mSubscription != null) {
            mSubscription.cancel();
        }
    }

    protected void onPictureTakenStart() {

    }

    protected void onPictureTakenComplete() {

    }
}
