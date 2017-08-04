package com.sibilantsolutions.grisonforandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import com.sibilantsolutions.grisonforandroid.data.repository.SharedPreferencesCamDefRepositoryImpl;
import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.usecase.GetCamDefUseCase;
import com.sibilantsolutions.grisonforandroid.presenter.CamViewContract;
import com.sibilantsolutions.grisonforandroid.presenter.CamViewPresenter;

public class CamViewActivity extends AppCompatActivity implements CamViewContract.View {

    private static final String EXTRA_CAM_ID = "EXTRA_CAM_ID";
    private CamViewPresenter presenter;
    private ImageView imageView;
    private Switch videoOnSwitch;

    public static Intent newIntent(Context context, int camId) {
        final Intent intent = new Intent(context, CamViewActivity.class);
        intent.putExtra(EXTRA_CAM_ID, camId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter = new CamViewPresenter(this, new GetCamDefUseCase(new SharedPreferencesCamDefRepositoryImpl(this)));

        imageView = (ImageView) findViewById(R.id.image_view);

        videoOnSwitch = (Switch) findViewById(R.id.video_on_switch);
    }

    @Override
    protected void onStart() {
        super.onStart();

        presenter.getCamDef(getIntent().getIntExtra(EXTRA_CAM_ID, Integer.MIN_VALUE));
    }

    @Override
    protected void onStop() {
        super.onStop();

        presenter.disconnect();
    }

    @Override
    @UiThread
    public void onCamDefLoaded(CamDef camDef) {
        presenter.launchCam(camDef);
    }

    @Override
    @UiThread
    public void showError() {
        Snackbar.make(imageView, "There was a problem", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    @UiThread
    public void onImageReceived(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    @UiThread
    public void setVideo(boolean isVideoOn) {
        videoOnSwitch.setChecked(isVideoOn);
    }

    @Override
    @UiThread
    public void setVideoChangeEnabled(boolean isVideoChangeEnabled) {
        videoOnSwitch.setEnabled(isVideoChangeEnabled);
    }

    @UiThread
    public void onClickVideoSwitch(View view) {
        presenter.setVideo(videoOnSwitch.isChecked());
    }

}
