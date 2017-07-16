package com.sibilantsolutions.grisonforandroid.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamDefRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CamDefRepository implementation that uses SharedPreferences.
 * <p>
 * Created by jt on 7/16/17.
 */

public class SharedPreferencesCamDefRepositoryImpl implements CamDefRepository {

    private static final String KEY_CAMDEF_IDS = "KEY_CAMDEF_IDS";
    private static final String KEY_CAMDEF_NEXT_ID = "KEY_CAMDEF_NEXT_ID";
    private static final String KEY_PREFIX_CAMDEF_NAME = "KEY_PREFIX_CAMDEF_NAME_";
    private static final String KEY_PREFIX_CAMDEF_HOST = "KEY_PREFIX_CAMDEF_HOST_";
    private static final String KEY_PREFIX_CAMDEF_PORT = "KEY_PREFIX_CAMDEF_PORT_";
    private static final String KEY_PREFIX_CAMDEF_USERNAME = "KEY_PREFIX_CAMDEF_USERNAME_";
    private static final String KEY_PREFIX_CAMDEF_PASSWORD = "KEY_PREFIX_CAMDEF_PASSWORD_";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesCamDefRepositoryImpl(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int add(CamDef camDef) {
        final Set<String> ids = sharedPreferences.getStringSet(KEY_CAMDEF_IDS, new HashSet<String>());
        final int id = sharedPreferences.getInt(KEY_CAMDEF_NEXT_ID, 1);
        ids.add(String.valueOf(id));
        sharedPreferences.edit()
                .putStringSet(KEY_CAMDEF_IDS, ids)
                .putInt(KEY_CAMDEF_NEXT_ID, id + 1)
                .putString(KEY_PREFIX_CAMDEF_NAME + id, camDef.getName())
                .putString(KEY_PREFIX_CAMDEF_HOST + id, camDef.getHost())
                .putInt(KEY_PREFIX_CAMDEF_PORT + id, camDef.getPort())
                .putString(KEY_PREFIX_CAMDEF_USERNAME + id, camDef.getUsername())
                .putString(KEY_PREFIX_CAMDEF_PASSWORD + id, camDef.getPassword())
                .apply();
        return id;
    }

    @Override
    public CamDef get(int id) {

        CamDef camDef = new CamDef(
                sharedPreferences.getString(KEY_PREFIX_CAMDEF_NAME + id, null),
                sharedPreferences.getString(KEY_PREFIX_CAMDEF_HOST + id, null),
                sharedPreferences.getInt(KEY_PREFIX_CAMDEF_PORT + id, -1),
                sharedPreferences.getString(KEY_PREFIX_CAMDEF_USERNAME + id, null),
                sharedPreferences.getString(KEY_PREFIX_CAMDEF_PASSWORD + id, null)
        );
        camDef.setId(id);
        return camDef;
    }

    @Override
    public List<CamDef> getAll() {

        final Set<String> ids = sharedPreferences.getStringSet(KEY_CAMDEF_IDS, new HashSet<String>());
        final List<CamDef> list = new ArrayList<>(ids.size());
        for (String idStr : ids) {
            final int id = Integer.valueOf(idStr);
            list.add(get(id));
        }

        return list;
    }

    @Override
    public boolean delete(int id) {

        final Set<String> ids = sharedPreferences.getStringSet(KEY_CAMDEF_IDS, new HashSet<String>());
        boolean removed = ids.remove(String.valueOf(id));
        sharedPreferences.edit()
                .putStringSet(KEY_CAMDEF_IDS, ids)
                .remove(KEY_PREFIX_CAMDEF_NAME + id)
                .remove(KEY_PREFIX_CAMDEF_HOST + id)
                .remove(KEY_PREFIX_CAMDEF_PORT + id)
                .remove(KEY_PREFIX_CAMDEF_USERNAME + id)
                .remove(KEY_PREFIX_CAMDEF_PASSWORD + id)
                .apply();
        return removed;
    }

}
