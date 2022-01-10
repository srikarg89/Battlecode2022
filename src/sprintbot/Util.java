package sprintbot;

import battlecode.common.*;

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
        for(int i = 0; i < dirs.length; i++){
            Direction dir = dirs[i];
//            Logger.Log("Tryna build robot of type: " + type.toString() + " in direction: " + dir.toString());
            if (rc.canBuildRobot(type, dir)) {
                rc.buildRobot(type, dir);
                return dir;
            }
        }
        return Direction.CENTER;
    }

    static boolean tryMove(Direction[] dirs) throws GameActionException {
        for(int i = 0; i < dirs.length; i++){
            if(Util.tryMove(dirs[i])){
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


    static int xAndYToCompressed(int x, int y){
        return x * 100 + y + 1;
    }

    static int[] compressedToXAndY(int num) {
        int x = (num - 1) / 100;
        int y = (num - 1) % 100;
        int[] data = {x, y};
        return data;
    }

    static MapLocation[] shuffleArr(MapLocation[] arr){
        Random rand = new Random();
        MapLocation[] copy = new MapLocation[arr.length];
        for(int i = 0; i < arr.length; i++){
            copy[i] = arr[i];
        }
        for (int i = 0; i < copy.length; i++) {
            int randomIndexToSwap = rand.nextInt(copy.length);
            MapLocation temp = copy[randomIndexToSwap];
            copy[randomIndexToSwap] = copy[i];
            copy[i] = temp;
        }
        return copy;
    }

    // Purpose of the +1 and -1 is to ensure that the integer version of a map location is never equal to 0 (useful for shared array)
    static int mapLocationToInt(MapLocation loc){
        return loc.x * 100 + loc.y + 1;
    }

    static MapLocation intToMapLocation(int num){
        return new MapLocation((num - 1) / 100, (num - 1) % 100);
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

    public static int minMovesToReach(MapLocation a, MapLocation b){
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        return Math.max(Math.abs(dx), Math.abs(dy));
    }

    public static int getArrayIndex(RobotType[] arr, RobotType value) {
        for(int i = 0; i < arr.length; i++){
            if(arr[i] == value){
                return i;
            }
        }
        return -1; // Should never reach this
    }

    public static int getArrayIndex(MapLocation[] arr, MapLocation value) {
        for(int i = 0; i < arr.length; i++){
            if(arr[i].equals(value)){
                return i;
            }
        }
        return -1; // Should never reach this
    }

    public static int getArrayIndex(int[] arr, int value) {
        for(int i = 0; i < arr.length; i++){
            if(arr[i] == value){
                return i;
            }
        }
        return -1; // Should never reach this
    }

    public static boolean checkRobotPresent(MapLocation loc, RobotType type, Team team) throws GameActionException {
        if(!rc.canSenseLocation(loc)){
            return false;
        }
        RobotInfo info = rc.senseRobotAtLocation(loc);
        if(info != null){
            if(info.getType() == type && info.getTeam() == team){
                return true;
            }
        }
        return false;
    }

    // reflection type: 1 = vertical reflection, 2 = horizontal reflection, 3 = 180 degree rotation
    public static MapLocation[] reflect(MapLocation[] locs, int reflectionType){
        MapLocation[] ret = new MapLocation[locs.length];
        for(int i = 0; i < locs.length; i++){
            if(reflectionType == 1){
                ret[i] = new MapLocation(locs[i].x, robot.mapHeight - locs[i].y - 1);
            }
            else if(reflectionType == 2){
                ret[i] = new MapLocation(robot.mapWidth - locs[i].x - 1, locs[i].y);
            }
            else if(reflectionType == 3){
                ret[i] = new MapLocation(robot.mapWidth - locs[i].x - 1, robot.mapHeight - locs[i].y - 1);
            }
            else{
                assert(false);
            }
        }
        return ret;
    }

    public static int countRobotTypes(RobotInfo[] infos, RobotType typ, Team team){
        int count = 0;
        for(int i = 0; i < infos.length; i++){
            if(infos[i].type == typ && infos[i].team == team){
                count++;
            }
        }
        return count;
    }

    public static int countRobotTypes(RobotInfo[] infos, RobotType type){
        int count = 0;
        for(int i = 0; i < infos.length; i++){
            if(infos[i].type == type){
                count++;
            }
        }
        return count;
    }

    public static int countRobotTypesWithinLocation(RobotInfo[] infos, RobotType typ, Team team, MapLocation center, int radiusSquared){
        int count = 0;
        for(int i = 0; i < infos.length; i++){
            if(infos[i].type == typ && infos[i].team == team && infos[i].getLocation().distanceSquaredTo(center) <= radiusSquared){
                count++;
            }
        }
        return count;
    }

    public static MapLocation calculateEnemySoldierCOM(RobotInfo[] nearby) throws GameActionException {
        int count = 0;
        int avgX = 0;
        int avgY = 0;
        for(int i = 0; i < nearby.length; i++){
            if(nearby[i].getType() != RobotType.SOLDIER || nearby[i].getTeam() != robot.myTeam.opponent()){
                continue;
            }
            avgX += nearby[i].getLocation().x;
            avgY += nearby[i].getLocation().y;
            count++;
        }
        if(count == 0){
            return robot.myLoc;
        }
        return new MapLocation(avgX / count, avgY / count);
    }

    public static MapLocation getLowestRubble(MapLocation[] locs) throws GameActionException {
        int lowest = Integer.MAX_VALUE;
        MapLocation lowestLoc = null;
        for(int i = 0; i < locs.length; i++){
            if(!rc.canSenseLocation(locs[i])){
                continue;
            }
            int rubble = rc.senseRubble(locs[i]);
            if(rubble < lowest){
                lowestLoc = locs[i];
            }
        }
        return lowestLoc;
    }

    public int directionToIndex(Direction dir){
        switch(dir){
            case NORTH:
                return 0;
            case NORTHEAST:
                return 1;
            case EAST:
                return 2;
            case SOUTHEAST:
                return 3;
            case SOUTH:
                return 4;
            case SOUTHWEST:
                return 5;
            case WEST:
                return 6;
            case NORTHWEST:
                return 7;
            default:
                return -1;
        }
    }

    public static Direction[] getDirectionsGoingTowards(Direction dir){
        return new Direction[]{dir, dir.rotateLeft(), dir.rotateRight()};
    }

    public static Direction[] closeDirections(Direction dir){
        Direction[] close = {
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateLeft(),
                dir,
                dir.rotateRight(),
                dir.rotateRight().rotateRight(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.opposite()
        };
        return close;
    }


}