package com.sibilantsolutions.grisonforandroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;

import com.sibilantsolutions.grisonforandroid.domain.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.CamSession;

import java.util.Observable;
import java.util.Observer;

public class CamViewActivity extends Activity {

    private static final String TAG = CamViewActivity.class.getSimpleName();

    private static final String EXTRA_CAMDEF = "EXTRA_CAMDEF";

    private CamDef camDef;
    private ImageView camImage;
    private ServiceConnection serviceConnection;

    final private Observer observer = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            updateOnUiThread();
        }
    };

    private void updateOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                camImage.setImageBitmap(camSession.getCurBitmap());
            }
        });
    }

    private CamSession camSession;

    public static Intent newIntent(Context context, CamDef camDef) {
        final Intent intent = new Intent(context, CamViewActivity.class);
        intent.putExtra(EXTRA_CAMDEF, camDef);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_view);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        camDef = (CamDef) getIntent().getSerializableExtra(EXTRA_CAMDEF);

        camImage = (ImageView) findViewById(R.id.cam_image);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected: name=" + name + ", service=" + service);
                CamService.CamServiceI camService = ((CamService.CamServiceBinder) service)
                        .getCamService();
                camSession = camService.getCamSession(camDef);
                assert camSession != null;
                camSession.addObserver(observer);
                camImage.setImageBitmap(camSession.getCurBitmap());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: name=" + name);
            }
        };

        bindService(CamService.newIntent(this), serviceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop.");
        super.onStop();
        camSession.deleteObserver(observer);
    }

}
