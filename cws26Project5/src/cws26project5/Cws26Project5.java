/**
 * @author Cody Skala cws26@zips.uakron.edu
 * Date: December 3, 2015 15:20
 * This program will get the weather forecast using a proxy for a certain zip code
 * and produce a nice GUI with different panels for organization and details.
 */
package cws26project5;

import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.border.*;
import proxyassignment.*;

public class Cws26Project5 {    
    public static void main(String[] args) {
        int count = 0;
        String zip;
        Scanner in = new Scanner(System.in);
        System.out.print("Please enter the zip code: ");
        zip = in.next();
        Proxy wf = new Proxy(zip);
        WeatherData[] wd = wf.getForecast();
        String[] images = new String[4];
        int[] temperatures = new int[4];
        String[] dayTime = {"Today", "Tonight", "Tomorrow", "Tomorrow Night"};
        for (WeatherData w : wd) {
            System.out.println(w);
            if (count < 4) {
                images[count] = w.getDescr();
                temperatures[count] = w.getTemp();
                count++;
            }
        }

        JFrame frame = new JFrame();
        frame.setTitle("Weather Forecast for ZIP code " + zip);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 4));

        java.awt.Image img = null;
        for (int i = 0; i < 4; i++) {
            JPanel subPanel = new JPanel();
            subPanel.setLayout(new GridLayout(2, 1));
            subPanel.setBorder(new TitledBorder(new LineBorder(Color.blue, 3),dayTime[i]));
            subPanel.setBackground(Color.GREEN);
            try {
                java.net.URL u = new java.net.URL(images[i]);//"http://forecast.weather.gov/images/wtf/nra90.jpg"); 
                img = javax.imageio.ImageIO.read(u);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            subPanel.add(new JLabel (new javax.swing.ImageIcon(img)));
            JLabel label2 = new JLabel((i%2 == 0? "High ": "Low ") + temperatures[i]);
            label2.setHorizontalAlignment(JLabel.CENTER);
            subPanel.add(label2);
            mainPanel.add(subPanel);

        }
        
        mainPanel.setBorder(new TitledBorder(new LineBorder(Color.RED, 2)));
        frame.add(mainPanel);
        frame.setSize(550, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
