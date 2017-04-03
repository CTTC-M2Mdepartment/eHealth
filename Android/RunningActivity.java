package com.example.administrator.saferun;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RunningActivity extends AppCompatActivity {

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
    }
}
