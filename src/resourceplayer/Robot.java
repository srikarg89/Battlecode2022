package resourceplayer;

import battlecode.common.*;

public class Robot {

    RobotController rc;
    Navigation nav;
    MapLocation myLoc;
    Team myTeam;
    RobotType myType;
    int age = 0;

    public Robot(RobotController rc){
        myLoc = rc.getLocation();
        this.rc = rc;
        nav = new Navigation(rc, this);
        Util.rc = rc;
        Util.robot = this;
        myTeam = rc.getTeam();
        myType = rc.getType();
        age = 0;
    }

    public void run() throws GameActionException {
        age++;
        myLoc = rc.getLocation();
    }

}
