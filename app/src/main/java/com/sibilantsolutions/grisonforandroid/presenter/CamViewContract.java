package com.sibilantsolutions.grisonforandroid.presenter;

import android.graphics.Bitmap;
import android.support.annotation.UiThread;

/**
 * Contract interface for cam view.
 * <p>
 * Created by jt on 8/3/17.
 */

public interface CamViewContract {

    interface Presenter {

        void launchCam(int camDefId);

        @UiThread
        void disconnect();

        @UiThread
        void setVideo(boolean isVideoOn);
    }

    interface View {

        @UiThread
        void showError();

        @UiThread
        void onImageReceived(Bitmap bitmap);

        @UiThread
        void setVideo(boolean isVideoOn);

        @UiThread
        void setVideoChangeEnabled(boolean isVideoChangeEnabled);

    }

}
