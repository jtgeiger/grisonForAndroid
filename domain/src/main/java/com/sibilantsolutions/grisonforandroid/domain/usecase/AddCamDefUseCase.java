package com.sibilantsolutions.grisonforandroid.domain.usecase;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

import java.util.concurrent.Executor;

/**
 * Add a camera definition to the repository.
 * <p>
 * Created by jt on 7/16/17.
 */

public class AddCamDefUseCase implements UseCase<CamDef, Integer> {

    private final CamDefRepository camDefRepository;
    private final Executor executor;

    public AddCamDefUseCase(CamDefRepository camDefRepository) {
        this(camDefRepository, UseCaseExecutor.getInstance().getExecutorService());
    }

    public AddCamDefUseCase(CamDefRepository camDefRepository, Executor executor) {
        this.camDefRepository = camDefRepository;
        this.executor = executor;
    }

    @Override
    public void execute(final CamDef parameter, final Callback<Integer> callback) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(camDefRepository.add(parameter));
                } catch (Exception e) {
                    callback.onError(new RuntimeException(e));
                }
            }
        });
    }

}
