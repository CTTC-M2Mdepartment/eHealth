package com.example.administrator.saferun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * @author Zexuan Yu
 */

public class PreviousDataActivity extends AppCompatActivity {

    final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    final int MP = ViewGroup.LayoutParams.MATCH_PARENT;

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
        try{
            String response = new Previous().execute(registerData.toString()).get();
            if (response != null) {
                if (response.equals("[]")) {
                    TextView statusTextView = (TextView) findViewById(R.id.userdata);
                    statusTextView.setText("no previous running");
                } else {
                    //draw table
                    TableLayout tableLayout = (TableLayout) findViewById(R.id.table1);
                    tableLayout.setStretchAllColumns(true);
                    //unpack json data
                    response = response.replace("[","");
                    response = response.replace("]","");
                    response = response.replaceAll("\"","");
                    String rarry[] =response.split(",");
                    int i=0;
                    //table header
                    TableRow tableHead = new TableRow(this);
                    TextView tv1 = new TextView(this);
                    tv1.setText("Race");
                    tableHead.addView(tv1);
                    TextView tv2 = new TextView(this);
                    tv2.setText("Max");
                    tableHead.addView(tv2);
                    TextView tv3 = new TextView(this);
                    tv3.setText("Min");
                    tableHead.addView(tv3);
                    TextView tv4 = new TextView(this);
                    tv4.setText("Average");
                    tableHead.addView(tv4);
                    tableLayout.addView(tableHead, new TableLayout.LayoutParams(MP, WC));
                    for (int row = 0; row < (rarry.length/4); row++) {
                        TableRow tableRow = new TableRow(this);
                        for (int col = 0; col < 4; col++) {
                            TextView tv = new TextView(this);
                            tv.setText(rarry[i].replace(" ",""));
                            tableRow.addView(tv);
                            i++;
                        }
                        tableLayout.addView(tableRow, new TableLayout.LayoutParams(MP, WC));
                    }
                }
            }
        }catch(Exception e){
            Toast.makeText(this, "system error", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickBackToChoose(View V){
        Intent intent = new Intent(PreviousDataActivity.this,ChooseFunctionActivity.class);
        startActivity(intent);
    }

    /**
     * communication with the cloud in background,return max,min,average value for each run
     */
    private class Previous extends AsyncTask<String, Integer, String> {
        String resultstring = "";
        public static final String url = "http://m2m-ehealth.appspot.com/previous";

        @Override
        protected String doInBackground(String... params){
            try {
                String input = params[0];
                byte[]data = input.getBytes();
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
                //get the return from the cloud
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
