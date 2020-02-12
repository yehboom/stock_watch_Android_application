package com.example.stock_watch2;



public class Stock implements Comparable<Stock>{
    private String stockName;
    private String pricePercent;
    private String stockFullName;
    private String price;
    private String change;

    public Stock(String stockName, String pricePercent, String stockFullName,String price,String change) {

        this.stockName = stockName;
        this.pricePercent = pricePercent;
        this.stockFullName = stockFullName;
        this.price=price;
        this.change=change;
    }

    public int compareTo(Stock anotherinstance){
        return this.stockName.compareTo(anotherinstance.stockName);
    }

    public String getChange(){return change;}
    public String getPrice(){return price;}

    public String getstockName() {
        return stockName;
    }

    public String getpricePercent() {
        return pricePercent;
    }

    public String getstockFullName() {
        return stockFullName;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "stockName='" + stockName + '\'' +
                ", pricePercent='" + pricePercent + '\'' +
                ", stockFullName='" + stockFullName + '\'' +
                ", price='"+price+'\''+
                ", change='"+change+'\''+

                '}';
    }

}

