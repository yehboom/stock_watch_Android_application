package com.example.stock_watch2;


import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;


public class StockAdapter extends RecyclerView.Adapter<StockViewHolder>{
    private static final String TAG="StockAdapter";
    private ArrayList<Stock> alist;
    private MainActivity mainActivity;

    StockAdapter(ArrayList<Stock> list, MainActivity mainActivity){
        alist=list;
        this.mainActivity=mainActivity;

    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG,"onCreateViewHolder: Creating New ");

        View itemView= LayoutInflater.from(parent.getContext())
                //pass the list_row_item back to itemView
                .inflate(R.layout.list_row_item,parent,false);

        //wait to open browser
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        //and pass the entry
        return new StockViewHolder(itemView);
    }

    //take note out from arraylist//update all the data
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {

        Log.d(TAG,"onCreateViewHolder: Setting Data ");

        Stock selectedStock=alist.get(position);
        String tempChange=selectedStock.getChange();

        holder.stockName.setText(selectedStock.getstockName());
        holder.price.setText(selectedStock.getPrice());
        holder.stockFullName.setText(selectedStock.getstockFullName());

        holder.pricePercent.setText("("+selectedStock.getpricePercent()+")");
        holder.change.setText(tempChange);

        if(Double.parseDouble(tempChange)>0){
            holder.icon.setText("▲ ");
            holder.icon.setTextColor(Color.parseColor("#21D72C"));
            holder.stockFullName.setTextColor(Color.parseColor("#21D72C"));
            holder.price.setTextColor(Color.parseColor("#21D72C"));
            holder.stockName.setTextColor(Color.parseColor("#21D72C"));
            holder.change.setTextColor(Color.parseColor("#21D72C"));
            holder.pricePercent.setTextColor(Color.parseColor("#21D72C"));

        }else if(Double.parseDouble(tempChange)<0){
            holder.icon.setText("▼ ");
            holder.icon.setTextColor(Color.RED);
            holder.stockFullName.setTextColor(Color.RED);
            holder.price.setTextColor(Color.RED);
            holder.stockName.setTextColor(Color.RED);
            holder.change.setTextColor(Color.RED);
            holder.pricePercent.setTextColor(Color.RED);
        }

    }

    @Override
    public int getItemCount() {
        return alist.size();
    }

}
