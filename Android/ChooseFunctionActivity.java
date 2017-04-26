package com.example.administrator.saferun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * @author Zexuan Yu
 */

public class ChooseFunctionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_function);
        TextView name = (TextView)findViewById(R.id.loginuser);
        SharedPreferences mSharedPreferences = getSharedPreferences("userLogin", 0);
        String userlogin = mSharedPreferences.getString("username", "default");
        name.setText(userlogin);
    }

    public void onClickLogOut(View v){
        SharedPreferences mSharedPreferences = getSharedPreferences("userLogin", 0);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
        Intent intent = new Intent(ChooseFunctionActivity.this,MainActivity.class);
        startActivity(intent);
    }

    public void onClickPrevious(View v){
        Intent intent = new Intent(ChooseFunctionActivity.this,PreviousDataActivity.class);
        startActivity(intent);
    }
    public void onClickRunning(View v){
        Intent intent = new Intent(ChooseFunctionActivity.this,RunningActivity.class);
        startActivity(intent);
    }
    public void onClickSetting(View v){
        Intent intent = new Intent(ChooseFunctionActivity.this,SettingActivity.class);
        startActivity(intent);
    }
}
