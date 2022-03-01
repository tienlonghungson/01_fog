package org.fog.utils;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;

import java.util.Random;

public class Service {

    /**
     * rd object
     */
    private static final Random rd = new Random();
    /**
     * generate a random number from min (including) to max (including)
     * @param min lower bound
     * @param max upper bound
     * @return a random number
     */
	public static int rand(int min, int max) {
        try {
            int range = max - min + 1;
            return min + rd.nextInt(range);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * calculates the cost (G$) when a fogDevice executes a cloudlet
     * @param cloudlet cloud information
     * @param fogDevice fogDevice information
     * @return the cost when a fogDevice executes a cloudlet
     */
    public static double calcCost(Cloudlet cloudlet, FogDevice fogDevice) {
        double cost = 0;
        //cost includes the processing cost
        cost += fogDevice.getCharacteristics().getCostPerSecond() * cloudlet.getCloudletLength() / fogDevice.getHost().getTotalMips();
        // cost includes the memory cost
        cost += fogDevice.getCharacteristics().getCostPerMem() * cloudlet.getMemRequired();
        // cost includes the bandwidth cost
        cost += fogDevice.getCharacteristics().getCostPerBw() * (cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize());
        return cost;
    }

    public static double binaryParameter(double idxDist){
        double u = rd.nextDouble();
        if (u<=(double)1/2){
            return Math.pow(2*u,(double)1/(idxDist+1));
        } else {
            return (double) 1/Math.pow((2*(1-u)),(double) 1/(idxDist+1));
        }
    }

    public static double binaryParameter(){
        return binaryParameter(1.5);
    }

    /**
     * generate a random number from Poisson distribution
     * @param lambda coefficient of Poisson distribution
     * @return a random number
     */
    public static int poissonRand(int lambda){
        final double L = Math.exp(-lambda);
        double p=1.0;
        int k=0;

        do{
            k++;
            p*=Math.random();
        } while (p>L);
        return k-1;
    }

    /**
     * calculate Euclid distance in datatype double
     * @param point1 first point
     * @param point2 second point
     * @return distance (datatype: double)
     */
    public static double euclidDistance(double[] point1, double[] point2){
        assert (point1.length== point2.length);
        final int LEN = point1.length;
        double dis=0;
        double tmp;
        for (int i=0;i<LEN;++i){
            tmp = point1[i]-point2[i];
            dis+= tmp*tmp;
        }
        return Math.sqrt(dis);
    }

    /**
     * calculate Euclid distance in datatype float
     * @param point1 first point
     * @param point2 second point
     * @return distance (datatype: float)
     */
    public static float euclidDistance(float[] point1, float[] point2) {
        assert (point1.length== point2.length);
        final int LEN = point1.length;
        float dis=0;
        float tmp;
        for (int i=0;i<LEN;++i){
            tmp = point1[i]-point2[i];
            dis+= tmp*tmp;
        }
        return (float)Math.sqrt(dis);
    }
}
