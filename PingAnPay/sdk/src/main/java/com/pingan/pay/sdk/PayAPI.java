package com.pingan.pay.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by liuxianwei on 17/2/22.
 */

public class PayAPI {

    private static final String INTENT_COMPONENT_PAYACTIVITY = PingAnPayActivity.class.getName();

    //单例模式获取对象
    private static PayAPI mPayAPI;

    public PayAPI() {

    }

    public static PayAPI getInstance() {
        if(mPayAPI == null) {
            mPayAPI = new PayAPI();
        }

        return mPayAPI;
    }

    public void startPaySDKForResult(Context var1, String json, int var3,CallBack callBack) {
        if(var1 != null) {
            Intent var4;
            (var4 = new Intent()).setClassName(var1, INTENT_COMPONENT_PAYACTIVITY);
            var4.putExtra("PayRequestParcelable", json);
            PayApplication.getInstance().setCallBack(callBack);//将callBack放入全局应用中
            ((Activity)var1).startActivityForResult(var4, var3);
        }
    }

    public void startPaySDKForResult(Activity var1, String json, int var3,CallBack callBack) {
        if(var1 != null) {
            Intent var5;
            (var5 = new Intent()).setClassName(var1, INTENT_COMPONENT_PAYACTIVITY);
            var5.putExtra("PayRequestParcelable", json);
            PayApplication.getInstance().setCallBack(callBack);//将callBack放入全局应用中
            var1.startActivityForResult(var5, var3);
        }
    }

    public void startPaySDKForResult(Fragment var1, String json, int var3,CallBack callBack) {
        if(var1 != null) {
            Intent var4;
            (var4 = new Intent()).setClassName(var1.getActivity(), INTENT_COMPONENT_PAYACTIVITY);
            var4.putExtra("PayRequestParcelable", json);
            PayApplication.getInstance().setCallBack(callBack);//将callBack放入全局应用中
            var1.startActivityForResult(var4, var3);
        }
    }
}
