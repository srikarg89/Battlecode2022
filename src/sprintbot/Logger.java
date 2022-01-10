package sprintbot;

import battlecode.common.RobotType;

public class Logger {

    static boolean limitToIDs = false;
    static int[] allowedIDs = {-1};
    static boolean limitToTypes = false;
    static RobotType[] allowedTypes = {RobotType.MINER};

    static RobotType myType;
    static int myID;

    public static void Log(String str){
        if(limitToIDs){
            if(Util.getArrayIndex(allowedIDs, myID) != -1){
                System.out.println(str);
            }
        }
        if(limitToTypes){
//            if(Util.getArrayIndex(allowedTypes, myType) != -1){
//            if(myType == RobotType.MINER){
//                System.out.println(str);
//            }
            if(myType == RobotType.SOLDIER && myID == 12887){
                System.out.println(str);
            }
        }

    }

}
