package com.example.lab77;

import java.util.Date;

public class InfoLog {
    public Date date;
    public Long period;
    public String URL;
    public InfoLog(Date date_, Long period_, String URL_) {
        this.date = date_;
        this.period = period_;
        this.URL = URL_;
    }

}
