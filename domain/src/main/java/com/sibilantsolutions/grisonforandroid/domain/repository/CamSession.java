package com.sibilantsolutions.grisonforandroid.domain.repository;

/**
 * Generic camera session domain object interface.
 * <p>
 * Created by jt on 8/4/17.
 */

public interface CamSession {

    boolean startVideo();

    boolean stopVideo();

    boolean startAudio();

    boolean stopAudio();

    void disconnect();

}
