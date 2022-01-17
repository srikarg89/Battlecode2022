package sprintbot5;

import battlecode.common.*;

public class Archon extends Robot {

    int soldierCount = 0;
    int minerCount = 0;
    int builderCount = 0;
    int sageCount = 0;
    int mySoldiers = 0;
    int myMiners = 0;
    int myBuilders = 0;
    int myCommsIdx = 0;
    int prevLead = 10000;

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
    }

    public void run() throws GameActionException {
        super.run();
        minerCount = comms.getRobotCount(RobotType.MINER);
        soldierCount = comms.getRobotCount(RobotType.SOLDIER);
        builderCount = comms.getRobotCount(RobotType.BUILDER);

        comms.findFriendlyArchons();
        RobotInfo[] enemiesInVision = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());
        MapLocation enemyCOM = Util.calculateEnemySoldierCOM(enemiesInVision);
        comms.updateCurrAttackLoc(enemiesInVision, enemyCOM);

//        System.out.println("My miners: " + minerCount);
//        System.out.println("My soldiers: " + soldierCount);
        // Try building
        if (Util.mapLocationToInt(rc.getLocation()) == rc.readSharedArray(rc.getRoundNum() % this.numFriendlyArchons)) {
            // Build in a different direction than last time
            boolean defended = defendYourself();
            if(!defended){
                runBuildOrder();
            }
        }
        // Try repairing
        runRepair();
        prevLead = rc.getTeamLeadAmount(myTeam);
    }

    // TODO: Figure out an optimal build order instead of overfitting to specific maps
    public void runBuildOrder() throws GameActionException {
        int lead = rc.getTeamLeadAmount(myTeam);
        int soldierCost = RobotType.SOLDIER.buildCostLead;
        // If the current miners can build a soldier every round, then just build a soldier every round

        int leadDiff = lead - prevLead;

        if(lead > 1500 && builderCount*30 < rc.getRoundNum() && builderCount < 4) {
//        if(false){
//            spawnTroop(RobotType.BUILDER);
            spawnUniformly(RobotType.BUILDER, builderCount);
        }
        else if(numFriendlyArchons > 0 && (lead - prevLead > soldierCost * numFriendlyArchons || lead / numFriendlyArchons > soldierCost * 10)){ // Also if you have a shitton of lead, just use it XD
//            spawnTroop(RobotType.SOLDIER);
            spawnUniformly(RobotType.SOLDIER, builderCount);
        }
        else if(rc.getRoundNum() < 30){
//            spawnTroop(RobotType.MINER);
            spawnUniformly(RobotType.MINER, builderCount);
        }
        else if (soldierCount < minerCount * 1.5){
//            spawnTroop(RobotType.SOLDIER);
            spawnUniformly(RobotType.SOLDIER, builderCount);
        }
        else if(minerCount < soldierCount){
//            spawnTroop(RobotType.MINER);
            spawnUniformly(RobotType.MINER, builderCount);
        }
        else{
//            spawnTroop(RobotType.SOLDIER);
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
        for(int i = 0; i < nearby.length; i++){
            if(!rc.canRepair(nearby[i].getLocation())){
                continue;
            }
            int repairPriority = 0;
            if(nearby[i].getType() == RobotType.WATCHTOWER){ // Prioritize repairing watchtowers
                repairPriority += 10000;
            }
            if(nearby[i].getType() == RobotType.SOLDIER){ // Prioritize healing soldiers
                repairPriority += 100;
            }
            repairPriority -= nearby[i].getHealth(); // Prioritize healing lower health ppl
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


}
