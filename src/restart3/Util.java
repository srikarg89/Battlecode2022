package restart3;

import battlecode.common.*;

import java.util.Random;

public class Util {

    static RobotController rc;
    static Robot robot;

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

    static final Direction[] allDirections = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    static final Direction[] cardinalDirections = {
            Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH
    };

    static final Direction[] nonCardinalDirections = {
            Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST
    };

    static Direction[] closeDirections(Direction dir){
        Direction[] close = {
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.opposite()
        };
        return close;
    }

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
            if(nearby[i] == null){
                continue;
            }
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

    public static int getPotentialDamage(RobotInfo[] infos){
        int damage = 0;
        for(int i = infos.length; i-- > 0; ){
            RobotType type = infos[i].type;
            MapLocation loc = infos[i].location;
            // Soldiers can move and then attack in the same turn, so for them its two in one
            if(type == RobotType.SOLDIER && robot.myLoc.distanceSquaredTo(loc) <= RobotType.SOLDIER.visionRadiusSquared){
                damage += type.damage;
            }
            if(type == RobotType.WATCHTOWER && robot.myLoc.distanceSquaredTo(loc) <= RobotType.WATCHTOWER.actionRadiusSquared){
                damage += type.damage;
            }
        }
        return damage;
    }

    // NOTE: This only works for
    public static MapLocation[] getSenseableLocsAfterMoving(Direction dir){
        MapLocation[] checkLocs = new MapLocation[9];
        MapLocation myLoc = robot.myLoc;
        switch(dir){
            case NORTH:
                checkLocs[0] = new MapLocation(myLoc.x - 3, myLoc.y + 3);
                checkLocs[1] = new MapLocation(myLoc.x - 2, myLoc.y + 4);
                checkLocs[2] = new MapLocation(myLoc.x - 1, myLoc.y + 4);
                checkLocs[3] = new MapLocation(myLoc.x, myLoc.y + 4);
                checkLocs[4] = new MapLocation(myLoc.x + 1, myLoc.y + 4);
                checkLocs[5] = new MapLocation(myLoc.x + 2, myLoc.y + 4);
                checkLocs[6] = new MapLocation(myLoc.x + 3, myLoc.y + 3);
                return checkLocs;
            case SOUTH:
                checkLocs[0] = new MapLocation(myLoc.x - 3, myLoc.y - 3);
                checkLocs[1] = new MapLocation(myLoc.x - 2, myLoc.y - 4);
                checkLocs[2] = new MapLocation(myLoc.x - 1, myLoc.y - 4);
                checkLocs[3] = new MapLocation(myLoc.x, myLoc.y - 4);
                checkLocs[4] = new MapLocation(myLoc.x + 1, myLoc.y - 4);
                checkLocs[5] = new MapLocation(myLoc.x + 2, myLoc.y - 4);
                checkLocs[6] = new MapLocation(myLoc.x + 3, myLoc.y - 3);
                return checkLocs;
            case EAST:
                checkLocs[0] = new MapLocation(myLoc.x + 3, myLoc.y - 3);
                checkLocs[1] = new MapLocation(myLoc.x + 4, myLoc.y - 2);
                checkLocs[2] = new MapLocation(myLoc.x + 4, myLoc.y - 1);
                checkLocs[3] = new MapLocation(myLoc.x + 4, myLoc.y);
                checkLocs[4] = new MapLocation(myLoc.x + 4, myLoc.y + 1);
                checkLocs[5] = new MapLocation(myLoc.x + 4, myLoc.y + 2);
                checkLocs[6] = new MapLocation(myLoc.x + 3, myLoc.y + 3);
                return checkLocs;
            case WEST:
                checkLocs[0] = new MapLocation(myLoc.x - 3, myLoc.y - 3);
                checkLocs[1] = new MapLocation(myLoc.x - 4, myLoc.y - 2);
                checkLocs[2] = new MapLocation(myLoc.x - 4, myLoc.y - 1);
                checkLocs[3] = new MapLocation(myLoc.x - 4, myLoc.y);
                checkLocs[4] = new MapLocation(myLoc.x - 4, myLoc.y + 1);
                checkLocs[5] = new MapLocation(myLoc.x - 4, myLoc.y + 2);
                checkLocs[6] = new MapLocation(myLoc.x - 3, myLoc.y + 3);
                return checkLocs;
            case NORTHEAST:
                checkLocs[0] = new MapLocation(myLoc.x + 4, myLoc.y - 1);
                checkLocs[1] = new MapLocation(myLoc.x + 4, myLoc.y);
                checkLocs[2] = new MapLocation(myLoc.x + 4, myLoc.y + 1);
                checkLocs[3] = new MapLocation(myLoc.x + 4, myLoc.y + 2);
                checkLocs[4] = new MapLocation(myLoc.x + 3, myLoc.y + 3);
                checkLocs[5] = new MapLocation(myLoc.x + 2, myLoc.y + 4);
                checkLocs[6] = new MapLocation(myLoc.x + 1, myLoc.y + 4);
                checkLocs[7] = new MapLocation(myLoc.x, myLoc.y + 4);
                checkLocs[8] = new MapLocation(myLoc.x - 1, myLoc.y + 4);
                return checkLocs;
            case NORTHWEST:
                checkLocs[0] = new MapLocation(myLoc.x - 4, myLoc.y - 1);
                checkLocs[1] = new MapLocation(myLoc.x - 4, myLoc.y);
                checkLocs[2] = new MapLocation(myLoc.x - 4, myLoc.y + 1);
                checkLocs[3] = new MapLocation(myLoc.x - 4, myLoc.y + 2);
                checkLocs[4] = new MapLocation(myLoc.x - 3, myLoc.y + 3);
                checkLocs[5] = new MapLocation(myLoc.x - 2, myLoc.y + 4);
                checkLocs[6] = new MapLocation(myLoc.x - 1, myLoc.y + 4);
                checkLocs[7] = new MapLocation(myLoc.x, myLoc.y + 4);
                checkLocs[8] = new MapLocation(myLoc.x + 1, myLoc.y + 4);
                return checkLocs;
            case SOUTHEAST:
                checkLocs[0] = new MapLocation(myLoc.x + 4, myLoc.y + 1);
                checkLocs[1] = new MapLocation(myLoc.x + 4, myLoc.y);
                checkLocs[2] = new MapLocation(myLoc.x + 4, myLoc.y - 1);
                checkLocs[3] = new MapLocation(myLoc.x + 4, myLoc.y - 2);
                checkLocs[4] = new MapLocation(myLoc.x + 3, myLoc.y - 3);
                checkLocs[5] = new MapLocation(myLoc.x + 2, myLoc.y - 4);
                checkLocs[6] = new MapLocation(myLoc.x + 1, myLoc.y - 4);
                checkLocs[7] = new MapLocation(myLoc.x, myLoc.y - 4);
                checkLocs[8] = new MapLocation(myLoc.x - 1, myLoc.y - 4);
                return checkLocs;
            case SOUTHWEST:
                checkLocs[0] = new MapLocation(myLoc.x - 4, myLoc.y + 1);
                checkLocs[1] = new MapLocation(myLoc.x - 4, myLoc.y);
                checkLocs[2] = new MapLocation(myLoc.x - 4, myLoc.y - 1);
                checkLocs[3] = new MapLocation(myLoc.x - 4, myLoc.y - 2);
                checkLocs[4] = new MapLocation(myLoc.x - 3, myLoc.y - 3);
                checkLocs[5] = new MapLocation(myLoc.x - 2, myLoc.y - 4);
                checkLocs[6] = new MapLocation(myLoc.x - 1, myLoc.y - 4);
                checkLocs[7] = new MapLocation(myLoc.x, myLoc.y - 4);
                checkLocs[8] = new MapLocation(myLoc.x + 1, myLoc.y - 4);
                return checkLocs;
        }
        return checkLocs;
    }

