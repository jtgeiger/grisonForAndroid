package com.sibilantsolutions.grisonforandroid;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MainActivity extends ListActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_ADD_CAM = 1;
    public static final String KEY_CAM_DEFS = "KEY_CAM_DEFS";
//    private ImageView mImageView;
//    private FoscamSession foscamSession;
//    private AudioTrack audioTrack;

    private Map<CamDef, CamConnectionListener> camDefCamConnectionListenerMap = new HashMap<>();

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mImageView = (ImageView) findViewById(R.id.image_view);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final ArrayList<CamDef> camDefs = new ArrayList<>();

        Set<String> strings = sharedPreferences.getStringSet(KEY_CAM_DEFS, null);
        if (strings != null) {
            for (String str : strings) {
                final CamDef camDef = deserialize(str);
                camDefs.add(camDef);
            }
        }

        setListAdapter(new MyCamAdapter(this, camDefs));
    }

    public void onClickAddCam(View view) {
        startActivityForResult(new Intent(this, AddCamActivity.class), REQ_ADD_CAM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_ADD_CAM && resultCode == RESULT_OK) {
            final CamDef camDef = (CamDef) data.getSerializableExtra(AddCamActivity.EXTRA_CAM_DEF);

            Set<String> strings = sharedPreferences.getStringSet(KEY_CAM_DEFS, null);
            if (strings == null) {
                strings = new HashSet<>();
            } else {
                //Make a copy because the returned obj is not guaranteed to be editable.
                strings = new HashSet<>(strings);
            }
            strings.add(serialize(camDef));
            final Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_CAM_DEFS, strings);
            editor.apply();

            final MyCamAdapter listAdapter = (MyCamAdapter) getListAdapter();
            listAdapter.add(camDef);

            startCam(camDef);
        }
    }

    CamDef deserialize(String s) {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes(ISO_8859_1));
        final Object obj;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new UnsupportedOperationException("TODO (CSB)");
        }

        if (obj instanceof CamDef) {
            return (CamDef) obj;
        }

        throw new IllegalArgumentException("Expected obj type=" + CamDef.class.getName() + ", got=" + obj.getClass()
                .getName());
    }

    String serialize(CamDef camDef) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(camDef);
        } catch (IOException e) {
            throw new UnsupportedOperationException("TODO (CSB)", e);
        }

        final byte[] bytes = byteArrayOutputStream.toByteArray();
        return new String(bytes, ISO_8859_1);
    }

    private interface CamConnectionListener {

        void onConnecting();

        void onConnected();

        void onDisconnected(String reason);
    }

    private static class MyCamAdapter extends ArrayAdapter<CamDef> {

        private final MainActivity activity;

        public MyCamAdapter(MainActivity context, List<CamDef> objects) {
            super(context, R.layout.card_cam_summary, objects);
            this.activity = context;
        }

        private static class ViewHolder {
            CamDef curCamDef;
            ImageView camPreview;
            ProgressBar camLoadingProgressBar;
            TextView camName;
            TextView camAddress;
            TextView camStatus;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_cam_summary, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);

                viewHolder.camPreview = (ImageView) convertView.findViewById(R.id.cam_image_preview);
                viewHolder.camLoadingProgressBar = (ProgressBar) convertView.findViewById(R.id.cam_image_progress_bar);
                viewHolder.camName = (TextView) convertView.findViewById(R.id.cam_name);
                viewHolder.camAddress = (TextView) convertView.findViewById(R.id.cam_address);
                viewHolder.camStatus = (TextView) convertView.findViewById(R.id.cam_status);
            }

            final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder.curCamDef != null) {
                activity.camDefCamConnectionListenerMap.remove(viewHolder.curCamDef);
            }
            CamDef camDef = getItem(position);
            assert camDef != null;
            viewHolder.curCamDef = camDef;
            viewHolder.camName.setText(camDef.getName());
            viewHolder.camAddress.setText(String.format(Locale.ROOT, "%s@%s:%d", camDef.getUsername(), camDef.getHost
                    (), camDef.getPort()));

            activity.camDefCamConnectionListenerMap.put(camDef, new CamConnectionListener() {

                @Override
                public void onConnecting() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.camLoadingProgressBar.setVisibility(View.VISIBLE);
                            viewHolder.camPreview.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                @Override
                public void onConnected() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                    viewHolder.camLoadingProgressBar.setVisibility(View.INVISIBLE);
                    viewHolder.camPreview.setImageDrawable(getContext().getDrawable(android.R.drawable.ic_menu_camera));
                    viewHolder.camPreview.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void onDisconnected(final String reason) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.camLoadingProgressBar.setVisibility(View.INVISIBLE);
                            viewHolder.camPreview.setImageDrawable(getContext().getDrawable(android.R.drawable
                                    .ic_dialog_alert));
                            viewHolder.camPreview.setVisibility(View.VISIBLE);
                            viewHolder.camStatus.setText(reason);
                        }
                    });
                }
            });

            return convertView;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final MyCamAdapter listAdapter = (MyCamAdapter) getListAdapter();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            CamDef camDef = listAdapter.getItem(i);
            startCam(camDef);
        }

    }

    private void startCam(final CamDef camDef) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                String host = camDef.getHost();
                int port = camDef.getPort();
                String username = camDef.getUsername();
                String password = camDef.getPassword();

                if (TextUtils.isEmpty(host) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Log.i(TAG, "run: nothing to do.");
                    return;
                }

                final InetSocketAddress address = new InetSocketAddress(host, port);

                //TODO: Need to handle failure to connect the TCP socket.
                //TODO: Grison threads should be daemons so they will die when the UI does.
                //TODO: Need to handle failed authentication (have a onAuthSuccess/onAuthFail handler).
                //TODO: Grison separate threads for video vs audio; but -- how to keep them in sync?

                final ImageHandlerI imageHandler = new ImageHandlerI() {
                    @Override
                    public void onReceive(VideoDataText videoData) {
                        byte[] dataContent = videoData.getDataContent();
                        final Bitmap bMap = BitmapFactory.decodeByteArray(dataContent, 0, dataContent.length);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //TODO mImageView.setImageBitmap(bMap);
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
//                            throw new UnsupportedOperationException("array len=" + shorts.length + " but only wrote
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
                    }
                };

                FoscamSession foscamSession;
                boolean success = false;
                final CamConnectionListener camConnectionListener = camDefCamConnectionListenerMap.get(camDef);
                try {
                    if (camConnectionListener != null) {
                        camConnectionListener.onConnecting();
                    }
                    foscamSession = FoscamSession.connect(address, username, password, audioHandler,
                            imageHandler, alarmHandler, lostConnHandler);
                    success = foscamSession.videoStart();
                    if (camConnectionListener != null) {
                        if (success)
                            camConnectionListener.onConnected();
                        else
                            camConnectionListener.onDisconnected("Could not start video at " + address);
                    }
                } catch (Exception e) {
                    if (camConnectionListener != null) {
                        camConnectionListener.onDisconnected("Could not connect to " + address);
                    }
                }
//                Log.i(TAG, "run: videoStart success=" + success);
//                boolean audioStartSuccess = foscamSession.audioStart();
//                if (audioStartSuccess) {
//                    audioTrack.play();
//                }
                String audioStartSuccess = "N/A";
                Log.i(TAG, "run: videoStart success=" + success + ", audioStartSuccess=" + audioStartSuccess);
            }
        };
        new Thread(r, "mySessionConnector").start();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (foscamSession != null) {
//            foscamSession.disconnect();
//            foscamSession = null;
//        }
//        if (audioTrack != null) {
//            audioTrack.pause();
//            audioTrack.flush();
//            audioTrack.release();
//            audioTrack = null;
//        }
//
//        if (mImageView != null) {
//            mImageView.setImageDrawable(null);
//        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.prefs_menu:
                Intent intent = new Intent(this, AppPreferencesActivity.class);
                startActivity(intent);
                return true;

            default:
                return false;
        }
    }

}
