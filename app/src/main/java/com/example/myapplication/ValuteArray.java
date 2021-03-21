package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ValuteArray{
    @SerializedName("Valute")
    private ArrayList<Valute> valute;
    public String getValutes(){
        StringBuilder s = new StringBuilder();
        for(Valute v: this.valute)
            s.append(v.toString());
        return s.toString();
    }
    public void setValutes(ArrayList<Valute> valute){
        this.valute = valute;
    }
}
