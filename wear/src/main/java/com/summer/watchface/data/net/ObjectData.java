package com.summer.watchface.data.net;


import java.io.Serializable;


public class ObjectData<T> implements Serializable {

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
