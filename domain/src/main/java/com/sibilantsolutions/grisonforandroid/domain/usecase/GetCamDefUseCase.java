package com.sibilantsolutions.grisonforandroid.domain.usecase;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

/**
 * Get a camera definition from the repository.
 * <p>
 * Created by jt on 7/16/17.
 */

public class GetCamDefUseCase implements UseCase<Integer, CamDef> {

    private final CamDefRepository camDefRepository;

    public GetCamDefUseCase(CamDefRepository camDefRepository) {
        this.camDefRepository = camDefRepository;
    }

    @Override
    public void execute(Integer parameter, Callback<CamDef> callback) {
        try {
            callback.onSuccess(camDefRepository.get(parameter));
        } catch (Exception e) {
            callback.onError(new RuntimeException(e));
        }
    }

}
