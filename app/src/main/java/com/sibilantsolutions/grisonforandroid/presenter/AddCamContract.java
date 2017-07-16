package com.sibilantsolutions.grisonforandroid.presenter;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;

/**
 * Contract interface for add cam.
 * <p>
 * Created by jt on 7/16/17.
 */

public interface AddCamContract {

    interface Presenter {

        void addCamDef(CamDef camDef);
    }

    interface View {
        void showError();

        void returnToList();
    }

}
