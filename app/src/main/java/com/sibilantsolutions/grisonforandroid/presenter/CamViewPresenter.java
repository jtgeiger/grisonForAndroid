package com.sibilantsolutions.grisonforandroid.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.sibilantsolutions.grison.driver.foscam.domain.AudioDataText;
import com.sibilantsolutions.grison.driver.foscam.domain.VideoDataText;
import com.sibilantsolutions.grison.evt.AlarmEvt;
import com.sibilantsolutions.grison.evt.AlarmHandlerI;
import com.sibilantsolutions.grison.evt.AudioHandlerI;
import com.sibilantsolutions.grison.evt.AudioStoppedEvt;
import com.sibilantsolutions.grison.evt.ImageHandlerI;
import com.sibilantsolutions.grison.evt.LostConnectionEvt;
import com.sibilantsolutions.grison.evt.LostConnectionHandlerI;
import com.sibilantsolutions.grison.evt.VideoStoppedEvt;
import com.sibilantsolutions.grison.sound.adpcm.AdpcmDecoder;
import com.sibilantsolutions.grisonforandroid.data.repository.FoscamCamSessionFactoryImpl;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamSession;
import com.sibilantsolutions.grisonforandroid.domain.usecase.GetCamDefUseCase;
import com.sibilantsolutions.grisonforandroid.domain.usecase.StartCamSessionUseCase;
import com.sibilantsolutions.grisonforandroid.domain.usecase.UseCase;
import com.sibilantsolutions.grisonforandroid.domain.usecase.UseCaseExecutor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Presenter for cam view.
 * <p>
 * Created by jt on 8/3/17.
 */
public class CamViewPresenter implements CamViewContract.Presenter {

    private static final String TAG = CamViewPresenter.class.getSimpleName();

    private final CamViewContract.View view;
    private final StartCamSessionUseCase startCamSessionUseCase;

    private final Handler handler = new Handler(Looper.getMainLooper());

    //TODO: What thread for accessing this?
    private CamSession camSession;

    //TOOD: Proper use case.
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    public CamViewPresenter(CamViewContract.View view, GetCamDefUseCase getCamDefUseCase) {
        this.view = view;
        startCamSessionUseCase = new StartCamSessionUseCase(getCamDefUseCase,
                new FoscamCamSessionFactoryImpl(newAudioHandler(), newImageHandler(),
                        newAlarmHandler(), newLostConnectionHandler()),
                UseCaseExecutor.getInstance().getExecutorService());
    }

    @Override
    public void launchCam(int camDefId) {
        startCamSessionUseCase.execute(camDefId, new UseCase.Callback<CamSession>() {
            @Override
            @WorkerThread
            public void onSuccess(CamSession result) {
                CamViewPresenter.this.camSession = result;
                final boolean isVideoStarted = result.startVideo();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setVideo(isVideoStarted);
                        view.setVideoChangeEnabled(true);
                        view.setAudio(false);
                        view.setAudioChangeEnabled(true);
                    }
                });
            }

            @Override
            @WorkerThread
            public void onError(Exception e) {
                throw new UnsupportedOperationException("TODO (IMB)", e);
            }
        });
    }

    @Override
    public void disconnect() {
        Runnable runnable = new Runnable() {
            public void run() {
                if (camSession != null) {
                    camSession.disconnect();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setVideo(false);
                            view.setVideoChangeEnabled(false);
                            view.setAudio(false);
                            view.setAudioChangeEnabled(false);
                        }
                    });
                }
            }
        };
        executor.execute(runnable);
    }

    @Override
    public void setVideo(final boolean isVideoOn) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (camSession != null) {
                    final boolean isVideoStarted;
                    if (isVideoOn) {
                        isVideoStarted = camSession.startVideo();
                    } else {
                        camSession.stopVideo();
                        isVideoStarted = false;
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setVideo(isVideoStarted);
                        }
                    });
                }
            }
        };
        executor.execute(runnable);
    }

    @Override
    public void setAudio(final boolean isAudioOn) {

        Runnable runnable = new Runnable() {
            public void run() {
                if (camSession != null) {
                    final boolean isAudioStarted;
                    if (isAudioOn) {
                        isAudioStarted = camSession.startAudio();
                    } else {
                        camSession.stopAudio();
                        isAudioStarted = false;
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setAudio(isAudioStarted);
                        }
                    });
                }
            }
        };
        executor.execute(runnable);
    }

    private LostConnectionHandlerI newLostConnectionHandler() {
        return new LostConnectionHandlerI() {
            @Override
            public void onLostConnection(LostConnectionEvt evt) {
                Log.i(TAG, "onLostConnection.");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setVideo(false);
                        view.setVideoChangeEnabled(false);
                        view.setAudio(false);
                        view.setAudioChangeEnabled(false);
                    }
                });
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
        class MyAudioHandler implements AudioHandlerI {

            private final AdpcmDecoder adpcmDecoder = new AdpcmDecoder();
            private final AudioTrack audioTrack;

            MyAudioHandler() {
                final int millisecondsToBuffer = 150;
                final double percentOfASecondToBuffer = millisecondsToBuffer / 1000.0;
                final int bitsPerByte = 8;
                final int bufferSizeInBytes = (int) ((AdpcmDecoder.SAMPLE_SIZE_IN_BITS / bitsPerByte) * AdpcmDecoder
                        .SAMPLE_RATE * percentOfASecondToBuffer);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, (int) AdpcmDecoder.SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack
                        .MODE_STREAM);
                audioTrack.play();  //TODO: This doesn't belong here; should happen when audio started.
            }

            @Override
            public void onAudioStopped(AudioStoppedEvt audioStoppedEvt) {
                Log.i(TAG, "onAudioStopped.");
            }

            @Override
            public void onReceive(AudioDataText audioData) {
//                Log.i(TAG, "onReceive: audioData");
                byte[] bytes = adpcmDecoder.decode(audioData.getDataContent());
                short[] shorts = byteArrayToShortArray(bytes, AdpcmDecoder.BIG_ENDIAN ? ByteOrder
                        .BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                int numWritten = audioTrack.write(shorts, 0, shorts.length);
                if (numWritten != shorts.length) {
                    throw new UnsupportedOperationException("array len=" + shorts.length +
                            " but only wrote " + numWritten + "byte(s)");
                }
            }
        }

        return new MyAudioHandler();
    }

    private short[] byteArrayToShortArray(byte[] bytes, ByteOrder byteOrder) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(byteOrder);
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = bb.getShort();
        }

        return shorts;
    }

}
