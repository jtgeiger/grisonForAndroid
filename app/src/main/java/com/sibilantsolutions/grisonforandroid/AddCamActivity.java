package com.sibilantsolutions.grisonforandroid;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.sibilantsolutions.grisonforandroid.data.repository.SharedPreferencesCamDefRepositoryImpl;
import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.usecase.AddCamDefUseCase;
import com.sibilantsolutions.grisonforandroid.presenter.AddCamContract;
import com.sibilantsolutions.grisonforandroid.presenter.AddCamPresenter;

public class AddCamActivity extends AppCompatActivity implements AddCamContract.View {

    //    public static final String EXTRA_CAM_DEF = "EXTRA_CAM_DEF";
    private EditText nameEditText;
    private EditText hostEditText;
    private EditText portEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private AddCamPresenter addCamPresenter;

    @Override
    @UiThread
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cam);

        addCamPresenter = new AddCamPresenter(this, new AddCamDefUseCase(new SharedPreferencesCamDefRepositoryImpl(this)));

        nameEditText = (EditText) findViewById(R.id.name_edittext);
        hostEditText = (EditText) findViewById(R.id.host_edittext);
        portEditText = (EditText) findViewById(R.id.port_edittext);
        usernameEditText = (EditText) findViewById(R.id.username_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
    }

    @UiThread
    public void onClickOk(View view) {
//        Intent intent = new Intent();
        CamDef camDef = new CamDef(nameEditText.getText().toString(), hostEditText.getText().toString(), Integer
                .parseInt(portEditText.getText().toString()), usernameEditText.getText().toString(), passwordEditText
                .getText().toString());
//        intent.putExtra(EXTRA_CAM_DEF, camDef);
//        setResult(RESULT_OK, intent);
//        finish();
        addCamPresenter.addCamDef(camDef);
    }

    @UiThread
    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    @UiThread
    public void showError() {
        Snackbar.make(nameEditText, "Failed to add cam", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    @UiThread
    public void returnToList() {
        setResult(RESULT_OK);
        finish();
    }
}
