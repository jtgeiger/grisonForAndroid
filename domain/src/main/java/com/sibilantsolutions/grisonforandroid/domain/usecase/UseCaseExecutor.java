package com.sibilantsolutions.grisonforandroid.domain.usecase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Wrapper for ExecutorService to be used by use cases to perform work in a background thread.
 * <p>
 * Created by jt on 7/16/17.
 */

public class UseCaseExecutor {

    //Let use cases run as many concurrent threads as desired.  It's up to somebody else to enforce
    //proper concurrent access to underlying resources.
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private UseCaseExecutor() {
        //Prevent outside instantiation.
    }

    private static class Holder {
        static final UseCaseExecutor INSTANCE = new UseCaseExecutor();
    }

    public static UseCaseExecutor getInstance() {
        return Holder.INSTANCE;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
