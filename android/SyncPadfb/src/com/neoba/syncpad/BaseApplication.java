package com.neoba.syncpad;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import android.app.Application;

public class BaseApplication extends Application{
	 
    private static Bus mEventBus;
 
    public static Bus getEventBus() {
        return mEventBus;
    }
 
    @Override
    public void onCreate() {
        super.onCreate();
 
        mEventBus = new Bus(ThreadEnforcer.ANY);
    }
}