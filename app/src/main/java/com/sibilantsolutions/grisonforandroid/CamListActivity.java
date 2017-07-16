package com.sibilantsolutions.grisonforandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.sibilantsolutions.grisonforandroid.data.repository.SharedPreferencesCamDefRepositoryImpl;
import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.usecase.GetAllCamDefsUseCase;
import com.sibilantsolutions.grisonforandroid.presenter.CamListContract;
import com.sibilantsolutions.grisonforandroid.presenter.CamListPresenter;

import java.util.List;

public class CamListActivity extends AppCompatActivity implements CamListContract.View {

    private TextView textView;

    private CamListPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter = new CamListPresenter(this, new GetAllCamDefsUseCase(new SharedPreferencesCamDefRepositoryImpl(this)));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CamListActivity.this, AddCamActivity.class));
            }
        });

        textView = (TextView) findViewById(R.id.text_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.getAllCamDefs();
    }

    @Override
    public void showAllCamDefs(List<CamDef> camDefs) {
        textView.setText(camDefs.toString());
    }

    @Override
    public void showError() {
        Snackbar.make(textView, "Trouble getting cams", Snackbar.LENGTH_SHORT).show();
    }

}
