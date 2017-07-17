package com.sibilantsolutions.grisonforandroid.domain.usecase;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

import java.util.concurrent.Executor;

/**
 * Get a camera definition from the repository.
 * <p>
 * Created by jt on 7/16/17.
 */

public class GetCamDefUseCase implements UseCase<Integer, CamDef> {

    private final CamDefRepository camDefRepository;
    private final Executor executor;

    public GetCamDefUseCase(CamDefRepository camDefRepository) {
        this(camDefRepository, UseCaseExecutor.getInstance().getExecutorService());
    }

    public GetCamDefUseCase(CamDefRepository camDefRepository, Executor executor) {
        this.camDefRepository = camDefRepository;
        this.executor = executor;
    }

    @Override
    public void execute(final Integer parameter, final Callback<CamDef> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(camDefRepository.get(parameter));
                } catch (Exception e) {
                    callback.onError(new RuntimeException(e));
                }

            }
        });
    }

}
