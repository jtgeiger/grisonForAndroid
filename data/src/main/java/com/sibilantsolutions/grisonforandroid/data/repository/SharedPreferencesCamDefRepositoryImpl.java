package com.sibilantsolutions.grisonforandroid.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

import java.util.List;

/**
 * CamDefRepository implementation that uses SharedPreferences.
 * <p>
 * Created by jt on 7/16/17.
 */

public class SharedPreferencesCamDefRepositoryImpl implements CamDefRepository {

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesCamDefRepositoryImpl(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int add(CamDef camDef) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CamDef get(int id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<CamDef> getAll() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException("TODO");
    }

}
