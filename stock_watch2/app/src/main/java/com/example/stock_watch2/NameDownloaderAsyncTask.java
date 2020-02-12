package com.example.stock_watch2;


import android.annotation.SuppressLint;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import java.util.HashMap;


public class NameDownloaderAsyncTask extends AsyncTask<String, Void, String> {
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    private static final String DATA_URL =
            "https://api.iextrading.com/1.0/ref-data/symbols";

    private static final String TAG = "NameDownloaderAsyncTask";
    private HashMap<String, String> tempData;

    NameDownloaderAsyncTask(MainActivity ma) {
        mainActivity = ma;
    }



    @Override
    protected void onPostExecute(String s) {
        HashMap<String, String> tempData2= parseJSON(s);

        mainActivity.updateData(tempData2);
    }


    @Override
    protected String doInBackground(String... params) {

        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Log.d(TAG, "doInBackground: ResponseCode: " + conn.getResponseCode());

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }


        } catch (Exception e) {

            return null;
        }

        return sb.toString();
    }


    private HashMap parseJSON(String s) {

        tempData = new HashMap<>();


        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);

                String symbol = jStock.getString("symbol");

                String name = jStock.getString("name");

                if(!name.equals("")) {
                    tempData.put(symbol, name);
                }
            }
            return tempData;

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}