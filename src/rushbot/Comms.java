package rushbot;

import battlecode.common.*;

import java.util.Map;
import java.util.Random;

public class Comms {

    // Constants
    final int MAX_COMMS_VAL = 65535;
    final int SYMMETRY_IDX = 12;
    final int MINER_COUNT_IDX = 13;
    final int SOLDIER_COUNT_IDX = 14;
    final int SAGE_COUNT_IDX = 15;
    final int BUILDER_COUNT_IDX = 16;

    // Properties
    RobotController rc;
    Robot robot;

    public Comms(RobotController rc, Robot robot){
        this.rc = rc;
        this.robot = robot;
    }

    public void findFriendlyArchons() throws GameActionException {
        int numArchons = 0;
        for(int i = 3; i >= 0; i--){
            int val = rc.readSharedArray(i);
            if(val != 0){
                if(numArchons == 0){
                    numArchons = i + 1;
                    this.robot.friendlyArchons = new MapLocation[numArchons];
                }
                this.robot.friendlyArchons[i] = Util.intToMapLocation(val);
            }
        }
        this.robot.numFriendlyArchons = numArchons;
    }

    // Symmetry stored as an integer, starting at 7 (111 in binary).
    // Third binary number: whether or not vertical symmetry is possible (0b001)
    // Second binary number: whether or not horizontal symmetry is possible (0b010)
    // First binary number: whether or not 180 degree rotational symmetry is possible (0b100)

