package com.summer.watchface.data.net;


import java.io.Serializable;
import java.util.ArrayList;

public class ListData<T> implements Serializable {

    private ArrayList<T> data;

    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        this.data = data;
    }
}
