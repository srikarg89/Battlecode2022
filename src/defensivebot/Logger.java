package defensivebot;

import battlecode.common.RobotType;

public class Logger {

    static boolean limitToIDs = true;
    static int[] allowedIDs = {-1};
    static boolean limitToTypes = false;
    static RobotType[] allowedTypes = {null};

    static RobotType myType;
    static int myID;

    public static void Log(String str){
        if(limitToIDs){
            if(Util.getArrayIndex(allowedIDs, myID) != -1){
                Logger.Log(str);
            }
        }
        if(limitToTypes){
            if(Util.getArrayIndex(allowedTypes, myType) != -1){
                Logger.Log(str);
            }
        }

    }

}
