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


import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zexuan Yu
 */

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
        EditText user = (EditText)findViewById(R.id.newuser);
        EditText password = (EditText)findViewById(R.id.newpassword);
        EditText againpsw = (EditText)findViewById(R.id.againPassword);
        String strpsw = password.getText().toString();
        String stragain = againpsw.getText().toString();
        String struser = user.getText().toString();
        String response = "";
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
            try{
                response = new Register().execute(registerData.toString()).get();
                if(response.equals("used")){
                    Toast.makeText(this, "the name is already used", Toast.LENGTH_SHORT).show();
                }
                if(response.equals("success")){
                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                }
                //Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    /**
     * communication with the cloud in background,store new user and password in DB
     */
    private class Register extends AsyncTask<String, Integer, String> {
        String resultstring = "";
        public static final String url = "http://m2m-ehealth.appspot.com/register";

        @Override
        protected String doInBackground(String... params){
            try {
                String input = params[0];
                byte[]data = input.getBytes();
                HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(url).openConnection();
                httpURLConnection.setConnectTimeout(3000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(data);
                InputStream is = httpURLConnection.getInputStream();
                String line = "";
                StringBuilder total = new StringBuilder();
                //get the return from the cloud
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



