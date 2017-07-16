package com.sibilantsolutions.grisonforandroid.domain.usecase;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

/**
 * Add a camera definition to the repository.
 * <p>
 * Created by jt on 7/16/17.
 */

public class AddCamDefUseCase implements UseCase<CamDef, Integer> {

    private CamDefRepository camDefRepository;

    public AddCamDefUseCase(CamDefRepository camDefRepository) {
        this.camDefRepository = camDefRepository;
    }

    @Override
    public void execute(CamDef parameter, Callback<Integer> callback) {
        try {
            callback.onSuccess(camDefRepository.add(parameter));
        } catch (Exception e) {
            callback.onError(e);
        }
    }

}
