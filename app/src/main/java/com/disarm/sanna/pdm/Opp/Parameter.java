package com.disarm.sanna.pdm.Opp;

/**
 * Created by naman on 24/3/17.
 */

public class Parameter {
    public static int maximum_AP_on = 600; //10 minutes
    public static int minimum_wait_AP = 10; //10 seconds
    public static int maximum_limit_wait_AP = 120; //2 minutes
    public static int alpha=2; //STA switch parameter
    public static double beta=1/2; //AP switch parameter
    public static double ws=1/40; //STA switch parameter
    public static double wa=1/20; //AP switch parameter
    public static String currentState = "IDLE"; //Current State
}
