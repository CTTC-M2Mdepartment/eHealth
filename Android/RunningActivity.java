package com.example.administrator.saferun;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * @author Zexuan Yu
 */
public class RunningActivity extends AppCompatActivity {

    boolean update = false;
    private Current task = null;
    private LineChartView lineChart;
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisValues = new ArrayList<AxisValue>();
    String [] point = {"1","2","3","4","5","6","7","8","9","10"};

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
        startTask();
    }

    public void onClickStop(View v){
        stopTask();
    }

    /**
     *create an instance of Asynctask Current with user and date information
     */
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

    /**
     *Delete the Asynctask to prevent memory leakage
     */
    public void stopTask(){
        if (task != null) {
            update = false;
            task.cancel(true);
            task = null;
        }
    }

    /**
     * communicate with the cloud to get the current data,display in UI thread
     */
    private class Current extends AsyncTask<String, String, String> {
        String resultstring = "";
        String value = "empty";
        public static final String updateurl = "http://m2m-ehealth.appspot.com/update";
        public static final String currenturl = "http://m2m-ehealth.appspot.com/current";
        JSONObject raceid = new JSONObject();
        @Override
        protected String doInBackground(String... params){
            try {
                //get the current race id
                String input = params[0];
                String line = "";
                byte[]data = input.getBytes();
                HttpURLConnection currentRun= (HttpURLConnection)new URL(currenturl).openConnection();
                currentRun.setConnectTimeout(3000);
                currentRun.setDoInput(true);
                currentRun.setDoOutput(true);
                currentRun.setRequestMethod("POST");
                currentRun.setUseCaches(false);
                currentRun.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                currentRun.setRequestProperty("Content-Length", String.valueOf(data.length));
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
                //update the diagram every 5 seconds
                while(update){
                    HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(updateurl).openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
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
                        //TODO:warning logic
                        Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                        vib.vibrate(1000);
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
                if (in.equals("[]")){
                    TextView statusTextView = (TextView) findViewById(R.id.test);
                    statusTextView.setText("no previous running");
                }else{
                    String change = in.replace("[","");
                    change = change.replace("]","");
                    change = change.replaceAll("\"","");
                    String data[] = change.split(",");
                    int display[] = new int[data.length];
                    for(int i = 0; i < data.length; i++){
                        display[i] = Integer.parseInt(data[(data.length-1)-i].trim(),10);
                    }
                    lineChart = (LineChartView)findViewById(R.id.line_chart);
                    for (int i = 0; i < data.length; i++) {
                        mAxisValues.add(new AxisValue(i).setLabel(point[i]));
                    }
                    //clean the previous data
                    mPointValues.clear();
                    //add new data
                    for (int i = 0; i < data.length; i++) {
                        mPointValues.add(new PointValue(i, display[i]));
                    }
                    //draw line
                    Line line = new Line(mPointValues).setColor(Color.BLACK).setCubic(false);
                    List<Line> lines = new ArrayList<Line>();
                    line.setShape(ValueShape.CIRCLE);
                    line.setCubic(false);
                    line.setFilled(false);
                    line.setHasLabelsOnlyForSelected(true);
                    line.setHasLines(true);
                    line.setHasPoints(true);
                    lines.add(line);
                    LineChartData draw = new LineChartData();
                    draw.setLines(lines);
                    Axis axisX = new Axis();
                    axisX.setHasTiltedLabels(true);
                    axisX.setTextColor(Color.BLACK);
                    axisX.setName("Time");
                    axisX.setTextSize(7);
                    axisX.setMaxLabelChars(7);
                    axisX.setValues(mAxisValues);
                    draw.setAxisXBottom(axisX);
                    Axis axisY = new Axis();
                    axisY.setMaxLabelChars(7);
                    axisY.setName("pulse");
                    axisY.setTextSize(7);
                    draw.setAxisYLeft(axisY);
                    lineChart.setInteractive(true);
                    lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
                    lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
                    lineChart.setLineChartData(draw);
                    lineChart.setVisibility(View.VISIBLE);
                }
            }
        }

    }
}
