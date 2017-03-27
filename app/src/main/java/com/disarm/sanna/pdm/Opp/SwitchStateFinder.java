package com.disarm.sanna.pdm.Opp;

/**
 * Created by naman on 24/3/17.
 */
import java.util.Random;
public class SwitchStateFinder {

    public static double getRandomNumber(int limit){
        Random rand= new Random();
        return rand.nextInt(limit);
    }

    public static boolean shouldIBecomeAP(int number_of_recent_neighbours, int time_since_it_was_off){

        if(number_of_recent_neighbours>0){
            if(time_since_it_was_off>Parameter.minimum_wait_AP){

                double prob = 1/number_of_recent_neighbours;
                double our_value = SwitchStateFinder.getRandomNumber(99)/100;

                if(our_value>prob)
                    return false;
                else
                    return true;
            }

            else {
                double prob = 1 / 2;
                double our_value = SwitchStateFinder.getRandomNumber(99) / 100;

                if (our_value > prob)
                    return false;
                else
                    return true;
            }
        }

        else
            return false;
    }

    public static boolean shouldSTAChange(int number_of_recent_neighbours){
        if(number_of_recent_neighbours>0){
            double prob = Parameter.ws * Math.pow(number_of_recent_neighbours,-Parameter.alpha);
            double our_value = SwitchStateFinder.getRandomNumber(99) / 100;

            if (our_value > prob)
                return false;
            else
                return true;

        }
        else
            return true;
    }

    public static boolean shouldAPChangeToIdel(int number_of_recent_neighbours,int total_time_when_AP_was_on){
        if(total_time_when_AP_was_on<Parameter.maximum_AP_on && number_of_recent_neighbours>0){
            double prob = Parameter.wa * Math.pow(number_of_recent_neighbours,-Parameter.beta);
            double our_value = SwitchStateFinder.getRandomNumber(99) / 100;

            if (our_value > prob)
                return false;
            else
                return true;
        }
        else
            return true;
    }
}
