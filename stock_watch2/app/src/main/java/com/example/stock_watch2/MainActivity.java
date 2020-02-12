package com.example.stock_watch2;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private Activity mActivity;
    private int pos;
    private boolean clickon=false;
    private static final int CODE_FOR_EDIT_ACTIVITY = 111;
    private ArrayList<Stock> stockList=new ArrayList<>();
    private ArrayList<Stock> tempstockList=new ArrayList<>();
    private ArrayList<Stock> reloadtockList;
    private HashMap<String,String> tempData2=new HashMap<>();
    private RecyclerView recyclerView;
    private StockAdapter StockAdapter;
    private static final String TAG = "MainActivity";
    private Stock newStock;
    private ArrayList<String> compare=new ArrayList<>();
    private boolean flag=false;
    private SwipeRefreshLayout swiper; // The SwipeRefreshLayout
    //write to json file
    @Override
    protected void onPause()
    {
        super.onPause();
        JSONArray jsonArray = new JSONArray();
        for (Stock n : stockList) {
            try {
                JSONObject stockJSON = new JSONObject();
                stockJSON.put("stockName", n.getstockName());
                stockJSON.put("stockFullName", n.getstockFullName());
                stockJSON.put("pricePercent", n.getpricePercent());
                stockJSON.put("price", n.getPrice());
                stockJSON.put("change",n.getChange());

                jsonArray.put(stockJSON);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String jsonText = jsonArray.toString();
       // Log.d(TAG, "doWrite: " + jsonText);
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            openFileOutput("mydata.txt", Context.MODE_PRIVATE)
                    );
            outputStreamWriter.write(jsonText);
            outputStreamWriter.close();

        }
        catch (IOException e) {
            Log.d(TAG, "doWrite: File write failed: " + e.toString());

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = MainActivity.this;
//
        swiper=findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "Reload!", Toast.LENGTH_SHORT).show();

                doReload();
            }
        });


        stockList.clear();

       //read json file
        try {
            InputStream inputStream = openFileInput("mydata.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                String jsonText = stringBuilder.toString();
                Log.d(TAG, "Read jsonTest!: " + jsonText);


                try {
                    JSONArray jsonArray = new JSONArray(jsonText);
                    Log.d(TAG, "Json Array length: " + jsonArray.length());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String stockName = jsonObject.getString("stockName");
                        String stockFullName = jsonObject.getString("stockFullName");
                        String pricePercent = jsonObject.getString("pricePercent");

                        String change=jsonObject.getString("change");
                        String price= jsonObject.getString("price");

                        Stock n = new Stock(stockName, pricePercent,stockFullName, price,change);
                        stockList.add(n);
                        compare.add(jsonObject.getString("stockName"));

                        Collections.sort(stockList);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "doRead: File not found: \" + e.toString()");
        } catch (IOException e) {
            Log.d(TAG, "doRead: Can not read file: " + e.toString());
        }


        recyclerView=findViewById(R.id.recyler);


        if(doNetCheck()==false){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection ");

            AlertDialog dialog = builder.create();
            dialog.show();


            for (int i=0;i<stockList.size();i++){
                tempstockList.add(new Stock(stockList.get(i).getstockName(),"0.00%",stockList.get(i).getstockFullName(),"0.0","0.0"));
                refresh(tempstockList);
            }

        }else{
            //to detect the connect is true
            flag=true;
            refresh(stockList);
            // Load the data
            new NameDownloaderAsyncTask(this).execute();

        }


    }
    private void doReload(){

        if(doNetCheck()!=false){
        reloadtockList  = new ArrayList<>(stockList);
        stockList.clear();

        for(int i=0;i<reloadtockList.size();i++){
            loadFinancial(reloadtockList.get(i).getstockName());
        }
        StockAdapter.notifyDataSetChanged();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection ");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        swiper.setRefreshing(false);
    }
    private void refresh(ArrayList stockList){
        recyclerView=findViewById(R.id.recyler);
        //put list into adapter
        StockAdapter=new StockAdapter(stockList,this);

        //recycleView need to know which adapter will use
        recyclerView.setAdapter(StockAdapter);
        LinearLayoutManager a=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(a);

    }

    //MENU OPTION
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    public void searchStock(String text){
        String textLower=text.toLowerCase();

        Map map = new HashMap();
        map=tempData2;

        ArrayList<String> temp=new ArrayList<>();

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString();
            String keyCompare=key.toLowerCase();
            String val = entry.getValue().toString();
            String valCompare=val.toLowerCase();

            if(keyCompare.contains(textLower)||valCompare.contains(textLower)){

                temp.add(key);
                Collections.sort(temp);
            }
        }
        //if match stock number is greater than one
        if(temp.size()>1){
            selectionStock(temp);
        }else if(temp.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Data for stock symbol");
            builder.setTitle("Symbol Not Found: "+text);

            AlertDialog dialog = builder.create();
            dialog.show();


        }else{
            if(!compare.contains(temp.get(0))){
                compare.add(temp.get(0));
                Collections.sort(compare);
                loadFinancial(temp.get(0));
            }else{
                Toast.makeText(MainActivity.this, "here2!", Toast.LENGTH_SHORT).show();
                final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setIcon(R.drawable.baseline_warning_black_48);

                builder2.setMessage("Stock Symbol is already displayed.");
                builder2.setTitle("Duplicate Stock");
                AlertDialog dialog2 = builder2.create();
                dialog2.show();
            }

        }

    }


    private String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }
        return capMatcher.appendTail(capBuffer).toString();
    }

    public void selectionStock(final ArrayList<String> temp){

        final String[] array = new String[temp.size()];
        final String[] arrayOriginal=new String[temp.size()];
        final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);

        for (int i = 0; i < temp.size(); i++) {
            String result=temp.get(i);
            String result2=tempData2.get(result);
            String result3="";


            if (result2.length()!=0) {
                result3=capitalize(result2);
            }
            array[i] = result+" - "+result3+".";
            arrayOriginal[i] = result;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");


        // Set the builder to display the string array as a selectable
        // list, and add the "onClick" for when a selection is made
        builder.setItems(array, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String s= array[which];

                Toast.makeText(MainActivity.this, "You Choose!"+s, Toast.LENGTH_SHORT).show();
                //call next loader

                Log.d(TAG, "s!!!!!!: "+ s);

                if(!compare.contains(arrayOriginal[which])){
                    Log.d(TAG, "Compare!!!!!qq "+compare);
                    compare.add(arrayOriginal[which]);
                    Collections.sort(compare);
                    //pass key
                    loadFinancial(arrayOriginal[which]);

                }else{//duplicate key
                    Log.d(TAG, "Compare!!!!!gg "+compare);

                    builder2.setIcon(R.drawable.baseline_warning_black_48);
                    builder2.setMessage("Stock Symbol is already displayed.");
                    builder2.setTitle("Duplicate Stock");
                    AlertDialog dialog2 = builder2.create();
                    dialog2.show();


                }

            }
        });

        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();

            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }



    public void loadFinancial(String inputtext){
        new StockDownloaderAsyncTask(this).execute(inputtext);
    }

    //menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.stockddid:
                if(doNetCheck()==false){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("No Network Connection");
                    builder.setMessage("Stocks Cannot Be Added Without A Network Connection ");

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }else{
                    //if previous is no connection but now is connected, and do Async to load
                    if(flag==false){
                        new NameDownloaderAsyncTask(this).execute();
                    }
                }



                // Single input value dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // Create an edittext and set it to be the builder's view
                final EditText et = new EditText(getApplicationContext());
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                et.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                et.setGravity(Gravity.CENTER_HORIZONTAL);

                builder.setView(et);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String text= et.getText().toString();
                        if(text.isEmpty()){
                            return;
                        }
                        searchStock(text);


                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();

                    }
                });

                builder.setMessage("Please enter a Stock Symbol:");
                builder.setTitle("Stock Selection");

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            default:
                Toast.makeText(this, "Pick the unknown item", Toast.LENGTH_LONG).show();
        }
        return true;

    }

    // open browser
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onClick(View view) {
        Log.d(TAG, "onClick: !");
        pos =recyclerView.getChildLayoutPosition(view);
        Log.d(TAG, "in onClick Pos position" + pos);
        Stock selectiion=stockList.get(pos);
        String symbol=selectiion.getstockName();

        String url = "https://www.marketwatch.com/investing/stock/" + symbol;

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

        Toast.makeText(this, "OnClick!", Toast.LENGTH_SHORT).show();
    }