    public static MapLocation getInitialMinerScoutLocation(int minerNumber) throws GameActionException {
        int minerVisionRadius = (int)Math.sqrt(RobotType.MINER.visionRadiusSquared);
        MapLocation myLoc = rc.getLocation();
        switch(minerNumber){
            case 0:
                return new MapLocation(0, 0);
            case 1:
                return new MapLocation(robot.mapWidth, robot.mapHeight);
            case 2:
                return new MapLocation(0, robot.mapHeight);
            case 3:
                return new MapLocation(robot.mapWidth, 0);
            case 4:
                return new MapLocation(0, robot.mapHeight / 2);
            case 5:
                return new MapLocation(robot.mapWidth / 2, 0);
            case 6:
                return new MapLocation(robot.mapWidth / 2, robot.mapHeight);
            case 7:
                return new MapLocation(robot.mapWidth, robot.mapHeight / 2);
            case 8:
                return new MapLocation(0, robot.mapHeight * 1 / 4);
            case 9:
                return new MapLocation(robot.mapWidth * 1 / 4, robot.mapHeight);
            case 10:
                return new MapLocation(robot.mapWidth * 3 / 4, 0);
            case 11:
                return new MapLocation(robot.mapWidth, robot.mapHeight * 3 / 4);
            case 12:
                return new MapLocation(robot.mapWidth * 1 / 4, 0);
            case 13:
                return new MapLocation(0, robot.mapHeight * 3 / 4);
            case 14:
                return new MapLocation(robot.mapWidth * 3 / 4, robot.mapHeight);
            case 15:
                return new MapLocation(robot.mapWidth, robot.mapHeight * 1 / 4);
        }
        return robot.nav.getRandomMapLocation();
    }

