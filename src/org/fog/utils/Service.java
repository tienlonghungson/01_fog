package org.fog.utils;

import java.util.Random;

public class Service {
    /**
     * generate a random number from min (including) to max (including)
     * @param min lower bound
     * @param max upper bound
     * @return a random number
     */
	public static int rand(int min, int max) {
        try {
            Random rn = new Random();
            int range = max - min + 1;
            return min + rn.nextInt(range);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
