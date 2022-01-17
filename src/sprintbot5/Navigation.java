package sprintbot5;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

// TODO BFS
public class Navigation {
    // Start of Navigation class
    RobotController rc;
    Robot robot;
    BFS20 bfs;
    MapLocation currentTarget;
    int minDistToSatisfy = 0;
    HashSet<Integer> visited;
    // Used in bugnav
    MapLocation[] lastVisited = new MapLocation[10];
    boolean goingLeft = true;

    public Navigation(RobotController rc, Robot robot){
        this.rc = rc;
        this.robot = robot;
        Util.rc = rc;
        Util.robot = robot;
        currentTarget = null;
        visited = new HashSet<Integer>();
        bfs = new BFS20(rc, robot);
    }

    public void update() throws GameActionException {
        if(!bfs.checkVarsReset()){
            bfs.resetVars();
        }
    }

    public MapLocation getRandomMapLocation() throws GameActionException {
        int x = (int)(Math.random() * robot.mapWidth);
        int y = (int)(Math.random() * robot.mapHeight);
        return new MapLocation(x, y);
    }

    public void moveAwayFrom(MapLocation targetLoc) throws GameActionException {
        Direction dir = robot.myLoc.directionTo(targetLoc);
        Direction[] oppDirections = Util.closeDirections(dir.opposite());
        Util.tryMove(oppDirections);
    }

    public void moveTowards(MapLocation targetLoc) throws GameActionException {
        Direction dir = robot.myLoc.directionTo(targetLoc);
        Direction[] oppDirections = Util.closeDirections(dir);
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
        if(currentTarget != target){
            // Reset pathfinding vars
            currentTarget = target;
            visited.clear();
        }
        Direction toGo;
        if(robot.age <= 3){
//        if(true){
            toGo = fuzzynav(target);
        }
        else{
            toGo = bfs.getBestDir(target);
        }
        if (toGo == null) {
            return false;
        }
        return Util.tryMove(toGo);
    }

    public Direction fuzzynav (MapLocation target) throws GameActionException {
        Direction toTarget = robot.myLoc.directionTo(target);
        Direction[] moveOptions = {toTarget, toTarget.rotateLeft(), toTarget.rotateRight(), toTarget.rotateLeft().rotateLeft(), toTarget.rotateRight().rotateRight()};

        Direction bestDir = null;
        int bestCost = Integer.MAX_VALUE;

//        if(robot.rc.getID() == 10886){
//            System.out.println("Current: " + robot.myLoc.toString() + ", Target: " + target.toString());
//        }

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

//            if(robot.rc.getID() == 10886){
//                System.out.println("Direction: " + dir.toString() + ", cost: " + cost);
//            }

            if(cost < bestCost){
                bestCost = cost;
                bestDir = dir;
            }
        }

        return bestDir;
    }

    // Tryna implement bug 0 algorithm in: https://eclass.upatras.gr/modules/document/file.php/CEID1207/%CE%92%CE%B9%CE%B2%CE%BB%CE%B9%CE%BF%CE%B3%CF%81%CE%B1%CF%86%CE%AF%CE%B1/788XF14L14.pathbugsmapsx.pdf
    public void bug0nav(MapLocation target) throws GameActionException {
        MapLocation prevLoc = robot.myLoc;
        Direction dir = robot.myLoc.directionTo(target);
        for(int i = 8; i-- > 0; ){
            MapLocation newLoc = robot.myLoc.add(dir);
            boolean seen = false;
            for(int j = 0; j < lastVisited.length; j++){
                if(lastVisited[j] != null && lastVisited[j].equals(newLoc)){ // Avoid the last 5 spots i alr went to (to avoid cycles)
                    seen = true;
                }
            }
            if(seen){
                continue;
            }
            if(Util.tryMove(dir)){
                for(int j = 0; j < lastVisited.length - 1; j++){
                    lastVisited[j] = lastVisited[j + 1];
                }
                lastVisited[lastVisited.length - 1] = prevLoc;
                if(i < 3){
                    goingLeft = !goingLeft; // Swapped sides aparently
                }
                return;
            }
            if(goingLeft){
                dir = dir.rotateLeft();
            }
            else{
                dir = dir.rotateRight();
            }
        }
        // Couldn't move anywhere, so j skip over the avoids and try moving wherever you can
        dir = robot.myLoc.directionTo(target);
        for(int i = 8; i-- > 0; ){
            if(Util.tryMove(dir)){
                for(int j = 0; j < lastVisited.length - 1; j++){
                    lastVisited[j] = lastVisited[j + 1];
                }
                lastVisited[lastVisited.length - 1] = prevLoc;
                if(i < 3){
                    goingLeft = !goingLeft; // Swapped sides aparently
                }
                return;
            }
            dir = dir.rotateLeft();
        }
    }

    public boolean moveTowardsSafe(MapLocation target) throws GameActionException {
        Direction dir = robot.myLoc.directionTo(target);
        Direction[] movePoss = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        int lowestRubble = rc.senseRubble(robot.myLoc) + 1;
        Direction bestDir = null;
        for(int i = 0; i < movePoss.length; i++){
            Direction testDir = movePoss[i];
            MapLocation newLoc = robot.myLoc.add(testDir);
            if(!rc.canSenseLocation(newLoc)){
                continue;
            }
            if(!rc.canMove(testDir)){
                continue;
            }
            int newRubble = rc.senseRubble(newLoc);
            if(newRubble < lowestRubble){
                lowestRubble = newRubble;
                bestDir = testDir;
            }
        }
        if(bestDir != null){ // Only move to an area w/ less rubble
            rc.move(bestDir);
            return true;
        }
        return false;
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
