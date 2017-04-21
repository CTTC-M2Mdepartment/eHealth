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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
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
    private List<PointValue> pulseValues = new ArrayList<PointValue>();
    private List<PointValue> oxygenValues = new ArrayList<PointValue>();
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
        stopTask();
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
        //String date = "2017-04-10";
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
        int minPulse;
        int maxPulse;
        int normalPulse = 0;
        int normalOxyegen = 0;
        static final String updateurl = "http://m2m-ehealth.appspot.com/update";
        static final String currenturl = "http://m2m-ehealth.appspot.com/current";
        static final String informationurl = "http://m2m-ehealth.appspot.com/warn";
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
                //get the threshold
                SharedPreferences sharedPreferences = getSharedPreferences("userLogin", 0);
                String user = sharedPreferences.getString("username", "default");
                JSONObject userinformation = new JSONObject();
                try{
                    userinformation.put("username",user);
                }catch (JSONException e){
                    Log.d("Json constructor","Error");
                    e.printStackTrace();
                }
                data = userinformation.toString().getBytes();
                HttpURLConnection threshold = (HttpURLConnection)new URL(informationurl).openConnection();
                threshold.setConnectTimeout(3000);
                threshold.setDoInput(true);
                threshold.setDoOutput(true);
                threshold.setRequestMethod("POST");
                threshold.setUseCaches(false);
                threshold.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                threshold.setRequestProperty("Content-Length", String.valueOf(data.length));
                outputStream = threshold.getOutputStream();
                outputStream.write(data);
                InputStream read = threshold.getInputStream();
                StringBuilder maxmin = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(read));
                try {
                    while ((line = br.readLine()) != null) {
                            maxmin.append(line);
                    }
                } catch (IOException e) {
                        e.printStackTrace();
                }
                value = maxmin.toString();
                String handle = value.replace("[","");
                handle = handle.replace("]","");
                String[] warning = handle.split(",");
                minPulse = Integer.parseInt(warning[0].trim());
                maxPulse = Integer.parseInt(warning[1].trim());
                //update the diagram every 5 seconds
                data = raceid.toString().getBytes();
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
                    //check user's condition
                    normalPulse = 0;
                    normalOxyegen = 0;
                    for(int i = 0; i < data.length/2; i++){
                        if(display[i]<minPulse||display[i]>maxPulse){
                            normalPulse++;
                        }
                    }
                    for(int i = data.length/2;i<data.length;i++){
                        if(display[i]<94){
                            normalOxyegen++;
                        }
                    }
                    if(normalPulse >= 5|| normalOxyegen>=5){
                        Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                        vib.vibrate(1000);
                    }
                    lineChart = (LineChartView)findViewById(R.id.line_chart);
                    for (int i = 0; i < data.length/2; i++) {
                        mAxisValues.add(new AxisValue(i).setLabel(point[i]));
                    }
                    //clean the previous data
                    pulseValues.clear();
                    oxygenValues.clear();
                    //add new data
                    for (int i = 0; i < data.length/2; i++) {
                        oxygenValues.add(new PointValue(i, display[i]));
                    }
                    for (int i = data.length/2; i < data.length; i++) {
                        pulseValues.add(new PointValue((i-data.length/2),display[i]));
                    }
                    //draw line
                    Line line = new Line(pulseValues).setColor(Color.BLACK).setCubic(false);
                    Line line2 =  new Line(oxygenValues).setColor(Color.BLUE).setCubic(false);
                    List<Line> lines = new ArrayList<Line>();
                    line.setShape(ValueShape.CIRCLE);
                    line.setCubic(false);
                    line.setFilled(false);
                    line.setHasLabelsOnlyForSelected(true);
                    line.setHasLines(true);
                    line.setHasPoints(true);
                    lines.add(line);
                    line2.setShape(ValueShape.CIRCLE);
                    line2.setCubic(false);
                    line2.setFilled(false);
                    line2.setHasLabelsOnlyForSelected(true);
                    line2.setHasLines(true);
                    line2.setHasPoints(true);
                    lines.add(line2);
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
           // TextView statusTextView = (TextView) findViewById(R.id.test);
            //statusTextView.setText(in);
        }


    }
}
