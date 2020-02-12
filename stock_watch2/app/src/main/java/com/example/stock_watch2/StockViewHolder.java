package com.example.stock_watch2;
import android.view.View;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder{
    TextView stockName;
    TextView stockFullName;
    TextView pricePercent;
    TextView price;
    TextView change;
    TextView icon;


    public StockViewHolder(View view) {
        super(view);
        stockName=view.findViewById(R.id.StocknameId);
        stockFullName=view.findViewById(R.id.StockFullnameid);
        pricePercent=view.findViewById(R.id.pricePercentId);
        price=view.findViewById(R.id.priceid);
        change=view.findViewById(R.id.changeId);
        icon=view.findViewById(R.id.iconId);
    }
}
