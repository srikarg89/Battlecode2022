package rushbot;

import battlecode.common.*;

public class Robot {

    // Constants

    // Robot properties
    RobotController rc;
    Navigation nav;
    Comms comms;
    MapLocation myLoc;
    Team myTeam;
    RobotType myType;
    int age = 0;
    MapLocation[] friendlyArchons;
    int numFriendlyArchons = 0;
    boolean symmetryDetermined = false;
    int mapWidth;
    int mapHeight;

    public Robot(RobotController rc) throws GameActionException {
        myLoc = rc.getLocation();
        this.rc = rc;
        nav = new Navigation(rc, this);
        Util.rc = rc;
        Util.robot = this;
        Logger.myID = rc.getID();
        Logger.myType = rc.getType();
        comms = new Comms(rc, this);
        myTeam = rc.getTeam();
        myType = rc.getType();
        age = 0;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        nav.minDistToSatisfy = myType.actionRadiusSquared;
        if(myType != RobotType.ARCHON){
            comms.findFriendlyArchons();
        }
    }

    public void run() throws GameActionException {
        if(rc.getRoundNum() == 2 && myType == RobotType.ARCHON){
            // Find teammate archons
            comms.findFriendlyArchons();
        }
        // TODO: Only do this for scouts, cuz most likely by the time anyone else gets there the archon will have moved
        if(rc.getRoundNum() >= 2){
//            Logger.Log("Determining symmetry");
            comms.determineSymmetry();
        }
        comms.scanEnemyArchons();
        age++;
        myLoc = rc.getLocation();
    }

}
