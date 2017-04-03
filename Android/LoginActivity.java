package com.example.administrator.saferun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClickToRegister(View v){
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }

    public void onClickVerifiedLogin(View v){
        String verify = "";
        EditText user = (EditText)findViewById(R.id.username);
        EditText password = (EditText)findViewById(R.id.password);
        String strpsw = password.getText().toString();
        String struser = user.getText().toString();
        JSONObject registerData = new JSONObject();
        try{
            registerData.put("username",struser);
            registerData.put("password",strpsw);
        }catch (JSONException e){
            Log.d("Json constructor","Error");
            e.printStackTrace();
        }
        try{
            verify = new Login().execute(registerData.toString()).get();
            if(verify.equals("")){
                Toast.makeText(this, "no such user", Toast.LENGTH_SHORT).show();
            }
            if(verify.equals("wrong")){
                Toast.makeText(this, "wrong password,please try again", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        // TODO: 2017/3/29 0029  verify the password from cloud
        if(verify.equals("correct")){
            //Toast.makeText(this, verify, Toast.LENGTH_SHORT).show();
            SharedPreferences mSharedPreferences = getSharedPreferences("userLogin", 0);
            mSharedPreferences.getString("username", "default");
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            mEditor.putString("username", struser);
            mEditor.commit();
            Intent intent = new Intent(LoginActivity.this,ChooseFunctionActivity.class);
            startActivity(intent);
        }
    }

    private class Login extends AsyncTask<String, Integer, String> {
        String resultstring = "";
        public static final String url = "http://m2m-ehealth.appspot.com/login";

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

    }
}
