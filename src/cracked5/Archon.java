package cracked5;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

class TempLocation {
    MapLocation loc;
    int idx;
    public TempLocation(MapLocation loc, int idx){
        this.loc = loc;
        this.idx = idx;
    }
}

// Miner types: 0 = explorer, 1 = cleanup crew

public class Archon extends Robot {

    int soldierCount = 0;
    int minerCount = 0;
    int builderCount = 0;
    int sageCount = 0;
    int mySoldiers = 0;
    int myMiners = 0;
    int myCleanupMiners = 0;
    int myBuilders = 0;
    int myCommsIdx = 0;
    int prevLead = 10000;
    MapLocation[] scoutingLocs;
    boolean spawnedMinerLastTurn = false;
    int savingUp = 0;
    MapLocation moveDestination = null;
    MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    Direction[] bestSpawnDirs;

    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        // Add current location to
        MapLocation currLoc = rc.getLocation();
        int locNum = Util.mapLocationToInt(currLoc);
        for(int i = 0; i < 4; i++){
            if(rc.readSharedArray(i) == 0){
                rc.writeSharedArray(i, locNum);
                myCommsIdx = i;
                break;
            }
        }
        scoutingLocs = getAllScoutingLocs();
    }

    public void run() throws GameActionException {
        super.run();
        minerCount = comms.getRobotCount(RobotType.MINER);
        soldierCount = comms.getRobotCount(RobotType.SOLDIER);
        builderCount = comms.getRobotCount(RobotType.BUILDER);

        indicatorString += rc.getMode().toString() + "; ";
        this.numFriendlyArchons = rc.getArchonCount();

        if(rc.getRoundNum() == 1){ // TODO: Might wanna do this based on map size. Might not be worth on smaller maps
            moveDestination = findLeastRubbleSpot(); // TODO Add heuristic to only move if lead is not a constraint or we have too many friendly soldiers around us (and moving to lower cool-down can benefit)
        }
        if(myLoc.distanceSquaredTo(moveDestination) > 0){ // if we are on the move to a lower rubble spot
            indicatorString += "MOVING; ";
            while(rc.getMode().equals(RobotMode.TURRET) && rc.canTransform()){
                rc.transform();
            }
            nav.goTo(moveDestination);
            comms.writeSharedArray(myCommsIdx, Util.mapLocationToInt(myLoc));
            bestSpawnDirs = null;
            //getBestSpawnDirs();
        }
        else {

            indicatorString += "STAYING; ";
            while(rc.getMode().equals(RobotMode.PORTABLE) && rc.canTransform()){    // make sure we are in turret mode
                indicatorString += "Transforming 2; ";
                rc.transform();
            }
            if(bestSpawnDirs == null){
                getBestSpawnDirs();
            }

            comms.findFriendlyArchons();
            RobotInfo[] enemiesInVision = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());
            MapLocation enemyCOM = Util.calculateEnemySoldierCOM(enemiesInVision);
            comms.updateCurrAttackLoc(enemiesInVision, enemyCOM);

            if (spawnedMinerLastTurn) {
                // TODO: Change this to spawn miner type based on miner #
                int minerType = 0; // Explore
                if (myMiners % 5 == 4) {
//                minerType = 1; // Go to biggest fight to cleanup
//                myCleanupMiners++;
                }
                comms.updateMinerInstruction(myCommsIdx, scoutingLocs[(myMiners - myCleanupMiners - 1) % scoutingLocs.length], minerType);
            }

            // Try building
            if (Util.mapLocationToInt(rc.getLocation()) == rc.readSharedArray(rc.getRoundNum() % this.numFriendlyArchons)) {
                // Build in a different direction than last time
                boolean defended = defendYourself();
                if (!defended) {
                    runBuildOrder();
                }
            }
            // Try repairing
            runRepair();
            prevLead = rc.getTeamLeadAmount(myTeam);
        }
    }

    public MapLocation findLeastRubbleSpot() throws GameActionException{
        // checks all spots in the archon's vision radius and returns spot with lowest rubble that is closest
        MapLocation bestSpot = null;
        int minRubble = Integer.MAX_VALUE;
        int distanceSquaredToBestSpot = Integer.MAX_VALUE;
        MapLocation[] visibleSpots = rc.getAllLocationsWithinRadiusSquared(myLoc, myType.visionRadiusSquared);

        for(int i = 0; i < visibleSpots.length; i++) {
            MapLocation potSpot = visibleSpots[i];
            if (rc.onTheMap(potSpot)) {     // is the potential spot on the map?
                int distanceSquaredToPotSpot = myLoc.distanceSquaredTo(potSpot);
                int rubbleAtPotSpot = rc.senseRubble(potSpot);
                if (rubbleAtPotSpot < minRubble || (rubbleAtPotSpot == minRubble && distanceSquaredToPotSpot < distanceSquaredToBestSpot)) {
                    bestSpot = potSpot;
                    distanceSquaredToBestSpot = distanceSquaredToPotSpot;
                    minRubble = rubbleAtPotSpot;
                }
            }
        }
        return bestSpot;
    }

    Comparator<Direction> compareByRubble  = new Comparator<Direction>() {
        public int compare(Direction d1, Direction d2) {
            try {           // hacky way to get past GameActionExceptions, which comparators aren't supposed to throw
                // check to see if either of the locations are null (might happen, based on how we defined the array
                MapLocation l1 = myLoc.add(d1);
                MapLocation l2 = myLoc.add(d2);
                int l1Rubble;
                int l2Rubble;
                if (!rc.onTheMap(l1)) {
                    l1Rubble = Integer.MAX_VALUE;
                }
                else {
                    l1Rubble = rc.senseRubble(l1);
                }
                if (!rc.onTheMap(l2)) {
                    l2Rubble = Integer.MAX_VALUE;
                }
                else {
                    l2Rubble = rc.senseRubble(l2);
                }
                if(l1Rubble < l2Rubble){      // l1 has less rubble
                    return -1;
                } else if (l2Rubble < l1Rubble) {    // l2 has less rubble
                    return 1;
                }

                else {                          // it's a tie, comparing distances to center
                    int l1DistanceToCenter;
                    int l2DistanceToCenter;
                    if(l1Rubble == Integer.MAX_VALUE){  // mapLocation was not on map, so doesn't make sense to compute distance
                        l1DistanceToCenter = Integer.MAX_VALUE;
                    }
                    else{
                        l1DistanceToCenter = l1.distanceSquaredTo(center);
                    }
                    if(l2Rubble == Integer.MAX_VALUE){  // mapLocation was not on map, so doesn't make sense to compute distance
                        l2DistanceToCenter = Integer.MAX_VALUE;
                    }
                    else{
                        l2DistanceToCenter = l2.distanceSquaredTo(center);
                    }
                    if(l1DistanceToCenter < l2DistanceToCenter){
                        return -1;
                    }
                    else if(l2DistanceToCenter < l1DistanceToCenter){
                        return 1;
                    }
                    else{
                        return 0;
                    }
                }
            } catch (GameActionException e) {  // hopefully, this should not happen
                System.out.println(e);
                return 0;
            }
        }
    };

    public void getBestSpawnDirs() throws GameActionException{
        bestSpawnDirs = Util.directions;
        Arrays.sort(bestSpawnDirs, compareByRubble);
    }

    // TODO: Figure out an optimal build order instead of overfitting to specific maps
    public void runBuildOrder() throws GameActionException {
        int lead = rc.getTeamLeadAmount(myTeam);
        int gold = rc.getTeamGoldAmount(myTeam);
        int soldierCost = RobotType.SOLDIER.buildCostLead;
        // If the current miners can build a soldier every round, then just build a soldier every round

        int leadDiff = lead - prevLead;

        if(gold >= RobotType.SAGE.buildCostGold){
            spawnUniformly(RobotType.SAGE, sageCount);
        }
        else if(builderCount * 50 + 50 < rc.getRoundNum() && builderCount < 1) {
            spawnUniformly(RobotType.BUILDER, builderCount);
        }
        else if(numFriendlyArchons > 0 && (lead - prevLead > soldierCost * numFriendlyArchons || lead / numFriendlyArchons > soldierCost * 10)){ // Also if you have a shitton of lead, just use it XD
            spawnUniformly(RobotType.SOLDIER, builderCount);
        }
        else if(rc.getRoundNum() < 30){
            spawnUniformly(RobotType.MINER, builderCount);
        }
        else if (soldierCount < minerCount * 1.5){
            spawnUniformly(RobotType.SOLDIER, builderCount);
        }
        else if(minerCount < soldierCount){
            spawnUniformly(RobotType.MINER, builderCount);
        }
        else{
            spawnUniformly(RobotType.SOLDIER, builderCount);
        }
    }

    public boolean defendYourself() throws GameActionException {
        // If you're building a soldier and you can see enemy soldier, spawn in direction of enemy
        MapLocation closest = null;
        int closestDist = 1000000;
        RobotInfo[] enemiesNearby = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());
        for(int i = enemiesNearby.length; i-- > 0; ){
            RobotInfo info = enemiesNearby[i];
            if(info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER){
                int dist = myLoc.distanceSquaredTo(nearby[i].location);
                if(dist < closestDist){
                    closest = nearby[i].location;
                    closestDist = dist;
                }
            }
        }
        if(closest == null){
            return false;
        }
        Direction dir = myLoc.directionTo(closest);
        Direction[] spawnDirs = Util.closeDirections(dir);
        if(Util.tryBuild(RobotType.SOLDIER, spawnDirs) != Direction.CENTER){
            Logger.Log("Successfully defended myself uwu");
            return true;
        }
        return false;
    }

    public void spawnUniformly(RobotType spawnType, int offset) throws GameActionException {
//        System.out.println("Trying to spawn: " + spawnType.toString());
        int leadUsable = rc.getTeamLeadAmount(myTeam) - comms.getLeadSaveUp() + savingUp;
        Direction[] defaultSpawnDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST, Direction.SOUTHEAST};
        Direction[] spawnDirs = Util.closeDirections(defaultSpawnDirs[offset % 8]);
        if(leadUsable < spawnType.buildCostLead){
//            System.out.println("Someone's saving up so im not gonna spawn this turn");
            return;
        }
        if(Util.tryBuild(spawnType, spawnDirs) != Direction.CENTER){
            comms.addRobotCount(spawnType, 1);
            if(spawnType == RobotType.MINER){
                Logger.Log("Successfully spawned a miner!");
                indicatorString += "Built a miner; ";
                myMiners++;
                spawnedMinerLastTurn = true;
            }
            else if(spawnType == RobotType.SOLDIER){
                Logger.Log("Successfully spawned a soldier!");
                indicatorString += "Built a soldier; ";
                mySoldiers++;
            }
            else if(spawnType == RobotType.BUILDER){
                Logger.Log("Successfully spawned a builder!");
                indicatorString += "Built a builder; ";
                myBuilders++;
            }
        }
    }

    public void runRepair() throws GameActionException {
        MapLocation toRepair = null;
        int bestRepairPriority = -100000;
        for(int i = 0; i < nearby.length; i++){
            if(!rc.canRepair(nearby[i].getLocation())){
                continue;
            }
            int repairPriority = 0;
            if(nearby[i].getType() == RobotType.SAGE){ // Prioritize repairing sages
                repairPriority += 10000;
            }
            if(nearby[i].getType() == RobotType.SOLDIER){ // Prioritize healing soldiers
                repairPriority += 100;
            }
//            repairPriority -= nearby[i].getHealth(); // Prioritize healing lower health ppl
            int maxHealth = nearby[i].getType().getMaxHealth(nearby[i].getLevel());
            int healthToFull = maxHealth - nearby[i].getHealth();
            if(healthToFull == 0){
                continue;
            }
            repairPriority -= healthToFull; // Prioritize healing lower health ppl
            if(repairPriority > bestRepairPriority){
                bestRepairPriority = repairPriority;
                toRepair = nearby[i].getLocation();
            }
        }
        if(toRepair == null){
            return;
        }
        if(rc.canRepair(toRepair)){
            rc.repair(toRepair);
            Logger.Log("Repairing: " + toRepair.toString());
            indicatorString += "Repairing: " + toRepair.toString() + "; ";
        }
    }

    public MapLocation[] getAllScoutingLocs(){
        int dx = 8;
        int numXLocs = mapWidth / dx + 1; // 8
        dx = mapWidth / numXLocs; // 7
        int dy = 8;
        int numYLocs = mapHeight / dy + 1; // 4
        dy = mapHeight / numYLocs; // 7

        System.out.println("Num X locs: " + numXLocs);
        System.out.println("Num Y locs: " + numYLocs);
        System.out.println("dx: " + dx);
        System.out.println("dy: " + dy);

        int count = 0;
        for(int x = dx / 2; x < mapWidth; x += dx){
            for(int y = dy / 2; y < mapHeight; y+= dy){
                count++;
            }
        }

        TempLocation[] locs = new TempLocation[count];
        int[] distances = new int[count];
        int idx = 0;
        for(int x = dx / 2; x < mapWidth; x += dx){
            for(int y = dy / 2; y < mapHeight; y+= dy){
                System.out.println(idx + ": " + x + ", " + y);
                locs[idx] = new TempLocation(new MapLocation(x, y), idx);
                distances[idx] = myLoc.distanceSquaredTo(locs[idx].loc);
                idx++;
            }
        }

        Arrays.sort(locs, new Comparator<TempLocation>() {
            @Override
            public int compare(TempLocation a, TempLocation b){
//            public int compare(int a, int b){
//                return distances[a.idx] - distances[b.idx];
                return distances[b.idx] - distances[a.idx];
//                return myLoc.distanceSquaredTo(b.loc) - myLoc.distanceSquaredTo(a.loc);
            }
        });

        MapLocation[] retLocs = new MapLocation[count];
        for(int i = 0; i < retLocs.length; i++){
            retLocs[i] = locs[i].loc;
        }

        return retLocs;
    }

}
