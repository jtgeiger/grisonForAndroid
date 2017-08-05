package com.sibilantsolutions.grisonforandroid.data.repository;

import com.sibilantsolutions.grison.driver.foscam.net.FoscamSession;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamSession;

/**
 * Foscam specific camera session implementation.
 * <p>
 * Created by jt on 8/4/17.
 */

public class FoscamCamSessionImpl implements CamSession {

    private final FoscamSession foscamSession;

    public FoscamCamSessionImpl(FoscamSession foscamSession) {
        this.foscamSession = foscamSession;
    }

    @Override
    public boolean startVideo() {
        return foscamSession.videoStart();
    }

    @Override
    public boolean stopVideo() {
        foscamSession.videoEnd();
        return true;
    }

    @Override
    public boolean startAudio() {
        return foscamSession.audioStart();
    }

    @Override
    public boolean stopAudio() {
        foscamSession.audioEnd();
        return true;
    }

    @Override
    public void disconnect() {
        foscamSession.disconnect();
    }

}
