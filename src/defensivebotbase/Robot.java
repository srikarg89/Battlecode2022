package defensivebotbase;

import battlecode.common.*;

public class Robot {

    // Constants

    // Robot properties
    RobotController rc;
    Navigation nav;
    Comms comms;
    MapLocation myLoc;
    Team myTeam;
    Team opponent;
    RobotType myType;
    int age = 0;
    MapLocation[] friendlyArchons;
    int numFriendlyArchons = 0;
    boolean symmetryDetermined = false;
    int mapWidth;
    int mapHeight;
    RobotInfo[] nearby;

    public Robot(RobotController rc) throws GameActionException {
        nearby = rc.senseNearbyRobots();
        myLoc = rc.getLocation();
        this.rc = rc;
        Util.rc = rc;
        Util.robot = this;
        Logger.myID = rc.getID();
        Logger.myType = rc.getType();
        myTeam = rc.getTeam();
        opponent = myTeam.opponent();
        myType = rc.getType();
        age = 0;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        nav = new Navigation(rc, this);
        comms = new Comms(rc, this);
//        nav.minDistToSatisfy = myType.actionRadiusSquared;
        if(myType != RobotType.ARCHON){
            comms.findFriendlyArchons();
        }
    }

    public void run() throws GameActionException {
        nearby = rc.senseNearbyRobots();
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
