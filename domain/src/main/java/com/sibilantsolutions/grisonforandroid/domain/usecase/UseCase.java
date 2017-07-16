package com.sibilantsolutions.grisonforandroid.domain.usecase;

/**
 * Parent interface for use cases.
 * <p>
 * Created by jt on 7/16/17.
 */

public interface UseCase<P, R> {

    interface Callback<R> {
        void onSuccess(R result);

        void onError(Exception e);
    }

    void execute(P parameter, Callback<R> callback);

}
