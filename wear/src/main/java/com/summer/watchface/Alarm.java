package com.summer.watchface;

import java.util.Calendar;
import java.util.Date;

public class Alarm  implements Comparable<Alarm>{
    private Integer id;

    private Long starttime;

    private Long endtime;

    private String text;

    private Integer enable;

    private boolean select;

    private int start;

    private int end;

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getStarttime() {
        return starttime;
    }

    public void setStarttime(Long starttime) {
        this.starttime = starttime;
    }

    public Long getEndtime() {
        return endtime;
    }

    public void setEndtime(Long endtime) {
        this.endtime = endtime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    @Override
    public int compareTo(Alarm o) {
        Calendar a = Calendar.getInstance();
        a.setTime(new Date(starttime));
        Calendar b = Calendar.getInstance();
        b.setTime(new Date(o.getStarttime()));
        int aa = a.get(Calendar.HOUR_OF_DAY)*60+a.get(Calendar.MINUTE);
        int bb = b.get(Calendar.HOUR_OF_DAY)*60+b.get(Calendar.MINUTE);
        if(aa>bb){
            return 1;
        }
        return -1;

    }
}