//    //delete stock
    @Override
    public boolean onLongClick(View view){
        final int pos =recyclerView.getChildLayoutPosition(view);
        Log.d(TAG, "delete position "+pos);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        if(doNetCheck()==true){

                            if(stockList.size()==1 && pos==1){
                                stockList.remove(0);
                            }else{
                                stockList.remove(pos);
                            }
                            compare.remove(pos);
                            StockAdapter.notifyDataSetChanged();
                            refresh(stockList);

                        }else{

                            stockList.remove(pos);
                            compare.remove(pos);
                            StockAdapter.notifyDataSetChanged();
                        }

                    }

                });

        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });

        builder.setTitle("Delete Stock ");

        builder.setMessage("Delete Stock Symbol");

        builder.setIcon(R.drawable.baseline_delete_black_48);

        AlertDialog dialog = builder.create();
        dialog.show();


        return true;
    }


    public void addList(Stock newStock){
        stockList.add(newStock);
        if(stockList.size()>1){
            Collections.sort(stockList);}
    }

    public void updateData(HashMap sHashMAP) {
        tempData2.putAll(sHashMAP);
    }
    public void updateData2(Stock newstock) {

        this.newStock=newstock;
        Log.d(TAG, "updateData in main: "+newstock);
        addList(newStock);

        StockAdapter.notifyDataSetChanged();
        refresh(stockList);
    }



    private Boolean doNetCheck() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
           // Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            //statusText.setText(R.string.connected);
            //Toast.makeText(this, "Internet Connect!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
//            statusText.setText(R.string.not_connected);
            Toast.makeText(this, "No Connect!!!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void networkCheck(View v) {
        doNetCheck();
    }

}
