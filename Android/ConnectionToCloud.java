package com.example.administrator.saferun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2017/4/26 0026.
 */

public class ConnectionToCloud {
    String url;
    String input;
    String resultstring;

    public ConnectionToCloud(String url,String input){
        this.url = url;
        this.input = input;
    }

    public String sendToCloud(){
        byte[]data = input.getBytes();
        try{
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
        }catch (IOException e) {
            e.printStackTrace();
        }
        return resultstring;
    }
}
