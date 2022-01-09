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


//    public class Pair {
//        int x;
//        int y;
//        public Pair(int x, int y){
//            this.x = x;
//            this.y = y;
//        }
//    }

//    public Direction bellmanFord(MapLocation target) throws GameActionException {
//
//        Logger.Log("Starting bellman ford with " + Clock.getBytecodesLeft() + " bytecode");
//        int visionRadius = (int)Math.sqrt(robot.myType.visionRadiusSquared) + 1;
//        int[][] distances = new int[visionRadius][visionRadius];
//        MapLocation[][] locs = new MapLocation[visionRadius][visionRadius];
//        Pair[][] previous = new Pair[visionRadius][visionRadius];
//        Direction diag = robot.myLoc.directionTo(target);
//        // Assuming start is 0, 0 and target is n, n
//        Logger.Log("Bytecode Checkpoint A: " + Clock.getBytecodesLeft());
//        for(int i = 0; i < visionRadius; i++){ // This took 4000 bytecode T_T
//            for(int j = 0; j < visionRadius; j++){
//                MapLocation temp = Util.multiplyDirection(robot.myLoc, diag.rotateLeft(), i);
//                temp = Util.multiplyDirection(temp, diag.rotateRight(), j);
//                locs[i][j] = temp;
//                distances[i][j] = Integer.MAX_VALUE;
//            }
//        }
//
//        distances[0][0] = 0;
//        Logger.Log("Bytecode Checkpoint B: " + Clock.getBytecodesLeft());
//
//        for(int i = 0; i < visionRadius * visionRadius; i++){
//            for(int xy = 0; xy < visionRadius * 2 - 2; xy++){ // Go in a reasonable order
//                for(int x = 0; x <= xy; x++){
//                    // Calculate current location
//                    int y = xy - x;
//                    if(x >= visionRadius || y >= visionRadius){
//                        continue;
//                    }
//                    if(distances[x][y] == Integer.MAX_VALUE){
//                        continue;
//                    }
//                    MapLocation curr = locs[x][y];
//                    int dist = distances[x][y];
//                    for(int nbrX = x-1; nbrX <= x+1; nbrX++){ // Loop through all the neighbors
//                        for(int nbrY = y-1; nbrY <= y+1; nbrY++){
//                            if(x == nbrX && y == nbrY){
//                                continue;
//                            }
//                            if(nbrX < 0 || nbrY < 0 || nbrX >= visionRadius || nbrY >= visionRadius){
//                                continue;
//                            }
//                            MapLocation nbrLoc = locs[nbrX][nbrY];
//                            if(!rc.canSenseLocation(nbrLoc)){
//                                continue;
//                            }
//                            int cooldown = 10 + rc.senseRubble(nbrLoc); // Check if dist + cooldown is better than what it currently takes to get there
//                            if(cooldown + dist < distances[nbrX][nbrY]){
//                                distances[nbrX][nbrY] = dist + cooldown;
//                                previous[nbrX][nbrY] = new Pair(nbrX, nbrY);
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
//
//        Logger.Log("Bytecode Checkpoint C: " + Clock.getBytecodesLeft());
//        MapLocation reachableTarget = target;
//        if(!rc.canSenseLocation(reachableTarget)){
//            reachableTarget = Util.multiplyDirection(robot.myLoc, diag, visionRadius);
//        }
//        // Find reachableTarget in my array
//        Pair prev = null;
//        for(int i = 0; i < visionRadius; i++){
//            for(int j = 0; j < visionRadius; j++){
//                if(locs[i][j].equals(reachableTarget)){
//                    prev = new Pair(i, j);
//                }
//                Logger.Log("Found: (" + i + ", " + j + ") corresponding to " + locs[i][j].toString());
//            }
//        }
//        Logger.Log("Reachable target: " + reachableTarget.toString());
//        Logger.Log("Diagonal: " + diag.toString());
//        Logger.Log("Vision radius: " + visionRadius);
//        if(prev == null){
//            return Direction.CENTER;
//        }
//        assert(prev != null);
//        Pair next = null;
//        while(prev.x != 0 || prev.y != 0){
//            Logger.Log("Backtracking!");
//            next = prev;
//            prev = previous[prev.x][prev.y];
//        }
//        Logger.Log("Ended dijkstra with bytecode: " + Clock.getBytecodesLeft());
//        return robot.myLoc.directionTo(locs[next.x][next.y]);
//
//    }


//    class Node implements Comparable<Node> {
//        Integer locInt;
//        Integer distance;
//        Integer previous;
//
//        public Node(Integer a, Integer b) {
//            this.locInt = a;
//            this.distance = b;
//        }
//
//        @Override
//        public int compareTo(Node o) {
//            return distance - o.distance;
//        }
//    }

