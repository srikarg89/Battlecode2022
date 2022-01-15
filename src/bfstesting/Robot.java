package bfstesting;

import battlecode.common.*;

public class Robot {

    // Constants

    // Robot properties
    RobotController rc;
    Navigation nav;
//    BFS bfs;
//    BFSOld bfsold;
    Comms comms;
    MapLocation myLoc;
    Team myTeam;
    Team opponent;
    RobotType myType;
    int age = 0;
    MapLocation[] friendlyArchons;
    int numFriendlyArchons = 0;
    int mapWidth;
    int mapHeight;
    RobotInfo[] nearby;
    int roundNum = 1;
    String indicatorString = "";

    public Robot(RobotController rc) throws GameActionException {
        nearby = rc.senseNearbyRobots();
        myLoc = rc.getLocation();
        this.rc = rc;
        Util.rc = rc;
        Util.robot = this;
        Logger.myID = rc.getID();
        Logger.myType = rc.getType();
        Logger.robot = this;
        myTeam = rc.getTeam();
        opponent = myTeam.opponent();
        myType = rc.getType();
        age = 0;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        nav = new Navigation(rc, this);
//        if(this.myType.equals(RobotType.MINER)){
//            bfs = new BFS13(rc, this);
//        }
//        else{
//            bfs = new BFS20(rc, this);
//        }
//        bfs = new BFS20(rc, this);
//        bfsold = new BFSOld(rc, this);
        comms = new Comms(rc, this);
        if(myType != RobotType.ARCHON){
            comms.findFriendlyArchons();
        }
    }

    public void run() throws GameActionException {
        roundNum = rc.getRoundNum();
        nearby = rc.senseNearbyRobots();
        if(rc.getRoundNum() == 2 && myType == RobotType.ARCHON){
            // Find teammate archons
            comms.findFriendlyArchons();
        }
        // TODO: Only do this for scouts, cuz most likely by the time anyone else gets there the archon will have moved
        if(rc.getRoundNum() >= 2){
            comms.determineSymmetry();
        }
        comms.scanEnemyArchons();
        age++;
        myLoc = rc.getLocation();
    }

}
