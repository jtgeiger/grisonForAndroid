package com.sibilantsolutions.grisonforandroid.domain;

import android.graphics.Bitmap;

import java.util.Observable;

public class CamSession extends Observable {
    private CamDef camDef;
    private CamStatus camStatus;
    private String reason = "UNKNOWN";
    private Bitmap curBitmap;

    public CamSession(CamDef camDef) {
        this.camDef = camDef;
    }

    public CamDef getCamDef() {
        return camDef;
    }

    public CamStatus getCamStatus() {
        return camStatus;
    }

    public void setCamStatus(CamStatus camStatus) {
        this.camStatus = camStatus;
        setChanged();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
        setChanged();
    }

    public Bitmap getCurBitmap() {
        return curBitmap;
    }

    public void setCurBitmap(Bitmap curBitmap) {
        this.curBitmap = curBitmap;
        setChanged();
    }

}
