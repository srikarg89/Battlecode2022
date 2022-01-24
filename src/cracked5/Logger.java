package cracked5;

import battlecode.common.RobotType;

public class Logger {

    static boolean limitToIDs = false;
    static int[] allowedIDs = {-1};
    static boolean limitToTypes = false;
    static RobotType[] allowedTypes = {RobotType.MINER};

    static RobotType myType;
    static int myID;
    static Robot robot;

    public static void Log(String str){
//        robot.indicatorString += str + "; ";
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
            if(myType == RobotType.SOLDIER && robot.rc.getID() == 10186){
                System.out.println(str);
            }
        }

    }

}
