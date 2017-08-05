package com.sibilantsolutions.grisonforandroid.domain.usecase;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamSession;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamSessionFactory;

import java.util.concurrent.Executor;

/**
 * Use case to start a camera session.
 * <p>
 * Created by jt on 8/4/17.
 */

public class StartCamSessionUseCase implements UseCase<Integer, CamSession> {

    private final GetCamDefUseCase getCamDefUseCase;
    private final CamSessionFactory camSessionFactory;
    private final Executor executor;

    public StartCamSessionUseCase(GetCamDefUseCase getCamDefUseCase,
                                  CamSessionFactory camSessionFactory, Executor executor) {
        this.getCamDefUseCase = getCamDefUseCase;
        this.camSessionFactory = camSessionFactory;
        this.executor = executor;
    }

    @Override
    public void execute(final Integer camDefId, final Callback<CamSession> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //This (might) start its own thread (depending on the executor it has) but that
                // shouldn't be a problem.
                getCamDefUseCase.execute(camDefId, new Callback<CamDef>() {
                    @Override
                    public void onSuccess(CamDef result) {
                        final CamSession camSession = camSessionFactory.newSession(result);
                        callback.onSuccess(camSession);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(new RuntimeException(e));
                    }
                });

            }
        });
    }

}
