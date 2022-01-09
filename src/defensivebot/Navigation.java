package defensivebot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

//import java.util.*;

public class Navigation {

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

    static boolean isCardinal(Direction dir){
        for(Direction card : cardinalDirections){
            if(card.equals(dir)){
                return true;
            }
        }
        return false;
    }

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

    static Direction[] randomizedDirs(){
        return Util.shuffleArr(directions);
    }

    // Start of Navigation class
    RobotController rc;
    Robot robot;
    MapLocation currentTarget;
    int minDistToSatisfy = 0;
    HashSet<Integer> visited;

    public Navigation(RobotController rc, Robot robot){
        this.rc = rc;
        this.robot = robot;
        Util.rc = rc;
        Util.robot = robot;
        currentTarget = null;
        visited = new HashSet<Integer>();
    }

    public MapLocation getRandomMapLocation() throws GameActionException {
        int x = (int)(Math.random() * robot.mapWidth);
        int y = (int)(Math.random() * robot.mapHeight);
        return new MapLocation(x, y);
    }


    public void moveAwayFrom(MapLocation targetLoc) throws GameActionException {
        Direction dir = this.robot.myLoc.directionTo(targetLoc);
        Direction[] oppDirections = Navigation.closeDirections(dir.opposite());
        Util.tryMove(oppDirections);
    }

    public void moveTowards(MapLocation targetLoc) throws GameActionException {
        Direction dir = this.robot.myLoc.directionTo(targetLoc);
        Direction[] oppDirections = Navigation.closeDirections(dir);
        Util.tryMove(oppDirections);
    }

    public boolean goTo(MapLocation target) throws GameActionException {
        if (robot.myLoc.distanceSquaredTo(target) <= minDistToSatisfy) {
            return true;
        }
        rc.setIndicatorLine(robot.myLoc, target, 0, 255, 0);
        if (!rc.isMovementReady()) {
            return false;
        }
        rc.setIndicatorString("Going towards: " + target.toString());
        if(currentTarget != target){
            // Reset pathfinding vars
            currentTarget = target;
            visited.clear();
        }
        Direction toGo = fuzzynav(target);
        if (toGo == null) {
            return false;
        }
        if (Util.tryMove(toGo)) {
            goTo(target);
            return true;
        }
        return false;
    }

    public Direction fuzzynav (MapLocation target) throws GameActionException {
        Direction toTarget = robot.myLoc.directionTo(target);
        Direction[] moveOptions = {toTarget, toTarget.rotateLeft(), toTarget.rotateRight(), toTarget.rotateLeft().rotateLeft(), toTarget.rotateRight().rotateRight()};

        Direction bestDir = null;
        int bestCost = Integer.MAX_VALUE;

        for(int i = moveOptions.length; i-- > 0; ){
            Direction dir = moveOptions[i];
            MapLocation newLoc = robot.myLoc.add(dir);
            if(!rc.canSenseLocation(newLoc) || !rc.canMove(dir)){
                continue;
            }
            int cost = 10 + rc.senseRubble(newLoc);
            if(i == 3){
                MapLocation loc2 = newLoc.add(moveOptions[1]);
                if(!rc.canSenseLocation(loc2)){
                    continue;
                }
                cost += 10 + rc.senseRubble(loc2) + Util.minMovesToReach(loc2, target) * 10;
            }
            else if(i == 4){
                MapLocation loc2 = newLoc.add(moveOptions[2]);
                if(!rc.canSenseLocation(loc2)){
                    continue;
                }
                cost += 10 + rc.senseRubble(loc2) + Util.minMovesToReach(loc2, target) * 10;
            }
            else{
                cost += Util.minMovesToReach(newLoc, target) * 10;
            }

            if(cost < bestCost){
                bestCost = cost;
                bestDir = dir;
            }
        }

        return bestDir;
    }

    public void circle(MapLocation center, int minDist, boolean ccw) throws GameActionException {
        MapLocation myLoc = robot.myLoc;
        int dx = myLoc.x - center.x;
        int dy = myLoc.y - center.y;
        double cs = Math.cos(ccw ? 0.5 : -0.5);
        double sn = Math.sin(ccw ? 0.5 : -0.5);
        int x = (int) (dx * cs - dy * sn);
        int y = (int) (dx * sn + dy * cs);
        MapLocation target = center.translate(x, y);
//		goTo(target);
        Direction targetDir = myLoc.directionTo(target);
        Direction[] options = {targetDir, targetDir.rotateRight(), targetDir.rotateLeft(), targetDir.rotateRight().rotateRight(), targetDir.rotateLeft().rotateLeft()};
        Util.tryMove(options);
    }

}
