package com.sibilantsolutions.grisonforandroid.domain.repository;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;

import java.util.List;

/**
 * CRUD interface for CamDefs.
 * <p>
 * Created by jt on 7/15/17.
 */

public interface CamDefRepository {

    int add(CamDef camDef);

    CamDef get(int id);

    List<CamDef> getAll();

    boolean delete(int id);

}
