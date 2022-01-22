package restart;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

abstract class BFS {

    RobotController rc;
    Robot robot;
    boolean vars_are_reset = false;

    public BFS(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot=robot;
        this.vars_are_reset=false;
    }

    abstract boolean checkVarsReset();
    abstract void resetVars() throws GameActionException;
    abstract Direction runBFSNorth(MapLocation target) throws GameActionException;
    abstract Direction runBFSSouth(MapLocation target) throws GameActionException;
    abstract Direction runBFSEast(MapLocation target) throws GameActionException;
    abstract Direction runBFSWest(MapLocation target) throws GameActionException;
    abstract Direction runBFSNortheast(MapLocation target) throws GameActionException;
    abstract Direction runBFSNorthwest(MapLocation target) throws GameActionException;
    abstract Direction runBFSSoutheast(MapLocation target) throws GameActionException;
    abstract Direction runBFSSouthwest(MapLocation target) throws GameActionException;
    abstract Direction getBestDir(MapLocation target) throws GameActionException;


}
