package com.pingan.pay.sdk;

import android.app.Application;

/**
 * Created by liuxianwei on 17/2/23.
 */

public class PayApplication extends Application {


    private static PayApplication instance;

    private CallBack callBack;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static PayApplication getInstance(){
        return instance;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public CallBack getCallBack() {
        return callBack;
    }

}