//    public Direction dijkstra2(MapLocation target) throws GameActionException {
//        Logger.Log("Starting dijkstra with bytecode: " + Clock.getBytecodesLeft());
//
//        Direction toTarget = robot.myLoc.directionTo(target);
//        Direction[] options = {toTarget, toTarget.rotateLeft(), toTarget.rotateRight(), toTarget.rotateLeft().rotateLeft(), toTarget.rotateRight().rotateRight()};
//
//        HashSet<Integer> visited = new HashSet<Integer>();
//        HashMap<Integer, Integer> distances = new HashMap<Integer, Integer>();
//        HashMap<Integer, Integer> previous = new HashMap<Integer, Integer>();
//
//        int start = Util.mapLocationToInt(robot.myLoc);
//        Queue<Node> queue = new PriorityQueue<Node>();
//        queue.add(new Node(start, 0));
//        while(!queue.isEmpty()){
//            Node curr = queue.poll();
//            if(visited.contains(curr.locInt)){
//                continue;
//            }
//            visited.add(curr.locInt);
//            if(curr.locInt != start){
//                previous.put(curr.locInt, curr.previous);
//            }
//            MapLocation currLoc = Util.intToMapLocation(curr.locInt);
////            distances.put(curr.locInt, curr.distance);
//            for(Direction dir : options){
//                MapLocation newLoc = currLoc.add(dir);
//                if(!rc.canSenseLocation(newLoc)){
//                    continue;
//                }
//                int newNum = Util.mapLocationToInt(newLoc);
//                int cooldown = 10 + rc.senseRubble(newLoc);
//                int newDist = curr.distance + cooldown;
//                Node newNode = new Node(newNum, newDist);
//                newNode.previous = curr.locInt;
//                queue.add(newNode);
//            }
//        }
//
//        MapLocation reachableTarget = target;
//        while(!rc.canSenseLocation(reachableTarget)){
//            reachableTarget = reachableTarget.add(reachableTarget.directionTo(robot.myLoc));
//        }
//        int next = Util.mapLocationToInt(reachableTarget);
//        int prev = next;
//        while(previous.containsKey(next)){
//            prev = next;
//            next = previous.get(next);
//        }
//        assert(next == start);
//        Logger.Log("Ended dijkstra with bytecode: " + Clock.getBytecodesLeft());
//        return robot.myLoc.directionTo(Util.intToMapLocation(prev));
//
//        // TODO: Test if this code even works XD
//    }



//    public Direction fuzzyNav(MapLocation target) throws GameActionException {
//
//        double[] distances = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
//        MapLocation myLoc = robot.myLoc;
//        Direction targetDir = myLoc.directionTo(target);
//        MapLocation[] testLocs = {myLoc.add(targetDir), myLoc.add(targetDir.rotateLeft()), myLoc.add(targetDir.rotateRight()),
//                myLoc.add(targetDir.rotateLeft().rotateLeft()).add(targetDir.rotateLeft()), myLoc.add(targetDir.rotateRight().rotateRight()).add(targetDir.rotateRight())};
//
//        Direction[] correspondingDirections = {targetDir, targetDir.rotateLeft(), targetDir.rotateRight(), targetDir.rotateLeft().rotateLeft(), targetDir.rotateRight().rotateRight()};
//        for (int i = 0; i < testLocs.length; i++) {
//            MapLocation testLoc = testLocs[i];
//            if (!rc.canSenseLocation(testLoc) || rc.isLocationOccupied(testLoc)) {
//                continue;
//            }
//            distances[i] = 1 / rc.sensePassability(testLoc);
//            if (i == 3) {
//                MapLocation testLoc2 = myLoc.add(targetDir.rotateLeft().rotateLeft());
//                if (!rc.canSenseLocation(testLoc2) || rc.isLocationOccupied(testLoc2)) {
//                    distances[i] = Double.MAX_VALUE;
//                    continue;
//                }
//                distances[i] += 1 / rc.sensePassability(testLoc2);
//            }
//            if (i == 4) {
//                MapLocation testLoc2 = myLoc.add(targetDir.rotateRight().rotateRight());
//                if (!rc.canSenseLocation(testLoc2) || rc.isLocationOccupied(testLoc2)) {
//                    distances[i] = Double.MAX_VALUE;
//                    continue;
//                }
//                distances[i] += 1 / rc.sensePassability(testLoc2);
//            }
//        }
//
//        // TODO: Don't revisit squares
//
//        double minVal = min(distances);
//        if (minVal == Double.MAX_VALUE) {
//            Log.log(" Ran into a massive obstacle, need to turn around!! ");
//            return null;
//        }
//
//        int minIdx = indexOf(distances, minVal);
//        return correspondingDirections[minIdx];
//    }

}
