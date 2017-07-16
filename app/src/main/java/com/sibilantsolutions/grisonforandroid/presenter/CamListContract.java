package com.sibilantsolutions.grisonforandroid.presenter;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;

import java.util.List;

/**
 * Contract interface for cam list.
 * <p>
 * Created by jt on 7/16/17.
 */

public interface CamListContract {

    interface Presenter {

        void getAllCamDefs();
    }

    interface View {

        void showAllCamDefs(List<CamDef> camDefs);

        void showError();
    }

}