    public void determineSymmetry() throws GameActionException {
        int symmetry = rc.readSharedArray(SYMMETRY_IDX); // Index for symmetry in shared array
        if(symmetry == 0){
            symmetry = 7;
        }
        if(symmetry == 1 || symmetry == 2 || symmetry == 4){ // Already determined symmetry
            return;
        }
        if((symmetry & 1) == 1){
            MapLocation[] enemyArchonLocs = Util.reflect(this.robot.friendlyArchons, 1);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                if(!rc.canSenseLocation(enemyArchonLocs[i])){
                    continue;
                }
                if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, robot.myTeam.opponent())){
                    symmetry &= 6;
                    break;
                }
            }
        }
        if((symmetry & 2) == 1){
            MapLocation[] enemyArchonLocs = Util.reflect(this.robot.friendlyArchons, 2);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                if(!rc.canSenseLocation(enemyArchonLocs[i])){
                    continue;
                }
                if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, robot.myTeam.opponent())){
                    symmetry &= 5;
                    break;
                }
            }
        }
        if((symmetry & 4) == 1){
            MapLocation[] enemyArchonLocs = Util.reflect(this.robot.friendlyArchons, 3);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                if(!rc.canSenseLocation(enemyArchonLocs[i])){
                    continue;
                }
                if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, robot.myTeam.opponent())){
                    symmetry &= 3;
                    break;
                }
            }
        }
        if(symmetry != rc.readSharedArray(SYMMETRY_IDX)){ // See if you determined a new symmetry
            rc.writeSharedArray(SYMMETRY_IDX, symmetry);
        }

    }

    // ALL ROBOTS SHOULD RUN THIS
    public void scanEnemyArchons() throws GameActionException {
        // Also update any enemy archon locs that you find to the comms array
        int[] enemyArchonIDs = new int[4];
        int[] enemyArchonLocs = new int[4];
        int known = 0;
        for(int i = 4; i < 8; i++){
            int id = rc.readSharedArray(i);
            if(id != 0){
                enemyArchonIDs[i - 4] = id;
                known++;
            }
            int locInt = rc.readSharedArray(i + 4);
            if(rc.canSenseRobot(id)){
                locInt = Util.mapLocationToInt(rc.senseRobot(id).getLocation());
            }
            enemyArchonLocs[i - 4] = locInt;
        }

        // Search for new archons
        RobotInfo[] info = rc.senseNearbyRobots();
        for(int i = 0; i < info.length; i++){
            if(info[i].getType() == RobotType.ARCHON && info[i].getTeam() == robot.myTeam.opponent()){
                int id = info[i].getID();
                int locInt = Util.mapLocationToInt(info[i].getLocation());
                if(Util.getArrayIndex(enemyArchonIDs, id) == -1){ // If you found a new enemy archon
                    enemyArchonIDs[known] = id;
                    enemyArchonLocs[known] = locInt;
                    known++;
                }
            }
        }
        // Update shared array
        for(int i = 0; i < 4; i++){
            if(enemyArchonIDs[i] != rc.readSharedArray(i + 4)){
                rc.writeSharedArray(i + 4, enemyArchonIDs[i]);
            }
            if(enemyArchonLocs[i] != rc.readSharedArray(i + 8)){
                rc.writeSharedArray(i + 8, enemyArchonLocs[i]);
            }
        }
    }

    public MapLocation searchForEnemyArchons() throws GameActionException {
        // Find the closest enemy we've detected
        MapLocation closestEnemy = null;
        int closestDist = Integer.MAX_VALUE;
        for(int i = 8; i < 12; i++){
            int val = rc.readSharedArray(i);
            if(val != 0 && val != MAX_COMMS_VAL){
                MapLocation enemyLoc = Util.intToMapLocation(val);
                int enemyDist = robot.myLoc.distanceSquaredTo(enemyLoc);
                if(enemyDist < closestDist){
                    closestDist = enemyDist;
                    closestEnemy = enemyLoc;
                }
            }
        }
        if(closestEnemy != null){
            return closestEnemy;
        }

        // Otherwise search for one based on symmetry
        int symmetry = rc.readSharedArray(SYMMETRY_IDX); // Index for symmetry in shared array
        if((symmetry & 1) == 1 || symmetry == 0){
            MapLocation[] enemyArchonLocs = Util.reflect(this.robot.friendlyArchons, 1);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                int dist = robot.myLoc.distanceSquaredTo(enemyArchonLocs[i]);
                if(dist < closestDist){
                    closestEnemy = enemyArchonLocs[i];
                    closestDist = dist;
                }
            }
        }
        if((symmetry & 2) == 1 || symmetry == 0){
            MapLocation[] enemyArchonLocs = Util.reflect(this.robot.friendlyArchons, 2);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                int dist = robot.myLoc.distanceSquaredTo(enemyArchonLocs[i]);
                if(dist < closestDist){
                    closestEnemy = enemyArchonLocs[i];
                    closestDist = dist;
                }
            }
        }
        if((symmetry & 4) == 1 || symmetry == 0){
            MapLocation[] enemyArchonLocs = Util.reflect(this.robot.friendlyArchons, 3);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                int dist = robot.myLoc.distanceSquaredTo(enemyArchonLocs[i]);
                if(dist < closestDist){
                    closestEnemy = enemyArchonLocs[i];
                    closestDist = dist;
                }
            }
        }

        return closestEnemy;
    }

    public void addRobotCount(RobotType type, int diff) throws GameActionException {
        if(type == RobotType.MINER){
            rc.writeSharedArray(MINER_COUNT_IDX, rc.readSharedArray(MINER_COUNT_IDX) + diff);
        }
        if(type == RobotType.SOLDIER){
            rc.writeSharedArray(SOLDIER_COUNT_IDX, rc.readSharedArray(SOLDIER_COUNT_IDX) + diff);
        }
        if(type == RobotType.SAGE){
            rc.writeSharedArray(SAGE_COUNT_IDX, rc.readSharedArray(SAGE_COUNT_IDX) + diff);
        }
        if(type == RobotType.BUILDER){
            rc.writeSharedArray(BUILDER_COUNT_IDX, rc.readSharedArray(BUILDER_COUNT_IDX) + diff);
        }
    }

    public int getRobotCount(RobotType type) throws GameActionException {
        if(type == RobotType.MINER){
            return rc.readSharedArray(MINER_COUNT_IDX);
        }
        if(type == RobotType.SOLDIER){
            return rc.readSharedArray(SOLDIER_COUNT_IDX);
        }
        if(type == RobotType.SAGE){
            return rc.readSharedArray(SAGE_COUNT_IDX);
        }
        if(type == RobotType.BUILDER){
            return rc.readSharedArray(BUILDER_COUNT_IDX);
        }
        return -1;
    }


}