package com.sibilantsolutions.grisonforandroid.presenter;

import android.util.Log;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.usecase.AddCamDefUseCase;
import com.sibilantsolutions.grisonforandroid.domain.usecase.UseCase;

/**
 * Presenter for add cam.
 * <p>
 * Created by jt on 7/16/17.
 */

public class AddCamPresenter implements AddCamContract.Presenter {

    private static final String TAG = AddCamPresenter.class.getSimpleName();

    private final AddCamContract.View view;
    private final AddCamDefUseCase addCamDefUseCase;

    public AddCamPresenter(AddCamContract.View view, AddCamDefUseCase addCamDefUseCase) {
        this.view = view;
        this.addCamDefUseCase = addCamDefUseCase;
    }

    @Override
    public void addCamDef(CamDef camDef) {
        addCamDefUseCase.execute(camDef, new UseCase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.returnToList();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Trouble adding camdef", new RuntimeException(e));
                view.showError();
            }
        });
    }

}
