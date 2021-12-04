package com.example.kelimebilmece;

import android.widget.ImageView;

import java.util.ArrayList;

public class Shop {

    private float itemPrice;
    private String itemTitle;
    private int itemImg;

    public Shop(){}


    public Shop(float itemPrice, String itemTitle, int itemImg) {
        this.itemPrice = itemPrice;
        this.itemTitle = itemTitle;
        this.itemImg = itemImg;
    }

    public float getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(float itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public int getItemImg() {
        return itemImg;
    }

    public void setItemImg(int itemImg) {
        this.itemImg = itemImg;
    }

    static  public ArrayList<Shop> getData(){
        ArrayList<Shop> shopList = new ArrayList<>();
        String[] itemTitleList = {"5 Adet Can", "20 Adet Can", "50 Adet Can", "150 Adet Can", "500 Adet Can"};
        int[] itemImgList = {R.drawable.heart, R.drawable.heart, R.drawable.heart, R.drawable.heart4, R.drawable.heart4};
        float[] itemPriceList = {0.99f, 2.99f, 5.99f, 12.99f, 19.99f};

        //Verileri sınıfa doldurduk
        for (int i = 0; i < itemTitleList.length; i++){
            Shop shop = new Shop();
            shop.setItemTitle(itemTitleList[i]);
            shop.setItemPrice(itemPriceList[i]);
            shop.setItemImg(itemImgList[i]);


            //sınıfı arrayListe ekledik
            shopList.add(shop);
        }
        return shopList;
    }
}
