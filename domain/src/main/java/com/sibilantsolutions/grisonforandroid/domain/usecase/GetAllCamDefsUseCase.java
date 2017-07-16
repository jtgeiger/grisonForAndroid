package com.sibilantsolutions.grisonforandroid.domain.usecase;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

import java.util.List;

/**
 * Get all camera definitions from the repository.
 * <p>
 * Created by jt on 7/16/17.
 */

public class GetAllCamDefsUseCase implements UseCase<Void, List<CamDef>> {

    private final CamDefRepository camDefRepository;

    public GetAllCamDefsUseCase(CamDefRepository camDefRepository) {
        this.camDefRepository = camDefRepository;
    }

    @Override
    public void execute(Void parameter, Callback<List<CamDef>> callback) {
        try {
            callback.onSuccess(camDefRepository.getAll());
        } catch (Exception e) {
            callback.onError(new RuntimeException(e));
        }
    }
}
