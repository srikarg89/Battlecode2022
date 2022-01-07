package resourceplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import gnu.trove.impl.sync.TSynchronizedShortObjectMap;

public class Archon extends Robot {

    int soldierCount = 0;
    int minerCount = 0;

    public Archon(RobotController rc){
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // Build in a different direction than last time
//        if (minerCount <= soldierCount) {
        if(minerCount < 6 || minerCount < soldierCount * 4){
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            spawnUniformly(RobotType.MINER, minerCount);
        } else {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            spawnUniformly(RobotType.SOLDIER, soldierCount);
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
                System.out.println("Successfully spawned a miner!");
                minerCount++;
            }
            else if(spawnType == RobotType.SOLDIER){
                System.out.println("Successfully spawned a soldier!");
                soldierCount++;
            }
        }
        // TODO: Instead of going in order, check where the current miners are and try to spawn in the direction opposite of the most miners
    }



}
