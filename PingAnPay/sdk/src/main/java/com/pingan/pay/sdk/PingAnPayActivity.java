package com.pingan.pay.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.pingan.pay.sdk.util.ConstClass;
import com.pingan.pay.sdk.util.OrderInfoUtil2_0;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PingAnPayActivity extends Activity {

    private WebView html_view;

    public static final int WEIXIN = 1;
    public static final int ALIPAY = 2;

    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;

    private IWXAPI api;

    private CallBack callBack;

    private String amount;

    private String productName;

    private String oneAccount;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WEIXIN:

                    Toast.makeText(PingAnPayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
                    // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
//                    api.sendReq(req);

                    PayReq request = new PayReq();
                    request.appId = "wxd930ea5d5a258f4f";
                    request.partnerId = "1900000109";
                    request.prepayId= "1101000000140415649af9fc314aa427";
                    request.packageValue = "Sign=WXPay";
                    request.nonceStr= "1101000000140429eb40476f8896f4c9";
                    request.timeStamp= "1398746574";
                    request.sign= "7FFECB600D7157C5AA49810D2D8F28BC2811827B";
                    boolean bl = api.sendReq(request);

                    break;
                case ALIPAY:

                    Log.i("ALIPAY","msg："+msg.obj);
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    Log.i("ALIPAY","payResult："+payResult.toString());
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    Log.i("ALIPAY","resultInfo："+resultInfo);
                    String resultStatus = payResult.getResultStatus();
                    Log.i("ALIPAY","resultStatus："+resultStatus);
                    // 判断resultStatus 为9000则代表支付成功

                    Intent intent = new Intent();

                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(PingAnPayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                        intent.putExtra("payResult","success");
                        callBack.success("success");
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(PingAnPayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                        intent.putExtra("payResult","fail");
                        callBack.fail("fail");
                    }

                    setResult(ConstClass.RESULT_CODE_ALIPAY,intent);

                    finish();

                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        setContentView(R.layout.pay_main);

        //初始化
        init();

    }

    private void init(){

        api = WXAPIFactory.createWXAPI(this, null);
        // 将该app注册到微信
        api.registerApp("wxd930ea5d5a258f4f");

        Intent intent = getIntent();
        //前端传入的json格式的参数
        String jsonStr = intent.getStringExtra("PayRequestParcelable");
        Log.i("PayRequestParcelable==","jsonStr："+jsonStr);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
            Log.i("PayRequestParcelable==","jsonObject："+jsonObject.toString());
            if (jsonObject != null){
                this.amount = jsonObject.getString("amount");
                this.productName = jsonObject.getString("productName");
                this.oneAccount = jsonObject.getString("mobileNo");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callBack = PayApplication.getInstance().getCallBack();

        JavaScriptInterface JSInterface= new JavaScriptInterface(this);

        html_view = (WebView) findViewById(R.id.html_view);

        html_view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        html_view.getSettings().setJavaScriptEnabled(true);

        html_view.getSettings().setSupportZoom(true);

        html_view.getSettings().setBuiltInZoomControls(false);

        html_view.addJavascriptInterface(JSInterface, "JSBridge");
        html_view.loadUrl("https://test-api-syt-pama.pingan.com.cn/pama_cashier_web/index.html");
//        html_view.loadUrl("file:///android_asset/index.html");
        html_view.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
    }

    /**
     * html与安卓原生的连接桥
     */
    public class JavaScriptInterface {
        Context mContext;
        JavaScriptInterface(Context context) {
            mContext = context;
        }

        //调用js代码
        @JavascriptInterface
        public void callCashier(String info) {

            JSONObject object = null;
            String payType = "";
            String amount ="";
            String productName ="";
            String signData = "";
            try {
                object = new JSONObject(info);

                payType = object.getString("payType");
                signData = object.getString("sign_Data");
                signData = signData.replace("&#39;","&");
                Log.i("callCashier","payType："+payType+"--amount："+amount+"--signData："+signData+"--productName："+productName);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            //微信支付
            if(ConstClass.PAYTYPE_WECHAT.equals(payType)){
//
                payWeChat();

            }
            //支付宝支付
            if(ConstClass.PAYTYPE_ALI.equals(payType)){
                //设置当前环境为沙箱环境
//                EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);

                payV2(signData);

            }

        }
        //提供给js调用
        @JavascriptInterface
        public void showAndroidData(){
            final String amount = PingAnPayActivity.this.amount;
            final String productName = PingAnPayActivity.this.productName;
            final String oneAccount = PingAnPayActivity.this.oneAccount;
            PingAnPayActivity.this.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    html_view.loadUrl("javascript:getProductInfo('" + amount + "','" + productName +"','" + oneAccount + "')");
                }
            });

        }
    }


    /**
     * 支付宝支付业务
     *
     */
    public void payV2(final  String signData) {
//        if (TextUtils.isEmpty(ConstClass.APPID_ALI) || (TextUtils.isEmpty(ConstClass.RSA2_PRIVATE) && TextUtils.isEmpty(ConstClass.RSA_PRIVATE))) {
//            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
//                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialoginterface, int i) {
//                            //
//                            finish();
//                        }
//                    }).show();
//            return;
//        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
//        boolean rsa2 = (ConstClass.RSA2_PRIVATE.length() > 0);
//        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(ConstClass.APPID_ALI,money, rsa2);
//        Log.i("msp3", params.toString());
//        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
//        Log.i("msp4", orderParam);
//        String privateKey = rsa2 ? ConstClass.RSA2_PRIVATE : ConstClass.RSA_PRIVATE;
//        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
//        final String orderInfo = orderParam + "&" + sign;
//        Log.i("msp5", orderInfo);
        Log.i("ALIPAY","signData："+signData);
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(PingAnPayActivity.this);
                Map<String, String> result = alipay.payV2(signData, true);
                Log.i("msp2", signData.toString());

                Message msg = new Message();
                msg.what = ALIPAY;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 支付宝账户授权业务
     *
     * @param v
     */
    public void authV2(View v) {
        if (TextUtils.isEmpty(ConstClass.PID_ALI) || TextUtils.isEmpty(ConstClass.APPID_ALI)
                || (TextUtils.isEmpty(ConstClass.RSA2_PRIVATE) && TextUtils.isEmpty(ConstClass.RSA_PRIVATE))
                || TextUtils.isEmpty(ConstClass.TARGET_ID_ALI)) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER |APP_ID| RSA_PRIVATE| TARGET_ID")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                        }
                    }).show();
            return;
        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * authInfo的获取必须来自服务端；
         */
        boolean rsa2 = (ConstClass.RSA2_PRIVATE.length() > 0);
        Map<String, String> authInfoMap = OrderInfoUtil2_0.buildAuthInfoMap(ConstClass.PID_ALI, ConstClass.APPID_ALI, ConstClass.TARGET_ID_ALI, rsa2);
        String info = OrderInfoUtil2_0.buildOrderParam(authInfoMap);

        String privateKey = rsa2 ? ConstClass.RSA2_PRIVATE : ConstClass.RSA_PRIVATE;
        String sign = OrderInfoUtil2_0.getSign(authInfoMap, privateKey, rsa2);
        final String authInfo = info + "&" + sign + "&" + getSignType();
        Runnable authRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造AuthTask 对象
                AuthTask authTask = new AuthTask(PingAnPayActivity.this);
                // 调用授权接口，获取授权结果
                Map<String, String> result = authTask.authV2(authInfo, true);

                Message msg = new Message();
                msg.what = SDK_AUTH_FLAG;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread authThread = new Thread(authRunnable);
        authThread.start();
    }

    /**
     * get the sdk version. 获取SDK版本号
     *
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(this);
        String version = payTask.getVersion();
        Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA2\"";
    }


    /**
     * 微信支付业务
     */
    private void payWeChat() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//			byte[] buf = Util.httpPost(url, entity);
//			Message msg = Message.obtain();
//			msg.what = NET;
//			msg.obj = buf;
//			handler.sendMessage(msg);
//			}
//		}).start();//这个start()方法不要忘记了
        Message msg = Message.obtain();
        msg.what = WEIXIN;
//		msg.obj = buf;
        handler.sendMessage(msg);

    }

    /**
     *  ALIPAY
     *  获取后台服务加签数据
     */
    private void getSignDataForAli(){

    }

    /**
     *  WECHAT
     *  获取后台服务加签数据
     */
    private void getSignDataForWeChat(){

    }

}
