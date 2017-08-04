package com.sibilantsolutions.grisonforandroid.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.util.Log;

import com.sibilantsolutions.grison.driver.foscam.domain.AudioDataText;
import com.sibilantsolutions.grison.driver.foscam.domain.VideoDataText;
import com.sibilantsolutions.grison.driver.foscam.net.FoscamSession;
import com.sibilantsolutions.grison.evt.AlarmEvt;
import com.sibilantsolutions.grison.evt.AlarmHandlerI;
import com.sibilantsolutions.grison.evt.AudioHandlerI;
import com.sibilantsolutions.grison.evt.AudioStoppedEvt;
import com.sibilantsolutions.grison.evt.ImageHandlerI;
import com.sibilantsolutions.grison.evt.LostConnectionEvt;
import com.sibilantsolutions.grison.evt.LostConnectionHandlerI;
import com.sibilantsolutions.grison.evt.VideoStoppedEvt;
import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.usecase.GetCamDefUseCase;
import com.sibilantsolutions.grisonforandroid.domain.usecase.UseCase;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


/**
 * Presenter for cam view.
 * <p>
 * Created by jt on 8/3/17.
 */
public class CamViewPresenter implements CamViewContract.Presenter {

    private static final String TAG = CamViewPresenter.class.getSimpleName();
    private final CamViewContract.View view;
    private final GetCamDefUseCase getCamDefUseCase;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile FoscamSession foscamSession;


    public CamViewPresenter(CamViewContract.View view, GetCamDefUseCase getCamDefUseCase) {
        this.view = view;
        this.getCamDefUseCase = getCamDefUseCase;
    }

    @Override
    @UiThread
    public void getCamDef(final int camId) {
        getCamDefUseCase.execute(camId, new MainThreadUseCaseCallbackDecorator<>(new UseCase.Callback<CamDef>() {
            @Override
            public void onSuccess(CamDef result) {
                view.onCamDefLoaded(result);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Trouble getting camdef=" + camId, new RuntimeException(e));
                view.showError();
            }
        }));
    }

    @Override
    @UiThread
    public void launchCam(final CamDef camDef) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                InetSocketAddress socketAddress = new InetSocketAddress(camDef.getHost(), camDef.getPort());
                foscamSession = FoscamSession.connect(socketAddress,
                        camDef.getUsername(), camDef.getPassword(), newAudioHandler(), newImageHandler(),
                        newAlarmHandler(), newLostConnectionHandler());
//                foscamSession.audioStart();
                foscamSession.videoStart();
            }
        };
        //TODO: Proper use case.
        Executors.newSingleThreadExecutor().execute(runnable);
    }

    @Override
    public void disconnect() {
        if (foscamSession != null) {
            foscamSession.disconnect();
        }
    }

    private LostConnectionHandlerI newLostConnectionHandler() {
        return new LostConnectionHandlerI() {
            @Override
            public void onLostConnection(LostConnectionEvt evt) {
                Log.i(TAG, "onLostConnection.");
            }
        };
    }

    private AlarmHandlerI newAlarmHandler() {
        return new AlarmHandlerI() {
            @Override
            public void onAlarm(AlarmEvt evt) {
                Log.i(TAG, "onAlarm.");
            }
        };
    }

    private ImageHandlerI newImageHandler() {
        return new ImageHandlerI() {
            @Override
            public void onReceive(VideoDataText videoData) {
//                Log.i(TAG, "onReceive: videoData");
                final byte[] dataContent = videoData.getDataContent();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(dataContent, 0, dataContent.length);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        view.onImageReceived(bitmap);
                    }
                });
            }

            @Override
            public void onVideoStopped(VideoStoppedEvt videoStoppedEvt) {
                Log.i(TAG, "onVideoStopped.");
            }
        };
    }

    private AudioHandlerI newAudioHandler() {
        return new AudioHandlerI() {
            @Override
            public void onAudioStopped(AudioStoppedEvt audioStoppedEvt) {
                Log.i(TAG, "onAudioStopped.");
            }

            @Override
            public void onReceive(AudioDataText audioData) {
                Log.i(TAG, "onReceive: audioData");
            }
        };
    }

}