    static double cos(int angle){
        switch(angle){
            case 0:
                return 1.0;
            case 15:
                return 0.966;
            case 30:
                return 0.866;
            case 45:
                return 0.707;
            case 60:
                return 0.5;
            case 75:
                return 0.259;
            case 90:
                return 0.0;
            case 105:
                return -0.259;
            case 120:
                return -0.5;
            case 135:
                return -0.707;
            case 150:
                return -0.866;
            case 165:
                return -0.96;
            case 180:
                return -1.0;
            case 195:
                return -0.96;
            case 210:
                return -0.866;
            case 225:
                return -0.707;
            case 240:
                return -0.5;
            case 255:
                return -0.259;
            case 270:
                return 0.0;
            case 285:
                return 0.259;
            case 300:
                return 0.5;
            case 315:
                return 0.707;
            case 330:
                return 0.866;
            case 345:
                return 0.966;
        }
        assert(false);
        return Double.MAX_VALUE;
    }

    static double sin(int angle){
        switch(angle){
            case 0:
                return 0.0;
            case 15:
                return 0.259;
            case 30:
                return 0.5;
            case 45:
                return 0.707;
            case 60:
                return 0.866;
            case 75:
                return 0.966;
            case 90:
                return 1.0;
            case 105:
                return 0.966;
            case 120:
                return 0.866;
            case 135:
                return 0.707;
            case 150:
                return 0.5;
            case 165:
                return 0.259;
            case 180:
                return 0.0;
            case 195:
                return -0.259;
            case 210:
                return -0.5;
            case 225:
                return -0.707;
            case 240:
                return -0.866;
            case 255:
                return -0.96;
            case 270:
                return -1.0;
            case 285:
                return -0.96;
            case 300:
                return -0.866;
            case 315:
                return -0.707;
            case 330:
                return -0.5;
            case 345:
                return -0.259;
        }
        assert(false);
        return Double.MAX_VALUE;
    }

//    static double cos2(int angle){
//        if(angle == 90 || angle == 270){
//            return 0.0;
//        }
//        if(angle < 180){
//            return (90 - angle) / 90.0;
//        }
//        return (angle - 270) / 90.0;
//    }
//
//    static double sin2(int angle){
//        if(angle == 0 || angle == 180){
//            return 0.0;
//        }
//        if(angle < 90){
//            return angle / 90.0;
//        }
//        if(angle < 270){
//            return (180 - angle) / 90.0;
//        }
//        return (angle - 360) / 90.0;
//    }

    static double cos2(int angle){
        switch(angle){
            case 0:
                return 1.0;
            case 15:
                return 0.8333333333333334;
            case 30:
                return 0.6666666666666666;
            case 45:
                return 0.5;
            case 60:
                return 0.3333333333333333;
            case 75:
                return 0.16666666666666666;
            case 90:
                return 0.0;
            case 105:
                return -0.16666666666666666;
            case 120:
                return -0.3333333333333333;
            case 135:
                return -0.5;
            case 150:
                return -0.6666666666666666;
            case 165:
                return -0.8333333333333334;
            case 180:
                return -1.0;
            case 195:
                return -0.8333333333333334;
            case 210:
                return -0.6666666666666666;
            case 225:
                return -0.5;
            case 240:
                return -0.3333333333333333;
            case 255:
                return -0.16666666666666666;
            case 270:
                return 0.0;
            case 285:
                return 0.16666666666666666;
            case 300:
                return 0.3333333333333333;
            case 315:
                return 0.5;
            case 330:
                return 0.6666666666666666;
            case 345:
                return 0.8333333333333334;
        }
        assert(false);
        return Double.MAX_VALUE;
    }

