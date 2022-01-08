package rushbot;

import battlecode.common.RobotType;

public class Logger {

    static boolean limitToIDs = true;
    static int[] allowedIDs = {11191};
    static boolean limitToTypes = false;
    static RobotType[] allowedTypes = {null};

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
