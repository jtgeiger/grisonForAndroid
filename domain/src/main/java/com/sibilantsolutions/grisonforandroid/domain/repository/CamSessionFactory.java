package com.sibilantsolutions.grisonforandroid.domain.repository;

import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;

/**
 * Factory interface for creating generic camera sessions.
 * <p>
 * Created by jt on 8/4/17.
 */

public interface CamSessionFactory {

    CamSession newSession(CamDef camDef);

}
