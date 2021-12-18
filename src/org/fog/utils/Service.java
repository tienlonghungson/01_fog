package org.fog.utils;

import java.util.Random;

public class Service {
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
