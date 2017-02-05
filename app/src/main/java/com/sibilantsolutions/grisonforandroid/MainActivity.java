package com.sibilantsolutions.grisonforandroid;

import android.app.ActivityOptions;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sibilantsolutions.grisonforandroid.domain.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.CamSession;
import com.sibilantsolutions.grisonforandroid.domain.CamStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MainActivity extends ListActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_ADD_CAM = 1;
    public static final String KEY_CAM_DEFS = "KEY_CAM_DEFS";
//    private AudioTrack audioTrack;

    private SharedPreferences sharedPreferences;

    private MyCamArrayAdapter myCamArrayAdapter;
    private ActionMode mActionMode;
    private CamService.CamServiceI camService;
    final private Observer observer = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            notifyDataSetChangedOnUiThread();
        }
    };
    private CamService.CamServiceI.StartCamListener startCamListener = new CamService.CamServiceI
            .StartCamListener() {
        @Override
        public void onCamStartResult(@NonNull CamSession camSession, boolean success) {
            Log.d(TAG, "onCamStartResult: success=" + success);
            if (success) {
                camService.startVideo(camSession);
            }
        }
    };

    final private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: name=" + name + ", service=" + service);
            camService = ((CamService.CamServiceBinder) service).getCamService();

            for (int i = 0; i < myCamArrayAdapter.getCount(); i++) {
                CamSession camSession = myCamArrayAdapter.getItem(i);
                assert camSession != null;
                camSession.addObserver(observer);
                camService.startCam(camSession, startCamListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: name=" + name);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final List<CamSession> camSessions = new ArrayList<>();

        final Set<String> strings = sharedPreferences.getStringSet(KEY_CAM_DEFS, null);
        if (strings != null) {
            for (String str : strings) {
                final CamDef camDef = deserialize(str);
                CamSession camSession = new CamSession(camDef);
                camSession.setCamStatus(CamStatus.CONNECTING);
                camSessions.add(camSession);
            }
        }

        startService(CamService.newIntent(this));


        final boolean boundService = bindService(new Intent(this, CamService.class), serviceConnection, BIND_AUTO_CREATE);

        Log.d(TAG, "onStart: bound service=" + boundService);


        myCamArrayAdapter = new MyCamArrayAdapter(this, camSessions);

        setListAdapter(myCamArrayAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final CamSession camSession = myCamArrayAdapter.getItem(position);
                assert camSession != null;
                final View camImageView = view.findViewById(R.id.cam_image_preview);
                final ActivityOptions activityOptions = ActivityOptions
                        .makeSceneTransitionAnimation(MainActivity.this, camImageView, getString
                                (R.string.camera_image_trans));
                startActivity(CamViewActivity.newIntent(MainActivity.this, camSession.getCamDef()),
                        activityOptions.toBundle());
            }
        });

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                // Start the CAB
                mActionMode = startActionMode(actionModeCallback(position));
                view.setSelected(true);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");
        super.onDestroy();

        unbindService(serviceConnection);
    }

    private Callback actionModeCallback(final int position) {
        return new Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_cam:
                        deleteItem(position);
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
            }

        };
    }

    private void deleteItem(final int position) {
        final CamSession camSession = myCamArrayAdapter.getItem(position);
        assert camSession != null;
//        if (camSession.foscamSession != null) {
//            camSession.foscamSession.disconnect();
//            camSession.foscamSession = null;
//        }
        Set<String> strings = sharedPreferences.getStringSet(KEY_CAM_DEFS, null);
        if (strings != null) {
            //Make a copy because the returned obj is not guaranteed to be editable.
            strings = new HashSet<>(strings);
            for (Iterator<String> iter = strings.iterator(); iter.hasNext(); ) {
                String camDefStr = iter.next();
                CamDef camDef = deserialize(camDefStr);
                if (camSession.getCamDef().equals(camDef)) {
                    iter.remove();
                    break;
                }
            }
            final Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_CAM_DEFS, strings);
            editor.apply();
        } else {
            Log.e(TAG, "deleteItem: cam defs was null");
        }

        myCamArrayAdapter.remove(camSession);

        if (camService != null) {
            camService.stopSession(camSession);
        }
    }


    public void onClickAddCam(View view) {
        startActivityForResult(new Intent(this, AddCamActivity.class), REQ_ADD_CAM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult.");
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

            CamSession camSession = new CamSession(camDef);
            camSession.setCamStatus(CamStatus.CONNECTING);

            myCamArrayAdapter.add(camSession);

            camService.startCam(camSession, startCamListener);
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

    private static class MyCamArrayAdapter extends ArrayAdapter<CamSession> {

        MyCamArrayAdapter(MainActivity context, List<CamSession> objects) {
            super(context, R.layout.card_cam_summary, objects);
        }

        private static class ViewHolder {
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

            CamSession camSession = getItem(position);
            assert camSession != null;
            CamDef camDef = camSession.getCamDef();
            viewHolder.camName.setText(camDef.getName());
            viewHolder.camAddress.setText(String.format(Locale.ROOT, "%s@%s:%d", camDef.getUsername(), camDef.getHost
                    (), camDef.getPort()));

            switch (camSession.getCamStatus()) {
                case CANT_CONNECT:
                case LOST_CONNECTION:
                    viewHolder.camLoadingProgressBar.setVisibility(View.INVISIBLE);
                    viewHolder.camPreview.setImageDrawable(getContext().getDrawable(android.R.drawable
                            .ic_dialog_alert));
                    viewHolder.camPreview.setVisibility(View.VISIBLE);
                    viewHolder.camStatus.setText(camSession.getReason());
                    break;

                case CONNECTED:
                    viewHolder.camLoadingProgressBar.setVisibility(View.INVISIBLE);
                    viewHolder.camStatus.setText(R.string.connected);
                    if (camSession.getCurBitmap() != null) {
                        viewHolder.camPreview.setImageBitmap(camSession.getCurBitmap());
                        viewHolder.camStatus.append(String.format(Locale.ROOT, " (%d x %d)",
                                camSession.getCurBitmap()
                                        .getWidth(), camSession.getCurBitmap().getHeight()));
                    } else {
                        viewHolder.camPreview.setImageDrawable(getContext().getDrawable(android.R.drawable
                                .ic_menu_camera));
                    }
                    viewHolder.camPreview.setVisibility(View.VISIBLE);
                    break;

                case CONNECTING:
                    viewHolder.camLoadingProgressBar.setVisibility(View.VISIBLE);
                    viewHolder.camPreview.setVisibility(View.INVISIBLE);
                    viewHolder.camStatus.setText(R.string.connecting);
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected status=" + camSession.getCamStatus());
            }

            return convertView;
        }
    }

    @Override
    protected void onStart() {

        Log.d(TAG, "onStart.");

        super.onStart();


//        for (int i = 0; i < myCamArrayAdapter.getCount(); i++) {
//            CamSession camSession = myCamArrayAdapter.getItem(i);
//            startCam(camSession);
//            camServiceBinder.startCam(camSession, new Runnable() {
//                @Override
//                public void run() {
//                    notifyDataSetChangedOnUiThread();
//                }
//            });
//        }

        //Make sure to show the current state of the session without having to wait for an update.
        notifyDataSetChangedOnUiThread();

        for (int i = 0; i < myCamArrayAdapter.getCount(); i++) {
            CamSession camSession = myCamArrayAdapter.getItem(i);
            assert camSession != null;
            camSession.addObserver(observer);
            if (camService != null) {
                camService.startVideo(camSession);
            }
        }

    }

//    private void startCam(final CamSession camSession) {
//        Runnable r = new Runnable() {
//
//            @Override
//            public void run() {
//                CamDef camDef = camSession.camDef;
//                String host = camDef.getHost();
//                int port = camDef.getPort();
//                String username = camDef.getUsername();
//                String password = camDef.getPassword();
//
//                if (TextUtils.isEmpty(host) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
//                    Log.i(TAG, "run: nothing to do.");
//                    return;
//                }
//
//                final InetSocketAddress address = new InetSocketAddress(host, port);
//
//                //TODO: Need to handle failed authentication (have a onAuthSuccess/onAuthFail handler).
//                //TODO: Grison separate threads for video vs audio; but -- how to keep them in sync?
//
//                final ImageHandlerI imageHandler = new ImageHandlerI() {
//                    @Override
//                    public void onReceive(VideoDataText videoData) {
//                        byte[] dataContent = videoData.getDataContent();
//                        final Bitmap bMap = BitmapFactory.decodeByteArray(dataContent, 0, dataContent.length);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                camSession.curBitmap = bMap;
//                                notifyDataSetChangedOnUiThread();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onVideoStopped(VideoStoppedEvt videoStoppedEvt) {
//
//                    }
//                };
//
//                AlarmHandlerI alarmHandler = new AlarmHandlerI() {
//                    @Override
//                    public void onAlarm(AlarmEvt evt) {
//                        //No-op.
//                    }
//                };
//
////                int millisecondsToBuffer = 150;
////                double percentOfASecondToBuffer = millisecondsToBuffer / 1000.0;
////                int bitsPerByte = 8;
////                int bufferSizeInBytes = (int) ((AdpcmDecoder.SAMPLE_SIZE_IN_BITS / bitsPerByte) * AdpcmDecoder
//// .SAMPLE_RATE * percentOfASecondToBuffer);
////                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, (int) AdpcmDecoder.SAMPLE_RATE,
////                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack
//// .MODE_STREAM);
////
////                AudioHandlerI audioHandler = new AudioHandlerI() {
////                    AdpcmDecoder adpcmDecoder = new AdpcmDecoder();
////
////                    @Override
////                    public void onAudioStopped(AudioStoppedEvt audioStoppedEvt) {
////                        //No-op.
////                    }
////
////                    @Override
////                    public void onReceive(AudioDataText audioData) {
////                        byte[] bytes = adpcmDecoder.decode(audioData.getDataContent());
////                        short[] shorts = byteArrayToShortArray(bytes, AdpcmDecoder.BIG_ENDIAN ? ByteOrder
//// .BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
////                        int numWritten = audioTrack.write(shorts, 0, shorts.length);
////                        if (numWritten != shorts.length) {
////                            throw new UnsupportedOperationException("array len=" + shorts.length + " but only wrote
//// " + numWritten + "byte(s)");
////                        }
////                    }
////                };
//                final AudioHandlerI audioHandler = new AudioHandlerI() {
//                    @Override
//                    public void onAudioStopped(AudioStoppedEvt audioStoppedEvt) {
//                        //No-op.
//                    }
//
//                    @Override
//                    public void onReceive(AudioDataText audioData) {
//                        //No-op.
//                    }
//                };
//                LostConnectionHandlerI lostConnHandler = new LostConnectionHandlerI() {
//                    @Override
//                    public void onLostConnection(LostConnectionEvt evt) {
//                        Log.i(TAG, "onLostConnection: ");
//                        camSession.camStatus = CamStatus.LOST_CONNECTION;
//                        camSession.reason = "Lost connection";
//                        notifyDataSetChangedOnUiThread();
//                    }
//                };
//
//                FoscamSession foscamSession;
//                boolean success = false;
//                try {
//                    camSession.camStatus = CamStatus.CONNECTING;
//                    notifyDataSetChangedOnUiThread();
//                    foscamSession = FoscamSession.connect(address, username, password, audioHandler,
//                            imageHandler, alarmHandler, lostConnHandler);
//                    success = foscamSession.videoStart();
//                    camSession.camStatus = success ? CamStatus.CONNECTED : CamStatus.CANT_CONNECT;
//                    if (!success) {
//                        camSession.reason = "Connected but couldn't start video";
//                        foscamSession.disconnect();
//                    } else {
//                        camSession.foscamSession = foscamSession;
//                    }
//                    notifyDataSetChangedOnUiThread();
//                } catch (Exception e) {
//                    Log.i(TAG, "run: " + address, e);
//                    camSession.camStatus = success ? CamStatus.CONNECTED : CamStatus.CANT_CONNECT;
//                    camSession.reason = "Could not connect to " + address;
//                    if (e.getLocalizedMessage() != null) {
//                        camSession.reason += ": " + e.getLocalizedMessage();
//                    }
//                    notifyDataSetChangedOnUiThread();
//                }
////                Log.i(TAG, "run: videoStart success=" + success);
////                boolean audioStartSuccess = foscamSession.audioStart();
////                if (audioStartSuccess) {
////                    audioTrack.play();
////                }
//                String audioStartSuccess = "N/A";
//                Log.i(TAG, "run: videoStart success=" + success + ", audioStartSuccess=" + audioStartSuccess);
//            }
//        };
//        new Thread(r, "mySessionConnector").start();
//    }

    private void notifyDataSetChangedOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myCamArrayAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    protected void onStop() {

        Log.d(TAG, "onStop.");

        super.onStop();

//        if (audioTrack != null) {
//            audioTrack.pause();
//            audioTrack.flush();
//            audioTrack.release();
//            audioTrack = null;
//        }

        for (int i = 0; i < myCamArrayAdapter.getCount(); i++) {
            CamSession camSession = myCamArrayAdapter.getItem(i);
            assert camSession != null;
//            if (camSession.foscamSession != null) {
//                camSession.foscamSession.disconnect();
//                camSession.foscamSession = null;
//            }
            camSession.deleteObserver(observer);
            camService.stopVideo(camSession);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume.");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause.");
        super.onPause();
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
