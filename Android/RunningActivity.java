package com.example.administrator.saferun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RunningActivity extends AppCompatActivity {

    boolean update = false;
    private Current task = null;
    int i=0;

    @Override
    protected void onDestroy() {
        stopTask();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
    }

    public void onClickBackToChoose(View v){
        Intent intent = new Intent(RunningActivity.this,ChooseFunctionActivity.class);
        startActivity(intent);
    }

    public void onClickStartRunning(View v){
        // TODO: 2017/3/29 0029  create start running activity and enter
        startTask();
    }

    public void onClickStop(View v){
        stopTask();
    }

    public void startTask(){
        stopTask();
        SharedPreferences mSharedPreferences = getSharedPreferences("userLogin", 0);
        String userlogin = mSharedPreferences.getString("username", "default");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = sDateFormat.format(new java.util.Date());
        JSONObject registerData = new JSONObject();
        try{
            registerData.put("userlogin",userlogin);
            registerData.put("date",date);
        }catch (JSONException e){
            Log.d("Json constructor","Error");
            e.printStackTrace();
        }
        update = true;
        task = (Current) new Current().execute(registerData.toString());
    }

    public void stopTask(){
        if (task != null) {
            update = false;
            task.cancel(true);
            task = null;
        }
    }

    private class Current extends AsyncTask<String, String, String> {
        String resultstring = "";
        String value = "empty";
        public static final String updateurl = "http://m2m-ehealth.appspot.com/update";
        public static final String currenturl = "http://m2m-ehealth.appspot.com/current";
        JSONObject raceid = new JSONObject();
        @Override
        protected String doInBackground(String... params){
            try {
                String input = params[0];
                String line = "";
                byte[]data = input.getBytes();
                HttpURLConnection currentRun= (HttpURLConnection)new URL(currenturl).openConnection();
                currentRun.setConnectTimeout(3000);           //设置连接超时时间
                currentRun.setDoInput(true);                  //打开输入流，以便从服务器获取数据
                currentRun.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
                currentRun.setRequestMethod("POST");          //设置以Post方式提交数据
                currentRun.setUseCaches(false);               //使用Post方式不能使用缓存
                //设置请求体的类型是文本类型
                currentRun.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //设置请求体的长度
                currentRun.setRequestProperty("Content-Length", String.valueOf(data.length));
                //获得输出流，向服务器写入数据
                OutputStream outputStream = currentRun.getOutputStream();
                outputStream.write(data);
                InputStream runnumber = currentRun.getInputStream();
                StringBuilder race  =new StringBuilder();
                BufferedReader bf = new BufferedReader(new InputStreamReader(runnumber));
                try {
                    while ((line = bf.readLine()) != null) {
                        race.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                value = race.toString();
                try{
                    raceid.put("runnumber",value);
                }catch (JSONException e){
                    Log.d("Json constructor","Error");
                    e.printStackTrace();
                }
                data = raceid.toString().getBytes();
                while(update){
                    HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(updateurl).openConnection();
                    httpURLConnection.setConnectTimeout(3000);           //设置连接超时时间
                    httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
                    httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
                    httpURLConnection.setRequestMethod("POST");          //设置以Post方式提交数据
                    httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
                    //设置请求体的类型是文本类型
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    //设置请求体的长度
                    httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
                    //获得输出流，向服务器写入数据
                    outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(data);
                    InputStream is = httpURLConnection.getInputStream();
                    StringBuilder total = new StringBuilder();
                    try{
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        try {
                            while ((line = rd.readLine()) != null) {
                                total.append(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        value = total.toString();
                        publishProgress(value);
                        Thread.sleep(5000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultstring ;
        }

        @Override
        protected void onProgressUpdate(String... params){
            String in = params[0];
            if (in != null) {
                TextView statusTextView = (TextView) findViewById(R.id.test);
                statusTextView.setText(in+i);
                i++;
            }else{
                TextView statusTextView = (TextView) findViewById(R.id.test);
                statusTextView.setText("no previous running");
            }
        }

    }
}
