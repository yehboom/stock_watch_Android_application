package com.example.stock_watch2;



import android.annotation.SuppressLint;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;

import java.util.HashMap;



public class StockDownloaderAsyncTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "StockDownloader";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private HashMap<String, String> wData = new HashMap<>();
    private Stock newStock;

    private static final String stockURL = "https://cloud.iexapis.com/stable/stock";

    private static final String yourAPIKey = "sk_3cd0143183c74327a24769905a4dc546";


    StockDownloaderAsyncTask(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPostExecute(String s) {
        mainActivity.updateData2(newStock);
    }

    @Override
    protected String doInBackground(String... params) {

        String input=params[0];

        Uri.Builder buildURL = Uri.parse(stockURL).buildUpon();

        buildURL.appendPath(input);


        buildURL.appendPath("quote");

        buildURL.appendQueryParameter("token", yourAPIKey);

        String urlToUse = buildURL.build().toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

        parseJSON(sb.toString());

        return null;
    }

    private void parseJSON(String s) {

        try {
            JSONObject jObjMain = new JSONObject(s);

            wData.put("symbol", jObjMain.getString("symbol"));
            wData.put("companyName", jObjMain.getString("companyName"));
            wData.put("latestPrice", jObjMain.getString("latestPrice"));
            wData.put("change", jObjMain.getString("change"));
            wData.put("changePercent", jObjMain.getString("changePercent"));


            if(!jObjMain.getString("changePercent").equals("null") && !jObjMain.getString("change").equals("null")){
                float result = Float.parseFloat(jObjMain.getString("changePercent")) * 100;

                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);


                String pricePercent = String.valueOf(df.format(result));

                if(jObjMain.getString("latestPrice").equals("null")){
                    newStock = new Stock(jObjMain.getString("symbol"), "0.0 %", jObjMain.getString("companyName"), "0.0", "0.0");
                }else{
                    newStock = new Stock(jObjMain.getString("symbol"),pricePercent +"%", jObjMain.getString("companyName"), jObjMain.getString("latestPrice"), jObjMain.getString("change"));
                }

               }
            else{
                newStock = new Stock(jObjMain.getString("symbol"), "0.0 %", jObjMain.getString("companyName"), "0.0", "0.0");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
