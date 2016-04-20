/**
 * @author Cody Skala cws26@zips.uakron.edu
 * Date: December 3, 2015 15:20
 * This is the Proxy class created to return the parsed data from NWS from Dr. O'Neil's files.
 */
package cws26project5;

import proxyassignment.*;

public class Proxy implements Weather{
    private String zip;
    private WeatherForecast wf;
    
    public Proxy(String z){
        zip = z;
        wf = null;
    }

    @Override
    public WeatherData[] getForecast() {
        if(wf == null)
            wf = new WeatherForecast(zip);
        return wf.getForecast();
    }
    
}
