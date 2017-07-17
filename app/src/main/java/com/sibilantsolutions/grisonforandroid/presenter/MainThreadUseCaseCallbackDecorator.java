package com.sibilantsolutions.grisonforandroid.presenter;

import android.os.Handler;
import android.os.Looper;

import com.sibilantsolutions.grisonforandroid.domain.usecase.UseCase;

/**
 * Decorates UseCase.Callback instances to ensure that the callback methods are invoked on the
 * main thread (UI thread).
 * <p>
 * Created by jt on 7/16/17.
 */
class MainThreadUseCaseCallbackDecorator<T> implements UseCase.Callback<T> {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final UseCase.Callback<T> delegate;

    public MainThreadUseCaseCallbackDecorator(UseCase.Callback<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onSuccess(final T result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                delegate.onSuccess(result);
            }
        });
    }

    @Override
    public void onError(final Exception e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                delegate.onError(e);
            }
        });
    }
}
