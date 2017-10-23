package com.example.hanfeng.phonsafe.acticity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.example.hanfeng.phonsafe.R;
import com.example.hanfeng.phonsafe.util.StreamUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    public static final String tag = "SplashActivity";

    //更新新版本状态码
    private static  final int UPDATE_VERSION=100;
    //进入主页面的状态码
    private static  final int ENTER_HOME = 101;
    //异常
    private static  final int URL_ERROR = 102;
    private static  final int IO_ERROR = 103;
    private static  final int JSON_ERROR = 104;


    private TextView textView;
    private TextView tv_version_name;

    private int mLocalVersionCode;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
                switch (msg.what){
                    case UPDATE_VERSION:
                        //弹出对话框,更新版本
                        break;
                    case ENTER_HOME:
                        //进入应用程序主界面
                        enterHome();
                        break;
                    case URL_ERROR:
                        break;
                    case IO_ERROR:
                        break;
                    case JSON_ERROR:
                        break;

                }
        }
    };

    /**
     * 进入应用程序主界面
     */
    private void enterHome() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);

        //开启新页面后,将导航页面关闭
        finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1.去除title栏(该方法失效,原因是:当前activity继承自appCompatActivity )
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //2.隐藏titleBar生效
//        getSupportActionBar().hide();
        //3.在mainfest中修改主题
        setContentView(R.layout.activity_main);
        //初始化UI
        initView();
        //初始化数据
        initData();
        
        
    }

    /**
     * 初始化UI
     *
     */
    private void initView() {
        tv_version_name = (TextView) findViewById(R.id.tv_version);
    }

    /**
     * 初始化数据
     */
    private void initData() {

        //1.获取版本号
       tv_version_name.setText("版本名称:"+getVersionName());
        //2.检测版本是否有更新,有则下载(比对版本号)
        mLocalVersionCode = getVersionCode();

        //3.网络访问,获取新版本号
        checkVersionCode();

    }

    /**
     * 获取网络版本号
     *
     */
    private void checkVersionCode() {

        new Thread(){
            @Override
            public void run() {
                //电脑ip会在不同的网络下,重新分配不同的(?)
//                    new Message(); 效率略低,用下一个
                Message message = Message.obtain();
                long startTime = System.currentTimeMillis();
                try {
                    //封装url
                    URL url = new URL("http://192.168.56.1:8080/json.json");
                    //开启链接
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    //设置常见请求参数(请求头)
                    urlConnection.setConnectTimeout(2000);//请求超时
                    urlConnection.setReadTimeout(2000);//读取超时

                    //默认请求方式就为get
                    urlConnection.setRequestMethod("GET");

                    //获取响应码
                    if(urlConnection.getResponseCode()==200){
                        InputStream is = urlConnection.getInputStream();
                        //流对象转换为字符串
                        String json = StreamUtil.streamToString(is);
                        Log.i(tag,json);

                        //解析json

                            JSONObject jsonObject = new JSONObject(json);


                            String versionName = jsonObject.getString("versionName");
                            String versionDes = jsonObject.getString("versionDes");
                            String versionCode = jsonObject.getString("versionCode");
                            String downloadUrl = jsonObject.getString("downloadUrl");
                            Log.i(tag,versionName);
                            Log.i(tag,versionDes);
                            Log.i(tag,versionCode);
                            Log.i(tag,downloadUrl);

                            if(mLocalVersionCode<Integer.parseInt(versionCode)){
                                //提示用户更新,弹出对话框
                                message.what = UPDATE_VERSION;
                            }else{
                                //进入程序主界面
                                message.what = ENTER_HOME;
                            }


                    }

                } catch (MalformedURLException e) {
                    //url异常
                    message.what = URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    //io异常
                    message.what = IO_ERROR;
                    e.printStackTrace();
                }catch (JSONException e) {
                    message.what = JSON_ERROR;
                    e.printStackTrace();
                }finally {
                    long endTime = System.currentTimeMillis();
                    if(endTime-startTime<4000){
                        try {
                            Thread.sleep(4000-(endTime-startTime));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(message);
                }
            }
        }.start();

//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//
//            }
//        }).start();
    }

    /**
     * 获取本地的版本好号
     *
     * @return 返回版本号,非0代表成功
     * */
    private int getVersionCode() {
        PackageManager manager = getPackageManager();

        //获取指定包名的基础信息,传0代表获取基础信息
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            Log.i(tag, "当前版本:"+String.valueOf(info.versionCode));
            return info.versionCode;


        } catch (PackageManager.NameNotFoundException e) {
            //包名不存在问题
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取版本号,清单文件中的
     * @return 应用版本名称,返回null,代表有异常
     */
    private String getVersionName() {
        PackageManager manager = getPackageManager();

        //获取指定包名的基础信息,传0代表获取基础信息
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;


        } catch (PackageManager.NameNotFoundException e) {
            //包名不存在问题
            e.printStackTrace();
        }
        return null;
    }


}
