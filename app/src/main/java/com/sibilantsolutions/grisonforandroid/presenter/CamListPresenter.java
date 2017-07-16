package com.sibilantsolutions.grisonforandroid.presenter;

import android.util.Log;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.usecase.GetAllCamDefsUseCase;
import com.sibilantsolutions.grisonforandroid.domain.usecase.UseCase;

import java.util.List;

/**
 * Presenter for cam list.
 * <p>
 * Created by jt on 7/16/17.
 */

public class CamListPresenter implements CamListContract.Presenter {

    private static final String TAG = CamListPresenter.class.getSimpleName();

    private final CamListContract.View view;
    private final GetAllCamDefsUseCase getAllCamDefsUseCase;

    public CamListPresenter(CamListContract.View view, GetAllCamDefsUseCase getAllCamDefsUseCase) {
        this.view = view;
        this.getAllCamDefsUseCase = getAllCamDefsUseCase;
    }

    @Override
    public void getAllCamDefs() {
        getAllCamDefsUseCase.execute(null, new UseCase.Callback<List<CamDef>>() {
            @Override
            public void onSuccess(List<CamDef> result) {
                view.showAllCamDefs(result);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Trouble getting all camdefs", new RuntimeException(e));
                view.showError();
            }
        });
    }
}
