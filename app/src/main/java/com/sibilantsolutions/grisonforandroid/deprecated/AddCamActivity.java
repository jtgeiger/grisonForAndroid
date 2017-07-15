package com.sibilantsolutions.grisonforandroid.deprecated;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.sibilantsolutions.grisonforandroid.R;
import com.sibilantsolutions.grisonforandroid.domain.CamDef;

public class AddCamActivity extends Activity {

    public static final String EXTRA_CAM_DEF = "EXTRA_CAM_DEF";
    private EditText nameEditText;
    private EditText hostEditText;
    private EditText portEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cam);

        nameEditText = (EditText) findViewById(R.id.name_edittext);
        hostEditText = (EditText) findViewById(R.id.host_edittext);
        portEditText = (EditText) findViewById(R.id.port_edittext);
        usernameEditText = (EditText) findViewById(R.id.username_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
    }

    public void onClickOk(View view) {
        Intent intent = new Intent();
        CamDef camDef = new CamDef(nameEditText.getText().toString(), hostEditText.getText().toString(), Integer
                .parseInt(portEditText.getText().toString()), usernameEditText.getText().toString(), passwordEditText
                .getText().toString());
        intent.putExtra(EXTRA_CAM_DEF, camDef);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
