package com.sibilantsolutions.grisonforandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddCamActivity extends Activity {

    public static final String EXTRA_NAME = "NAME";
    public static final String EXTRA_HOST = "HOST";
    public static final String EXTRA_PORT = "PORT";
    public static final String EXTRA_USERNAME = "USERNAME";
    public static final String EXTRA_PASSWORD = "PASSWORD";

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
        intent.putExtra(EXTRA_NAME, nameEditText.getText().toString());
        intent.putExtra(EXTRA_HOST, hostEditText.getText().toString());
        intent.putExtra(EXTRA_PORT, Integer.parseInt(portEditText.getText().toString()));
        intent.putExtra(EXTRA_USERNAME, usernameEditText.getText().toString());
        intent.putExtra(EXTRA_PASSWORD, passwordEditText.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
