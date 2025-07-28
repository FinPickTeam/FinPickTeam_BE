package org.scoula.finance.util;


public class UtilityCalculator {
    public double calculateUtility(double baseRate, double additionalRate, int conditionCount,double beta){
        double R_expected = baseRate + additionalRate;

        //유저 성향 받아서 적용
//        double beta;
//        if(유저성향 == 안정형) beta = 0.01;
//        else if(유저성향 == 보통형) beta = 0.03;
//        else beta = 0.05;


        return R_expected - conditionCount * beta;
    }

}
