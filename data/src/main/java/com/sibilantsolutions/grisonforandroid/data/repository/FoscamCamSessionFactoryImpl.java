package com.sibilantsolutions.grisonforandroid.data.repository;

import com.sibilantsolutions.grison.driver.foscam.net.FoscamSession;
import com.sibilantsolutions.grison.evt.AlarmHandlerI;
import com.sibilantsolutions.grison.evt.AudioHandlerI;
import com.sibilantsolutions.grison.evt.ImageHandlerI;
import com.sibilantsolutions.grison.evt.LostConnectionHandlerI;
import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamSession;
import com.sibilantsolutions.grisonforandroid.domain.repository.CamSessionFactory;

import java.net.InetSocketAddress;

/**
 * Foscam specific camera session factory.
 * <p>
 * Created by jt on 8/4/17.
 */

public class FoscamCamSessionFactoryImpl implements CamSessionFactory {

    private final AudioHandlerI audioHandler;
    private final ImageHandlerI imageHandler;
    private final AlarmHandlerI alarmHandler;
    private final LostConnectionHandlerI lostConnectionHandler;

    public FoscamCamSessionFactoryImpl(AudioHandlerI audioHandler, ImageHandlerI imageHandler,
                                       AlarmHandlerI alarmHandler,
                                       LostConnectionHandlerI lostConnectionHandler) {
        this.audioHandler = audioHandler;
        this.imageHandler = imageHandler;
        this.alarmHandler = alarmHandler;
        this.lostConnectionHandler = lostConnectionHandler;
    }

    @Override
    public CamSession newSession(CamDef camDef) {
        InetSocketAddress socketAddress = new InetSocketAddress(camDef.getHost(), camDef.getPort());

        FoscamSession foscamSession = FoscamSession.connect(socketAddress,
                camDef.getUsername(), camDef.getPassword(), audioHandler, imageHandler,
                alarmHandler, lostConnectionHandler);

        return new FoscamCamSessionImpl(foscamSession);
    }

}
