package resourceplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Random;

public class Util {

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static RobotController rc;
    static Robot robot;

    static MapLocation copyLoc(MapLocation loc){ return loc.add(Direction.CENTER); }

    static Direction tryBuild(RobotType type, Direction[] dirs) throws GameActionException {
        for(Direction dir : dirs){
            if (rc.canBuildRobot(type, dir)) {
                rc.buildRobot(type, dir);
                return dir;
            }
        }
        return Direction.CENTER;
    }

    static boolean tryMove(Direction[] dirs) throws GameActionException {
        for(Direction dir : dirs){
            if(Util.tryMove(dir)){
                return true;
            }
        }
        return false;
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    static Direction[] shuffleArr(Direction[] arr){
        Random rand = new Random();
        Direction[] copy = new Direction[arr.length];
        for(int i = 0; i < arr.length; i++){
            copy[i] = arr[i];
        }
        for (int i = 0; i < copy.length; i++) {
            int randomIndexToSwap = rand.nextInt(copy.length);
            Direction temp = copy[randomIndexToSwap];
            copy[randomIndexToSwap] = copy[i];
            copy[i] = temp;
        }
        return copy;
    }

}