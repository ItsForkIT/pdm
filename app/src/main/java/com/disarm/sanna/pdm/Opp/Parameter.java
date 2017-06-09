package com.disarm.sanna.pdm.Opp;

/**
 * Created by naman on 24/3/17.
 */

public class Parameter {
    public static int maximum_AP_on = 600; //10 minutes
    public static int minimum_wait_AP = 10; //10 seconds
    public static int ap_increase_time= 10000;
    public static int current_ap_time = 60000;
    public static int maximum_ap_increase_time = 120000;
    public static int current_wifi_time = 10000;
    public static int increase_wifi_time = 10000;
    public static int maximum_wifi_time = 120000;
    public static int alpha=2; //STA switch parameter
    public static double beta=1/2; //AP switch parameter
    public static double ws=1/40; //STA switch parameter
    public static double wa=1/20; //AP switch parameter
    public static int first_time=1;
    public static int idle_time_for_AP_mode=0;
    public static int recent_number_of_neighbours = 0;
    public static int current_number_of_neighbours = 0;
    public static int current_hotspot_running_time = 0;
    public static int previous_hotspot_running_time = 0;

}
