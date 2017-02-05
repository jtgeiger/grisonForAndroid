package com.sibilantsolutions.grisonforandroid;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.sibilantsolutions.grisonforandroid.domain.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.CamSession;
import com.sibilantsolutions.grisonforandroid.domain.CamStatus;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CamService extends Service {

    private static final String TAG = CamService.class.getSimpleName();

    public static Intent newIntent(Context context) {
        return new Intent(context, CamService.class);
    }

    public interface CamServiceI {

        @Nullable
        CamSession getCamSession(@NonNull CamDef camDef);

        void startCam(@NonNull CamSession camSession, @NonNull StartCamListener startCamListener);

        void startVideo(@NonNull CamSession camSession);

        void stopVideo(@NonNull CamSession camSession);

        void stopSession(@NonNull CamSession camSession);

        interface StartCamListener {
            void onCamStartResult(@NonNull CamSession camSession, boolean success);
        }

    }

    private static class Cammy implements CamServiceI {

        private final Map<CamSession, FoscamSession> camSessionFoscamSessionMap = new HashMap<>();

        private final Map<CamSession, Integer> camSessionVideoCountMap = new HashMap<>();

        //TODO: Single thread executor, one for each session.
        //TODO: Use submit instead of execute and track result and possible exceptions.
        private ExecutorService executorService = Executors.newCachedThreadPool();

        @Override
        @Nullable
        public CamSession getCamSession(@NonNull CamDef camDef) {
            for (CamSession camSession : camSessionFoscamSessionMap.keySet()) {
                if (camSession.getCamDef().equals(camDef)) {
                    return camSession;
                }
            }
            return null;
        }

        @Override
        public void startCam(@NonNull final CamSession camSession, @NonNull final
        StartCamListener startCamListener) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    CamDef camDef = camSession.getCamDef();
                    String host = camDef.getHost();
                    int port = camDef.getPort();
                    String username = camDef.getUsername();
                    String password = camDef.getPassword();

                    if (TextUtils.isEmpty(host) || TextUtils.isEmpty(username) || TextUtils
                            .isEmpty(password)) {
                        Log.i(TAG, "run: nothing to do.");
                        return;
                    }

                    final InetSocketAddress address = new InetSocketAddress(host, port);

                    //TODO: Need to handle failed authentication (have a onAuthSuccess/onAuthFail handler).
                    //TODO: Grison separate threads for video vs audio; but -- how to keep them
                    // in sync?

                    final ImageHandlerI imageHandler = new ImageHandlerI() {
                        @Override
                        public void onReceive(VideoDataText videoData) {
                            byte[] dataContent = videoData.getDataContent();
                            //                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
                            camSession.setCurBitmap(BitmapFactory.decodeByteArray(dataContent, 0,
                                    dataContent.length));
//                                notifyDataSetChangedOnUiThread();
//                            dataSetChangedCallback.run();
                            camSession.notifyObservers();
//                            }
//                        });
                        }

                        @Override
                        public void onVideoStopped(VideoStoppedEvt videoStoppedEvt) {

                        }
                    };

                    AlarmHandlerI alarmHandler = new AlarmHandlerI() {
                        @Override
                        public void onAlarm(AlarmEvt evt) {
                            //No-op.
                        }
                    };

//                int millisecondsToBuffer = 150;
//                double percentOfASecondToBuffer = millisecondsToBuffer / 1000.0;
//                int bitsPerByte = 8;
//                int bufferSizeInBytes = (int) ((AdpcmDecoder.SAMPLE_SIZE_IN_BITS / bitsPerByte) * AdpcmDecoder
// .SAMPLE_RATE * percentOfASecondToBuffer);
//                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, (int) AdpcmDecoder.SAMPLE_RATE,
//                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack
// .MODE_STREAM);
//
//                AudioHandlerI audioHandler = new AudioHandlerI() {
//                    AdpcmDecoder adpcmDecoder = new AdpcmDecoder();
//
//                    @Override
//                    public void onAudioStopped(AudioStoppedEvt audioStoppedEvt) {
//                        //No-op.
//                    }
//
//                    @Override
//                    public void onReceive(AudioDataText audioData) {
//                        byte[] bytes = adpcmDecoder.decode(audioData.getDataContent());
//                        short[] shorts = byteArrayToShortArray(bytes, AdpcmDecoder.BIG_ENDIAN ? ByteOrder
// .BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
//                        int numWritten = audioTrack.write(shorts, 0, shorts.length);
//                        if (numWritten != shorts.length) {
//                            throw new UnsupportedOperationException("array len=" + shorts
// .length + " but only wrote
// " + numWritten + "byte(s)");
//                        }
//                    }
//                };
                    final AudioHandlerI audioHandler = new AudioHandlerI() {
                        @Override
                        public void onAudioStopped(AudioStoppedEvt audioStoppedEvt) {
                            //No-op.
                        }

                        @Override
                        public void onReceive(AudioDataText audioData) {
                            //No-op.
                        }
                    };
                    LostConnectionHandlerI lostConnHandler = new LostConnectionHandlerI() {
                        @Override
                        public void onLostConnection(LostConnectionEvt evt) {
                            Log.i(TAG, "onLostConnection: ");
                            camSession.setCamStatus(CamStatus.LOST_CONNECTION);
                            camSession.setReason("Lost connection");
//                        notifyDataSetChangedOnUiThread();
//                            dataSetChangedCallback.run();
                            camSession.notifyObservers();
                        }
                    };

                    final FoscamSession foscamSession;
                    camSession.setCamStatus(CamStatus.CONNECTING);
                    camSession.notifyObservers();
                    try {
                        foscamSession = FoscamSession.connect(address, username, password,
                                audioHandler,
                                imageHandler, alarmHandler, lostConnHandler);
                    } catch (Exception e) {
                        Log.i(TAG, "run: " + address, e);
                        camSession.setCamStatus(CamStatus.CANT_CONNECT);
                        camSession.setReason("Could not connect to " + address);
                        if (e.getLocalizedMessage() != null) {
                            camSession.setReason(camSession.getReason() + ": " + e
                                    .getLocalizedMessage());
                        }
                        camSession.notifyObservers();
                        startCamListener.onCamStartResult(camSession, false);
                        return;
                    }

                    camSession.setCamStatus(CamStatus.CONNECTED);
                    camSessionFoscamSessionMap.put(camSession, foscamSession);
                    camSessionVideoCountMap.put(camSession, 0);
                    camSession.notifyObservers();
                    startCamListener.onCamStartResult(camSession, true);

//                Log.i(TAG, "run: videoStart success=" + success);
//                boolean audioStartSuccess = foscamSession.audioStart();
//                if (audioStartSuccess) {
//                    audioTrack.play();
//                }
//                    String audioStartSuccess = "N/A";
//                    Log.i(TAG, "run: videoStart success=" + success + ", audioStartSuccess=" +
//                            audioStartSuccess);
                }
            };

            executorService.execute(r);
        }

        @Override
        public void startVideo(@NonNull final CamSession camSession) {
            if (camSessionVideoCountMap.get(camSession) == 0) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        final FoscamSession foscamSession = camSessionFoscamSessionMap.get
                                (camSession);

                        if (foscamSession != null) {
                            final boolean success = foscamSession.videoStart();
                            if (success) {
                                camSessionVideoCountMap.put(camSession, 1);
                            }
                        }
                    }
                };
                executorService.execute(runnable);
            } else {
                camSessionVideoCountMap.put(camSession, camSessionVideoCountMap.get(camSession) +
                        1);
            }
        }

        @Override
        public void stopVideo(@NonNull final CamSession camSession) {
            if (camSessionVideoCountMap.get(camSession) > 0) {
                camSessionVideoCountMap.put(camSession, camSessionVideoCountMap.get(camSession) -
                        1);

                if (camSessionVideoCountMap.get(camSession) == 0) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            final FoscamSession foscamSession = camSessionFoscamSessionMap.get
                                    (camSession);

                            if (foscamSession != null) {
                                foscamSession.videoEnd();
                            }
                        }
                    };
                    executorService.execute(runnable);
                }
            }

            //TODO: Start a timer and if nobody connects to video or audio, disconnect from camera.
        }

        @Override
        public void stopSession(@NonNull final CamSession camSession) {
            Runnable runnable = new Runnable() {
                public void run() {
                    final FoscamSession foscamSession = camSessionFoscamSessionMap.remove
                            (camSession);
                    if (foscamSession != null) {
                        foscamSession.disconnect();
                    }
                }
            };
            executorService.execute(runnable);
        }

        private void shutdown() {
            Runnable runnable = new Runnable() {
                public void run() {
                    for (Iterator<Map.Entry<CamSession, FoscamSession>> iterator =
                         camSessionFoscamSessionMap.entrySet().iterator(); iterator.hasNext(); ) {

                        final Map.Entry<CamSession, FoscamSession> entry = iterator.next();
                        iterator.remove();
                        entry.getValue().disconnect();
                    }
                }
            };

            executorService.execute(runnable);

            shutdownAndAwaitTermination(executorService);
        }

        static void shutdownAndAwaitTermination(ExecutorService pool) {
            pool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }

    }

    private Cammy cammy;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate.");
        cammy = new Cammy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy.");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind.");

        cammy.shutdown();

        stopSelf();

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind.");
        return new CamServiceBinder();
    }

    public class CamServiceBinder extends Binder {

//        public void startCam(MainActivity.CamSession camSession, Runnable
// dataSetChangedCallback) {
//            CamService.this.startCam(camSession, dataSetChangedCallback);
//        }
//
//        public FoscamSession getFoscamSession(MainActivity.CamSession camSession) {
//            return camSessionFoscamSessionMap.get(camSession);
//        }

        CamServiceI getCamService() {
            return cammy;
        }

    }

}
