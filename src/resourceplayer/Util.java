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

    static int mapLocationToInt(MapLocation loc){
        return loc.x * 1000 + loc.y;
    }

    static MapLocation intToMapLocation(int num){
        return new MapLocation(num / 1000, num % 1000);
    }

    static MapLocation multiplyDirection(MapLocation loc, Direction dir, int n){
        int x = 0;
        int y = 0;
        if(dir == Direction.NORTH){
            x = 0; y = 1;
        }
        if(dir == Direction.SOUTH){
            x = 0; y = -1;
        }
        if(dir == Direction.EAST){
            x = 1; y = 0;
        }
        if(dir == Direction.WEST){
            x = -1; y = 0;
        }
        if(dir == Direction.NORTHEAST){
            x = 1; y = 1;
        }
        if(dir == Direction.NORTHWEST){
            x = -1; y = 1;
        }
        if(dir == Direction.SOUTHEAST){
            x = 1; y = -1;
        }
        if(dir == Direction.SOUTHWEST){
            x = -1; y = -1;
        }

        return new MapLocation(loc.x + x * n, loc.y + y * n);
    }

    public static int directionToX(Direction dir){
        if(dir == Direction.EAST || dir == Direction.NORTHEAST || dir == Direction.SOUTHEAST){
            return 1;
        }
        if(dir == Direction.WEST || dir == Direction.NORTHWEST || dir == Direction.SOUTHWEST){
            return -1;
        }
        return 0;
    }

    public static int directionToY(Direction dir){
        if(dir == Direction.NORTH || dir == Direction.NORTHEAST || dir == Direction.NORTHWEST){
            return 1;
        }
        if(dir == Direction.SOUTH || dir == Direction.SOUTHEAST || dir == Direction.SOUTHWEST){
            return -1;
        }
        return 0;
    }

}