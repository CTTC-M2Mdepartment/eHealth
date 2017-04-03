package com.example.administrator.saferun;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private Register register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onClickMain(View v){
        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
    }

    public void onClickRegister(View v){
        // TODO: 2017/3/29 0029  register to db
        EditText user = (EditText)findViewById(R.id.newuser);
        EditText password = (EditText)findViewById(R.id.newpassword);
        EditText againpsw = (EditText)findViewById(R.id.againPassword);
        String strpsw = password.getText().toString();
        String stragain = againpsw.getText().toString();
        String struser = user.getText().toString();
        if(strpsw.equals(stragain)==false||struser.equals("")){
            Toast.makeText(this, "no mach,try again"+strpsw+"**"+stragain, Toast.LENGTH_SHORT).show();
        }
       else{
            JSONObject registerData = new JSONObject();
            try{
                registerData.put("username",struser);
                registerData.put("password",strpsw);
            }catch (JSONException e){
                Log.d("Json constructor","Error");
                e.printStackTrace();
            }
            new Register().execute(registerData.toString());
            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }

    private class Register extends AsyncTask<String, Integer, String> {
        String resultstring = "";
        public static final String url = "http://m2m-ehealth.appspot.com/register";

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

                int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
                if(response == HttpURLConnection.HTTP_OK) {
                    //InputStream inptStream = httpURLConnection.getInputStream();
                    resultstring = "1";                     //处理服务器的响应结果
                }
                else{
                    resultstring ="0";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultstring ;
        }

    }
}



