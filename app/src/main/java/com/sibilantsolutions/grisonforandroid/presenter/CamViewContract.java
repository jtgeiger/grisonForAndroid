package com.sibilantsolutions.grisonforandroid.presenter;

import android.graphics.Bitmap;
import android.support.annotation.UiThread;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;

/**
 * Contract interface for cam view.
 * <p>
 * Created by jt on 8/3/17.
 */

public interface CamViewContract {

    interface Presenter {

        @UiThread
        void getCamDef(int camId);

        @UiThread
        void launchCam(CamDef camDef);

        @UiThread
        void disconnect();
    }

    interface View {

        @UiThread
        void onCamDefLoaded(CamDef camDef);

        @UiThread
        void showError();

        @UiThread
        void onImageReceived(Bitmap bitmap);
    }

}
