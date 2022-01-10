package defensivebotbase;

import battlecode.common.RobotType;

public class Logger {

    static boolean limitToIDs = false;
    //    static int[] allowedIDs = {10025, 11726};
    static int[] allowedIDs = {11229};
    static boolean limitToTypes = true;
    static RobotType[] allowedTypes = {RobotType.SOLDIER};

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
