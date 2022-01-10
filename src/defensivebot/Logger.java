package defensivebot;

import battlecode.common.RobotType;

public class Logger {

    static boolean limitToIDs = false;
    static int[] allowedIDs = {13048};
    static boolean limitToTypes = false;
    static RobotType[] allowedTypes = {RobotType.BUILDER};

    static RobotType myType;
    static int myID;

    public static void Log(String str){
        if(limitToIDs){
            if(Util.getArrayIndex(allowedIDs, myID) != -1){
                System.out.println(str);
            }
        }
        if(limitToTypes){
            if(Util.getArrayIndex(allowedTypes, myType) != -1){
                System.out.println(str);
            }
        }

    }

}
