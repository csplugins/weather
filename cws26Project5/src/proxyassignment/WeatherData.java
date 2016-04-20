/**
   COPYRIGHT (C) 2015 Tim O’Neil. All rights reserved.
   Weather forecast assignment: daily weather forecast objects 
   @author Tim O’Neil
   @version 1.01 10.21.2015
*/

package proxyassignment;

import java.time.LocalDate;

public class WeatherData {
    private String date;
    private String dayOrNite = "";
    private int temp = 0;
    private String descr = "";
    
    public void setDate(LocalDate d) { date = d.toString(); }
    public String getDate() { return date; }
    public void setAMPM(String d) { dayOrNite = d; }
    public String getAMPM() { return dayOrNite; }
    public void setTemp(int t) { temp = t; }
    public int getTemp() { return temp; }
    public void setDescr(String s) { descr = s; }
    public String getDescr() { return descr; }
    public String toString() {
        return (date + " " + dayOrNite + ": Temp = " + temp + ", " + descr);
    }
}
