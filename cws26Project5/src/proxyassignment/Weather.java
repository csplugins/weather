/**
   COPYRIGHT (C) 2015 Tim O’Neil. All rights reserved.
   Weather forecast assignment: interface for WeatherForecast and its proxy
   @author Tim O’Neil
   @version 1.01 10.20.2015
*/

package proxyassignment;

public interface Weather {
        public WeatherData[] getForecast();
}