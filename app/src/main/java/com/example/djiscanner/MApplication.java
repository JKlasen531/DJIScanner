package com.example.djiscanner;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private DJIScannerApplication djiScannerApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if(djiScannerApplication == null) {
            djiScannerApplication = new DJIScannerApplication();
            djiScannerApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        djiScannerApplication.onCreate();
    }
}
