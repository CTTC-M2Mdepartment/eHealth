package com.example.administrator.saferun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    Button click;
    Spinner gender;
    Spinner year;
    Spinner month;
    Spinner day;
    Spinner rate;
    ArrayList<String> dataGender = new ArrayList<String>();
    ArrayList<String> dataYear = new ArrayList<String>();
    ArrayList<String> dataMonth = new ArrayList<String>();
    ArrayList<String> dataDay = new ArrayList<String>();
    ArrayList<String> dataRate = new ArrayList<String>();
    ArrayAdapter<String> adapterGender;
    ArrayAdapter<String> adapterYear;
    ArrayAdapter<String> adapterMonth;
    ArrayAdapter<String> adapterDay;
    ArrayAdapter<String> adapterRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        click  =(Button)super.findViewById(R.id.button);
        click.setOnClickListener(new OnClickListenerImpl());
        gender = (Spinner)findViewById(R.id.gender);
        year = (Spinner)findViewById(R.id.year);
        month = (Spinner)findViewById(R.id.month);
        day = (Spinner)findViewById(R.id.day);
        rate = (Spinner)findViewById(R.id.restRate);
        //spinner for gender
        dataGender.add("Male");
        dataGender.add("Female");
        adapterGender = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, dataGender);
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapterGender);
        //spinner for heart rate
        for(int i=60;i<90;i++){
            dataRate.add(""+i);
        }
        adapterRate =  new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, dataRate);
        adapterRate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rate.setAdapter(adapterRate);
        //spinner for age
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 70; i++) {
            dataYear.add("" + (cal.get(Calendar.YEAR) - 70 + i));
        }
        adapterYear = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, dataYear);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(adapterYear);
        year.setSelection(69);//set default
        for(int i=1;i<13;i++){
            if(i<10){
                dataMonth.add("0"+i);
            }else{
                dataMonth.add(""+i);
            }
        }
        adapterMonth = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataMonth);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        month.setAdapter(adapterMonth);

        adapterDay = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, dataDay);
        adapterDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day.setAdapter(adapterDay);
        month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                dataDay.clear();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, Integer.valueOf(year.getSelectedItem().toString()));
                cal.set(Calendar.MONTH, arg2);
                int dayofmonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                for (int i = 1; i <= dayofmonth; i++) {
                    if(i<10){
                        dataDay.add("0"+i);
                    }else {
                        dataDay.add("" + i);
                    }
                }
                adapterDay.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });




    }
    public void onClickChooseFunction(View v){
        //TODO:get all setting of the user(AsyncTask)
        Intent intent = new Intent(SettingActivity.this,ChooseFunctionActivity.class);
        startActivity(intent);
    }
    private class OnClickListenerImpl implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            SharedPreferences mSharedPreferences = getSharedPreferences("userLogin", 0);
            String userlogin = mSharedPreferences.getString("username", "default");
            String getGender = gender.getSelectedItem().toString();
            String getYear = year.getSelectedItem().toString();
            String getMonth = month.getSelectedItem().toString();
            String getDay = day.getSelectedItem().toString();
            String getRate = rate.getSelectedItem().toString();
            String birthday = getYear+"-"+getMonth+"-"+getDay;
            //TODO:update to the cloud
            String response = "";
            JSONObject change = new JSONObject();
            try{
                change.put("username",userlogin);
                change.put("gender",getGender);
                change.put("birthday",birthday);
                change.put("rate",getRate);
            }catch (JSONException e){
                Log.d("Json constructor","Error");
                e.printStackTrace();
            }
            try{
               response = new ChangeSetting().execute(change.toString()).get();
            }catch (Exception e){
                e.printStackTrace();
            }
            if(response.equals("ok")){
                Intent intent = new Intent(SettingActivity.this,ChooseFunctionActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(SettingActivity.this, "error occurs,please try again", Toast.LENGTH_LONG).show();
            }
            //TextView t = (TextView)findViewById(R.id.textView);
            //t.setText(year);
        }

    }

    private class ChangeSetting extends AsyncTask<String, Integer, String> {
        String resultstring = "";
        static final String url = "http://m2m-ehealth.appspot.com/changeSetting";

        @Override
        protected String doInBackground(String... params){
           // try {
                String input = params[0];
                ConnectionToCloud send = new ConnectionToCloud(url,input);
                resultstring = send.sendToCloud();
                return resultstring;
                /*byte[]data = input.getBytes();
                //http request
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
                returnString = total.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return returnString ;*/
        }
    }
}
