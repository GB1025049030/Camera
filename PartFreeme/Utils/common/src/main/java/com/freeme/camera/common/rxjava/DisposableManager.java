package com.freeme.camera.common.rxjava;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public enum DisposableManager {
    I;

    private CompositeDisposable mCompositeDisposable;

    public void add(Disposable disposable) {
        ifPresent(disposable, d -> {
            if (!d.isDisposed()) {
                getCompositeDisposable().add(d);
            }
        });
    }

    public void clear() {
        ifPresent(mCompositeDisposable, compositeDisposable -> {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        });
    }

    private CompositeDisposable getCompositeDisposable() {
        mCompositeDisposable = get(mCompositeDisposable, CompositeDisposable::new);
        return mCompositeDisposable;
    }

    private <T> T get(T t, Supplier<T> supplier) {
        return Optional.ofNullable(t).orElseGet(supplier);
    }

    private <T> void ifPresent(T t, Consumer<T> consumer) {
        Optional.ofNullable(t).ifPresent(consumer);
    }
}
