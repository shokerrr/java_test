package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class Valute {
    @SerializedName("ID")
    private String ID;
    @SerializedName("NumCode")
    private String NumCode;
    @SerializedName("CharCode")
    private String CharCode;
    @SerializedName("Nominal")
    private Integer Nominal;
    @SerializedName("Name")
    private String  Name;
    @SerializedName("Value")
    private Double Value;
    @SerializedName("Previous")
    private Double Previous;



    public String toString() {
        return String.format("%s Курс:%f\n", Name, Value);
    }
}
