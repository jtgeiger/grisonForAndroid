package com.sibilantsolutions.grisonforandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

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

import java.net.InetSocketAddress;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageView mImageView;
    private FoscamSession foscamSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_view);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Runnable r = new Runnable() {

            @Override
            public void run() {
                final InetSocketAddress address = new InetSocketAddress("192.168.43.203", 50080);

                //TODO: Grison threads should be daemons so they will die when the UI does.
                //TODO: Need to handle failed authentication (have a onAuthSuccess/onAuthFail handler).
                //TODO: Audio decoder without java dependencies.
                //TODO: Need a session.close() method!

                final String username = "TODOuser";
                final String password = "TODOpass";

                final ImageHandlerI imageHandler = new ImageHandlerI() {
                    @Override
                    public void onReceive(VideoDataText videoData) {
                        Log.i(TAG, "onReceive: ");

                        byte[] dataContent = videoData.getDataContent();
                        final Bitmap bMap = BitmapFactory.decodeByteArray(dataContent, 0, dataContent.length);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(bMap);
                            }
                        });
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

                final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 500000, AudioTrack.MODE_STREAM);

                AudioHandlerI audioHandler = new AudioHandlerI() {
                    HACK_AdpcmDecoder adpcmDecoder = new HACK_AdpcmDecoder();

                    @Override
                    public void onAudioStopped(AudioStoppedEvt audioStoppedEvt) {
                        //No-op.
                    }

                    @Override
                    public void onReceive(AudioDataText audioData) {
                        byte[] bytes = adpcmDecoder.decode(audioData.getDataContent());
                        audioTrack.write(bytes, 0, bytes.length);
                    }
                };
                LostConnectionHandlerI lostConnHandler = new LostConnectionHandlerI() {
                    @Override
                    public void onLostConnection(LostConnectionEvt evt) {
                        Log.i(TAG, "onLostConnection: ");
                    }
                };

                foscamSession = FoscamSession.connect(address, username, password, audioHandler, imageHandler, alarmHandler, lostConnHandler);
                boolean success = foscamSession.videoStart();
                boolean audioStartSuccess = foscamSession.audioStart();
                if (audioStartSuccess) {
                    audioTrack.play();
                }
                Log.i(TAG, "run: videoStart success=" + success + ", audioStartSuccess=" + audioStartSuccess);
            }
        };
        new Thread(r, "mySessionConnector").start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (foscamSession != null) {
            foscamSession.audioEnd();
            foscamSession.videoEnd();
            foscamSession.talkEnd();
        }
    }

}
