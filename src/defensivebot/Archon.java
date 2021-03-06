package defensivebot;

import battlecode.common.*;

public class Archon extends Robot {

    int soldierCount = 0;
    int minerCount = 0;
    int builderCount = 0;
    int sageCount = 0;
    int mySoldiers = 0;
    int myMiners = 0;
    int myBuilders = 0;
    int prevMinerCount = 0;
    int prevSoldierCount = 0;
    boolean givingChance = false;
    int id = 0;
    int prevLead = 10000;

    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        // Add current location to
        MapLocation currLoc = rc.getLocation();
        int locNum = Util.mapLocationToInt(currLoc);
        for(int i = 0; i < 4; i++){
            if(rc.readSharedArray(i) == 0){
                rc.writeSharedArray(i, locNum);
                id = i;
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
        this.comms.updateCenterOfAttackingMass(id);

        Logger.Log(myMiners + "");
        Logger.Log(mySoldiers + "");
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

    public void runBuildOrder() throws GameActionException {
        int lead = rc.getTeamLeadAmount(myTeam);
        int soldierCost = RobotType.SOLDIER.buildCostLead;
        // If the current miners can build a soldier every round, then just build a soldier every round

//
//        if(rc.getRoundNum()%10 < 7) spawnUniformly(RobotType.MINER, myMiners);
//        else spawnUniformly(RobotType.BUILDER, myBuilders);

        if(lead > 1500 && builderCount*30 < rc.getRoundNum() && builderCount < 4) {
//        if(false){
            spawnUniformly(RobotType.BUILDER, myBuilders);
        }
        else if(numFriendlyArchons > 0 && (lead - prevLead > soldierCost * numFriendlyArchons || lead / numFriendlyArchons > soldierCost * 10)){ // Also if you have a shitton of lead, just use it XD
            spawnUniformly(RobotType.SOLDIER, mySoldiers);
        }
        else if(rc.getRoundNum() < 30){
            spawnUniformly(RobotType.MINER, myMiners);
        }
        else if (soldierCount < minerCount * 2){
            spawnUniformly(RobotType.SOLDIER, mySoldiers);
        }
        else if(minerCount < soldierCount){
            spawnUniformly(RobotType.MINER, myMiners);
        }
        else{
            spawnUniformly(RobotType.SOLDIER, mySoldiers);
        }
    }

    public boolean defendYourself() throws GameActionException {
        // If you're building a soldier and you can see enemy soldier, spawn in direction of enemy
        MapLocation closest = null;
        int closestDist = 1000000;
        for(int i = nearby.length; i-- > 0; ){
            if(nearby[i].team == myTeam.opponent() && nearby[i].type == RobotType.SOLDIER){
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
        Direction[] spawnDirs = Navigation.closeDirections(dir);
        if(Util.tryBuild(RobotType.SOLDIER, spawnDirs) != Direction.CENTER){
            Logger.Log("Successfully defended myself uwu");
            return true;
        }
        return false;
    }

    public void spawnUniformly(RobotType spawnType, int offset) throws GameActionException {
        Direction[] defaultSpawnDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST, Direction.SOUTHEAST};
        Direction[] spawnDirs = new Direction[8];
        for(int i = 0; i < 8; i++){
            spawnDirs[i] = defaultSpawnDirs[(i + offset) % 8];
        }
        if(Util.tryBuild(spawnType, spawnDirs) != Direction.CENTER){
            comms.addRobotCount(spawnType, 1);
            if(spawnType == RobotType.MINER){
                Logger.Log("Successfully spawned a miner!");
                rc.setIndicatorString("Built a miner");
                myMiners++;
            }
            else if(spawnType == RobotType.SOLDIER){
                Logger.Log("Successfully spawned a soldier!");
                rc.setIndicatorString("Built a soldier");
                mySoldiers++;
            }
            else if(spawnType == RobotType.BUILDER){
                Logger.Log("Successfully spawned a builder!");
                rc.setIndicatorString("Built a builder");
                myBuilders++;
            }
        }
        // TODO: Instead of going in order, check where the current miners are and try to spawn in the direction opposite of the most miners
    }

    public void runRepair() throws GameActionException {
        MapLocation toRepair = null;
        int bestRepairPriority = -100000;
        for(int i = 0; i < nearby.length; i++){
            if(!rc.canRepair(nearby[i].getLocation())){
                continue;
            }
            int repairPriority = 0;
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
            Logger.Log("Repairing a soldier: " + toRepair.toString());
            rc.setIndicatorString("Repairing a soldier: " + toRepair.toString());
        }
    }


}
