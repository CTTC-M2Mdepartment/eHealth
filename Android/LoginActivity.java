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

/**
 * @author Zexuan Yu
 */

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

    /**
     *
     * @param v
     * Send the username and password to the cloud and get verification from the cloud
     */

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
        if(verify.equals("correct")){
            SharedPreferences mSharedPreferences = getSharedPreferences("userLogin", 0);
            mSharedPreferences.getString("username", "default");
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            mEditor.putString("username", struser);
            mEditor.commit();
            Intent intent = new Intent(LoginActivity.this,ChooseFunctionActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Communicate with the cloud in background,return verification
     */
    private class Login extends AsyncTask<String, Integer, String> {
        String resultstring = "";
        static final String url = "http://m2m-ehealth.appspot.com/login";

        /**
         *
         * @param username and password
         * @return verify
         */

        @Override
        protected String doInBackground(String... params){
            String input = params[0];
            ConnectionToCloud send = new ConnectionToCloud(url,input);
            resultstring = send.sendToCloud();
            return resultstring;
        }

    }
}
