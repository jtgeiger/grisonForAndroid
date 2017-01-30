package com.sibilantsolutions.grisonforandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CamViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_view);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static Intent newIntent(Context context, MainActivity.CamSession camSession) {
        return new Intent(context, CamViewActivity.class);
    }

}
