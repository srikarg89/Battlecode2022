package hopefullyiwin;

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

public class Archon extends Robot {

    int soldierCount = 0;
    int minerCount = 0;
    int builderCount = 0;
    int totalSoldierCount = 0;
    int totalMinerCount = 0;
    int sageCount = 0;
    int mySoldiers = 0;
    int myMiners = 0;
    int myBuilders = 0;
    int myCommsIdx = 0;
    int prevLead = 10000;
    boolean givingChance = false;
    MapLocation[] scoutingLocs;
    boolean spawnedMinerLastTurn = false;
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
        minerCount = comms.getAliveRobotCount(RobotType.MINER);
        soldierCount = comms.getAliveRobotCount(RobotType.SOLDIER);
        builderCount = comms.getAliveRobotCount(RobotType.BUILDER);
        totalSoldierCount = comms.getTotalRobotCount(RobotType.SOLDIER);
        totalMinerCount = comms.getTotalRobotCount(RobotType.MINER);

        indicatorString += rc.getMode().toString() + "; ";
        this.numFriendlyArchons = rc.getArchonCount();
//        rc.setIndicatorString(rc.getMode().toString());

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
        }

        else { // spawn troops
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
                int canExplore = 1;
                if(myMiners % 3 == 0){
                    canExplore = 0;
                }
                comms.updateMinerInstruction(myCommsIdx, scoutingLocs[(myMiners - 1) % scoutingLocs.length], canExplore);
            }
            // Try building
            if (myCommsIdx == rc.getRoundNum() % this.numFriendlyArchons) {
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

    // TODO: Figure out an optimal build order instead of overfitting to specific maps
    public void runBuildOrder() throws GameActionException {
        int lead = rc.getTeamLeadAmount(myTeam);
        int soldierCost = RobotType.SOLDIER.buildCostLead;
        // If the current miners can build a soldier every round, then just build a soldier every round

//        if(!givingChance && lead < soldierCost * rc.getArchonCount()){
//            if((totalSoldierCount != 0 && (double)mySoldiers / (double)totalSoldierCount > 1.3 / (double)rc.getArchonCount())){
//                givingChance = true;
//                return;
//            }
//            else if(totalMinerCount != 0 && (double)myMiners / (double)totalMinerCount > 1.3 / (double)rc.getArchonCount()){
//                givingChance = true;
//                return;
//            }
//        }
//        givingChance = false;

        int leadDiff = lead - prevLead;

        if(lead > 1500 && builderCount*30 < rc.getRoundNum() && builderCount < 4) {
//        if(false){
//            spawnTroop(RobotType.BUILDER);
            spawnUniformly(RobotType.BUILDER, builderCount);
//            spawnLowRubble(RobotType.BUILDER);
        }
        else if(numFriendlyArchons > 0 && (lead - prevLead > soldierCost * numFriendlyArchons || lead / numFriendlyArchons > soldierCost * 10)){ // Also if you have a shitton of lead, just use it XD
//            spawnTroop(RobotType.SOLDIER);
            spawnUniformly(RobotType.SOLDIER, builderCount);
//            spawnLowRubble(RobotType.SOLDIER);

        }
        else if(rc.getRoundNum() < 30){
//            spawnTroop(RobotType.MINER);
            spawnUniformly(RobotType.MINER, builderCount);
//            spawnLowRubble(RobotType.MINER);

        }
        else if (soldierCount < minerCount * 1.5){
//            spawnTroop(RobotType.SOLDIER);
            spawnUniformly(RobotType.SOLDIER, builderCount);
//            spawnLowRubble(RobotType.SOLDIER);

        }
        else if(minerCount < soldierCount){
//            spawnTroop(RobotType.MINER);
            spawnUniformly(RobotType.MINER, builderCount);
//            spawnLowRubble(RobotType.MINER);

        }
        else{
//            spawnTroop(RobotType.SOLDIER);
            spawnUniformly(RobotType.SOLDIER, builderCount);
//            spawnLowRubble(RobotType.SOLDIER);

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

    public void spawnTroop(RobotType spawnType) throws GameActionException {
        MapLocation map_center = new MapLocation(mapWidth / 2, mapHeight / 2);
        if(myLoc.equals(map_center)){ // Check just in case, shouldn't ever really occur
            map_center = myLoc.add(Direction.NORTHEAST);
        }

        Direction spawnDir = null;
        if(spawnType == RobotType.SOLDIER){
            MapLocation attackLoc = comms.getCurrAttackLoc();
            if(attackLoc != null){
                spawnDir = myLoc.directionTo(attackLoc);
            }
            if(spawnDir == null){
                MapLocation enemyArchonLoc = comms.getClosestEnemyArchonOnComms();
                if(enemyArchonLoc != null){
                    spawnDir = myLoc.directionTo(enemyArchonLoc);
                }
            }
        }
        else if(spawnType == RobotType.MINER){
            MapLocation closestMine = null;
            int closestDist = Integer.MAX_VALUE;
            // Find closest gold mine
            MapLocation[] goldMines = rc.senseNearbyLocationsWithGold();
            for(int i = 0; i < goldMines.length; i++){
                int dist = myLoc.distanceSquaredTo(goldMines[i]);
                if(dist < closestDist){
                    closestMine = goldMines[i];
                    closestDist = dist;
                }
            }
            if(closestMine != null){
                spawnDir = myLoc.directionTo(closestMine);
            }
            if(closestMine == null){
                // Find closest lead mine
                MapLocation[] leadMines = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared, 2);
                for(int i = 0; i < leadMines.length; i++){
                    int dist = myLoc.distanceSquaredTo(leadMines[i]);
                    if(dist < closestDist){
                        closestMine = leadMines[i];
                        closestDist = dist;
                    }
                }
                if(closestMine != null){
                    myLoc.directionTo(closestMine);
                }
            }
        }
        if(spawnDir == null || spawnDir == Direction.CENTER){
            spawnDir = myLoc.directionTo(map_center);
        }

        // Determine order to spawn in (depends on cooldown)

        Direction[] checkOrder = Util.closeDirections(spawnDir);
        int minCooldown = Integer.MAX_VALUE;
        int[] cooldowns = new int[checkOrder.length];
        for(int i = 0; i < checkOrder.length; i++){
            MapLocation checkLoc = myLoc.add(checkOrder[i]);
            if(!rc.canSenseLocation(checkLoc)){
                cooldowns[i] = Integer.MAX_VALUE;
                continue;
            }
            if(!rc.canBuildRobot(spawnType, checkOrder[i])){
                cooldowns[i] = Integer.MAX_VALUE;
                continue;
            }
            cooldowns[i] = rc.senseRubble(checkLoc);
            minCooldown = Math.min(minCooldown, cooldowns[i]);
        }
        if(minCooldown == Integer.MAX_VALUE){ // No available places to spawn troop
            return;
        }
        for(int i = 0; i < checkOrder.length; i++){
            if(cooldowns[i] == minCooldown){
                spawnRobot(spawnType, checkOrder[i]);
                return;
            }
        }

        // TODO: Instead of going in order, check where the current miners are and try to spawn in the direction opposite of the most miners
    }

    public void spawnRobot(RobotType spawnType, Direction dir) throws GameActionException {
        assert(rc.canBuildRobot(spawnType, dir));
        rc.buildRobot(spawnType, dir);
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

    public void spawnUniformly(RobotType spawnType, int offset) throws GameActionException {
        Direction[] defaultSpawnDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST, Direction.SOUTHEAST};
        Direction[] spawnDirs = Util.closeDirections(defaultSpawnDirs[offset % 8]);
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
        // TODO: Instead of going in order, check where the current miners are and try to spawn in the direction opposite of the most miners
        // TODO: Also look at the cooldown of the block ur spawning on
    }

    public void runRepair() throws GameActionException {
        MapLocation toRepair = null;
        int bestRepairPriority = -100000;
        for(int i = 0; i < nearbyFriendlies.length; i++){
            RobotInfo info = nearbyFriendlies[i];
            if(!rc.canRepair(info.getLocation())){
                continue;
            }
            if(info.getType().getMaxHealth(info.getLevel()) == info.getHealth()){
                continue; // Doesn't need repairing
            }
            int repairPriority = 0;
            if(info.getType() == RobotType.SAGE){ // Prioritize repairing sages
                repairPriority += 10000;
            }
            if(info.getType() == RobotType.SOLDIER){ // Prioritize healing soldiers
                repairPriority += 100;
            }
            repairPriority += nearby[i].getHealth(); // Prioritize healing higher health ppl (since they're closer to full and can get back into the action)
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
                return distances[a.idx] - distances[b.idx];
//                return myLoc.distanceSquaredTo(b.loc) - myLoc.distanceSquaredTo(a.loc);
            }
        });

        MapLocation[] retLocs = new MapLocation[count];
        for(int i = 0; i < retLocs.length; i++){
            retLocs[i] = locs[i].loc;
        }

        return retLocs;
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
            } catch (GameActionException e) {  // hopefullyiwinfully, this should not happen
                System.out.println(e);
                return 0;
            }
        }
    };


    public void getBestSpawnDirs() throws GameActionException{
        bestSpawnDirs = Util.directions;
        Arrays.sort(bestSpawnDirs, compareByRubble);
    }

    public void spawnLowRubble(RobotType spawnType) throws GameActionException {
        if(Util.tryBuild(spawnType, bestSpawnDirs) != Direction.CENTER){
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
        // TODO: Instead of going in order, check where the current miners are and try to spawn in the direction opposite of the most miners
        // TODO: Also look at the cooldown of the block ur spawning on
    }


}
