package com.example.administrator.saferun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PreviousDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_data);
        SharedPreferences mSharedPreferences = getSharedPreferences("userLogin", 0);
        String userlogin = mSharedPreferences.getString("username", "default");
        JSONObject registerData = new JSONObject();
        try{
            registerData.put("userlogin",userlogin);
        }catch (JSONException e){
            Log.d("Json constructor","Error");
            e.printStackTrace();
        }
        new Previous().execute(registerData.toString());
    }

    public void onClickBackToChoose(View V){
        Intent intent = new Intent(PreviousDataActivity.this,ChooseFunctionActivity.class);
        startActivity(intent);
    }

    private class Previous extends AsyncTask<String, Integer, String> {
        String resultstring = "";
        public static final String url = "http://m2m-ehealth.appspot.com/previous";

        @Override
        protected String doInBackground(String... params){
            try {
                String input = params[0];
                byte[]data = input.getBytes();
                HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(url).openConnection();
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
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(data);
                InputStream is = httpURLConnection.getInputStream();
                String line = "";
                StringBuilder total = new StringBuilder();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                try {
                    while ((line = rd.readLine()) != null) {
                        total.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                resultstring = total.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultstring ;
        }

        protected void onPostExecute(String response){
            super.onPostExecute(response);
            // parsing the response
            if (response != null) {
                TextView statusTextView = (TextView) findViewById(R.id.userdata);
                statusTextView.setText(response);
            }else{
                TextView statusTextView = (TextView) findViewById(R.id.userdata);
                statusTextView.setText("no previous running");
            }


        }

    }







}
