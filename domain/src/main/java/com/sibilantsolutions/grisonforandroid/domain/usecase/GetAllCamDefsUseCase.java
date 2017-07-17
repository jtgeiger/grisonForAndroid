package com.sibilantsolutions.grisonforandroid.domain.usecase;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Get all camera definitions from the repository.
 * <p>
 * Created by jt on 7/16/17.
 */

public class GetAllCamDefsUseCase implements UseCase<Void, List<CamDef>> {

    private final CamDefRepository camDefRepository;
    private final Executor executor;

    public GetAllCamDefsUseCase(CamDefRepository camDefRepository) {
        this(camDefRepository, UseCaseExecutor.getInstance().getExecutorService());
    }

    public GetAllCamDefsUseCase(CamDefRepository camDefRepository, Executor executor) {
        this.camDefRepository = camDefRepository;
        this.executor = executor;
    }

    @Override
    public void execute(Void parameter, final Callback<List<CamDef>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(camDefRepository.getAll());
                } catch (Exception e) {
                    callback.onError(new RuntimeException(e));
                }

            }
        });
    }
}
