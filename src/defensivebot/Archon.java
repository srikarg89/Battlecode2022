package defensivebot;

import battlecode.common.*;

public class Archon extends Robot {

    int soldierCount = 0;
    int minerCount = 0;
    int builderCount = 0;
    int sageCount = 0;
    int mySoldiers = 0;
    int myMiners = 0;
    int prevMinerCount = 0;
    int prevSoldierCount = 0;
    boolean givingChance = false;
    int id = 0;

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

        comms.findFriendlyArchons();
        this.comms.updateCenterOfAttackingMass(id);


        Logger.Log(myMiners + "");
        Logger.Log(mySoldiers + "");
        if (Util.mapLocationToInt(rc.getLocation())== rc.readSharedArray(rc.getRoundNum()%this.numFriendlyArchons)) {
            // Build in a different direction than last time
//            if (myMiners <= mySoldiers * 0.75) {
//                // Let's try to build a miner.
//                rc.setIndicatorString("Trying to build a miner");
//                spawnUniformly(RobotType.MINER, minerCount);
//            } else {
//                // Let's try to build a soldier.
//                rc.setIndicatorString("Trying to build a soldier");
//                spawnUniformly(RobotType.SOLDIER, soldierCount);
//            }
            if(rc.getRoundNum() < 30){
                rc.setIndicatorString("Trying to build a miner");
                spawnUniformly(RobotType.MINER, minerCount);
            } else if (soldierCount < minerCount * 2){
                rc.setIndicatorString("Trying to build a soldier");
                spawnUniformly(RobotType.SOLDIER, soldierCount);
            }
            else if(minerCount < soldierCount){
                rc.setIndicatorString("Trying to build a miner");
                spawnUniformly(RobotType.MINER, minerCount);
            }
            else{
                rc.setIndicatorString("Trying to build a soldier");
                spawnUniformly(RobotType.SOLDIER, soldierCount);
            }
        }
    }

    public void spawnUniformly(RobotType spawnType, int offset) throws GameActionException {
        Direction[] defaultSpawnDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST, Direction.SOUTHEAST};
        Direction[] spawnDirs = new Direction[8];
        for(int i = 0; i < 8; i++){
            spawnDirs[i] = defaultSpawnDirs[(i + offset) % 8];
        }
        if(Util.tryBuild(spawnType, spawnDirs) != Direction.CENTER){
            if(spawnType == RobotType.MINER){
                Logger.Log("Successfully spawned a miner!");
                comms.addRobotCount(RobotType.MINER, 1);
                myMiners++;
            }
            else if(spawnType == RobotType.SOLDIER){
                Logger.Log("Successfully spawned a soldier!");
                comms.addRobotCount(RobotType.SOLDIER, 1);
                mySoldiers++;
            }
        }
        // TODO: Instead of going in order, check where the current miners are and try to spawn in the direction opposite of the most miners
    }


}
