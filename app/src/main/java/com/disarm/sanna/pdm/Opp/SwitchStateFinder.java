package com.disarm.sanna.pdm.Opp;

/**
 * Created by naman on 24/3/17.
 */
import android.util.Log;

import java.util.Random;
public class SwitchStateFinder {


    public static double getRandomNumber(int limit){
        Random rand= new Random();
        return rand.nextInt(limit);
    }

    public static boolean shouldIBecomeAP(int number_of_recent_neighbours, int time_since_it_was_off){
        Log.v("Should I become AP","Recent_Neighnours "+number_of_recent_neighbours+" time_since_ap_off "+time_since_it_was_off);
        if(number_of_recent_neighbours>1){
            if(time_since_it_was_off>Parameter.minimum_wait_AP){

                double prob = 1/number_of_recent_neighbours;
                double our_value = SwitchStateFinder.getRandomNumber(99)/100;
                Log.v("Should I become AP","Our prob "+our_value+" calculated value "+prob);
                if(our_value>prob)
                    return false;
                else
                    return true;
            }

            else {
                double prob = 1 / 2;
                double our_value = SwitchStateFinder.getRandomNumber(99) / 100;
                Log.v("Should I become AP","Our prob "+our_value+" calculated value "+prob);
                if (our_value > prob)
                    return false;
                else
                    return true;
            }
        }

        else{
            if(SwitchStateFinder.getRandomNumber(99)>50) {
                Log.v("Should I become AP","Becoming AP");
                return true;
            }
            else {
                Log.v("Should I become AP","Denied");
                return false;
            }
        }
    }

    public static boolean shouldSTAChange(int number_of_recent_neighbours){
        if(number_of_recent_neighbours>1){
            double prob = Parameter.ws * Math.pow(number_of_recent_neighbours,-Parameter.alpha);
            double our_value = SwitchStateFinder.getRandomNumber(99) / 100;
            Log.v("shouldSTAChange","our prob "+our_value+" calculated prob "+prob);
            Log.v("shouldSTAChange","Number of neighbours "+number_of_recent_neighbours);
            if (our_value > prob)
                return false;
            else
                return true;

        }
        else {
            Log.v("ShouldSTAChange","number of neighbours were zero");
            return true;
        }
    }

    public static boolean shouldAPChangeToIdel(int number_of_recent_neighbours,int total_time_when_AP_was_on){
        Log.v("ShouldAPChangeToIdel","recent neighbours "+number_of_recent_neighbours+" time since AP was on "+total_time_when_AP_was_on);
        if(total_time_when_AP_was_on<Parameter.maximum_AP_on && number_of_recent_neighbours>0){
            double prob = Parameter.wa * Math.pow(number_of_recent_neighbours,-Parameter.beta);
            double our_value = SwitchStateFinder.getRandomNumber(99) / 100;
            Log.v("ShouldAPChangeToIdeal","our prob "+our_value+" calculated prob "+prob);
            if (our_value > prob)
                return false;
            else
                return true;
        }
        else {
            Log.v("shouldAPChangeToIdel","Forcefully switched to idel");
            return true;
        }
    }
}