    static double sin2(int angle){
        switch(angle){
            case 0:
                return 0.0;
            case 15:
                return 0.16666666666666666;
            case 30:
                return 0.3333333333333333;
            case 45:
                return 0.5;
            case 60:
                return 0.6666666666666666;
            case 75:
                return 0.8333333333333334;
            case 90:
                return 1.0;
            case 105:
                return 0.8333333333333334;
            case 120:
                return 0.6666666666666666;
            case 135:
                return 0.5;
            case 150:
                return 0.3333333333333333;
            case 165:
                return 0.16666666666666666;
            case 180:
                return 0.0;
            case 195:
                return -0.16666666666666666;
            case 210:
                return -0.3333333333333333;
            case 225:
                return -0.5;
            case 240:
                return -0.6666666666666666;
            case 255:
                return -0.8333333333333334;
            case 270:
                return -1.0;
            case 285:
                return -0.8333333333333334;
            case 300:
                return -0.6666666666666666;
            case 315:
                return -0.5;
            case 330:
                return -0.3333333333333333;
            case 345:
                return -0.16666666666666666;
        }
        assert(false);
        return Double.MAX_VALUE;
    }

    //    static double distanceToEdge(MapLocation loc, int angle) throws GameActionException {
    static double distanceToEdge(MapLocation loc, double cos_angle, double sin_angle) throws GameActionException {
        double distVert = Double.MAX_VALUE;
        double distHor = Double.MAX_VALUE;
        if(cos_angle > 0){ // Rightwards
            distHor = (robot.mapWidth - loc.x) / cos_angle;
        }
        else if(cos_angle < 0){ // Leftwards
            distHor = -loc.x / cos_angle;
        }

        if(sin_angle > 0){ // Upwards
            distVert = (robot.mapHeight - loc.y) / sin_angle;
        }
        else if(sin_angle < 0){ // Downwards
            distVert = -loc.y / sin_angle;
        }
        if(distHor < distVert){
            return distHor;
        }
        return distVert;

//        switch(angle){
//            case 0:
//                return loc.x;
//            case 15:
//                return Math.min(loc.x / 3.0, robot.mapHeight - loc.y) * robot.SQRT_4;
//            case 30:
//                return Math.min(loc.x / 2.0, robot.mapHeight - loc.y) * robot.SQRT_3;
//            case 45:
//                return Math.min(loc.x, robot.mapHeight - loc.y);
//            case 60:
//                return Math.min(loc.x, (robot.mapHeight - loc.y) / 2.0) * robot.SQRT_3;
//            case 75:
//                return Math.min(loc.x, (robot.mapHeight - loc.y) / 3.0) * robot.SQRT_4;
//            case 90:
//                return robot.mapHeight - loc.y;
//            case 105:
//                return Math.min(robot.mapWidth - loc.x, (robot.mapHeight - loc.y) / 3.0) * robot.SQRT_4;
//            case 120:
//                return Math.min(robot.mapWidth - loc.x, (robot.mapHeight - loc.y) / 2.0) * robot.SQRT_3;
//            case 135:
//                return Math.min(robot.mapWidth - loc.x, robot.mapHeight - loc.y);
//            case 150:
//                return Math.min((robot.mapWidth - loc.x) / 2.0, robot.mapHeight - loc.y) * robot.SQRT_3;
//            case 165:
//                return Math.min((robot.mapWidth - loc.x) / 3.0, robot.mapHeight - loc.y) * robot.SQRT_4;
//            case 180:
//                return robot.mapWidth - loc.x;
//            case 195:
//                return Math.min((robot.mapWidth - loc.x) / 3.0, loc.y) * robot.SQRT_4;
//            case 210:
//                return Math.min((robot.mapWidth - loc.x) / 2.0, loc.y) * robot.SQRT_3;
//            case 225:
//                return Math.min(robot.mapWidth - loc.x, loc.y);
//            case 240:
//                return Math.min(robot.mapWidth - loc.x, loc.y / 2.0) * robot.SQRT_3;
//            case 255:
//                return Math.min(robot.mapWidth - loc.x, loc.y / 3.0) * robot.SQRT_4;
//            case 270:
//                return loc.y;
//            case 285:
//                return Math.min(loc.x, loc.y / 3.0) * robot.SQRT_4;
//            case 300:
//                return Math.min(loc.x, loc.y / 2.0) * robot.SQRT_3;
//            case 315:
//                return Math.min(loc.x, loc.y);
//            case 330:
//                return Math.min(loc.x / 2.0, loc.y) * robot.SQRT_3;
//            case 345:
//                return Math.min(loc.x / 3.0, loc.y) * robot.SQRT_4;
//        }
//
//        throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Trying to find dist to unrecognized angle");
    }

    public static double mathRandom(){
        for(int i = 0; i < 10; i++){
            Math.random();
        }
        return Math.random();
    }

}