package sprintbot;

import battlecode.common.*;

public class Comms {

    // Constants
    final int MAX_COMMS_VAL = 65535;
    final int SYMMETRY_IDX = 12;
    final int MINER_COUNT_IDX = 13;
    final int SOLDIER_COUNT_IDX = 14;
    final int SAGE_COUNT_IDX = 15;
    final int BUILDER_COUNT_IDX = 16;
    final int CENTER_OF_ATTACKING_MASS_IDX = 25;
    final int BIGGEST_THREAT_LEVEL_IDX = 40;
    final int BIGGEST_THREAT_LOC_IDX = 41;
    final int THREAT_THRESHOLD = 10;
    final int HELP_REQUEST_START_IDX = 50;
    final int HELP_REQUEST_END_IDX = 55;

    // Properties
    RobotController rc;
    Robot robot;

    public Comms(RobotController rc, Robot robot){
        this.rc = rc;
        this.robot = robot;
    }

    public void writeSharedArray(int idx, int value) throws GameActionException {
        int currVal = rc.readSharedArray(idx);
        if(value != currVal){
            rc.writeSharedArray(idx, value);
        }
    }

    public void findFriendlyArchons() throws GameActionException {
        int numArchons = 0;
        for(int i = 4; i-- > 0; ){
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
        int[] binVals = {1, 2, 4};
        for(int j = 0; j < 3; j++){
            int reflectionType = j + 1;
            int binVal = binVals[j];
            if((symmetry & binVal) != 0){
                MapLocation[] enemyArchonLocs = Util.reflect(this.robot.friendlyArchons, reflectionType);
                for(int i = 0; i < enemyArchonLocs.length; i++){
                    if(!rc.canSenseLocation(enemyArchonLocs[i])){
                        continue;
                    }
                    if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, robot.myTeam.opponent())){
                        symmetry &= (7 ^ binVal);
                        break;
                    }
                }
            }
        }
        if(symmetry != rc.readSharedArray(SYMMETRY_IDX)){ // See if you determined a new symmetry
            Logger.Log("CHANGING SYMMETRY FROM: " + rc.readSharedArray(SYMMETRY_IDX) + " TO " + symmetry);
            writeSharedArray(SYMMETRY_IDX, symmetry);
        }

    }

    // ALL ROBOTS SHOULD RUN THIS
    public void scanEnemyArchons() throws GameActionException {
        // Also update any enemy archon locs that you find to the comms array
        int[] enemyArchonIDs = new int[4];
        enemyArchonIDs[0] = -1;
        enemyArchonIDs[1] = -1;
        enemyArchonIDs[2] = -1;
        enemyArchonIDs[3] = -1;
        int[] enemyArchonLocs = new int[4];
        int known = 0;
        for(int i = 4; i < 8; i++){
            int id = rc.readSharedArray(i) - 1; // NOTE: ARCHON IDs ARE SAVED AS ID + 1 since 0 is a possible ID
            if(id != -1){
                enemyArchonIDs[i - 4] = id;
                known++;
            }
            int locInt = rc.readSharedArray(i + 4);
            if(locInt != 0 && locInt != Integer.MAX_VALUE){
                MapLocation loc = Util.intToMapLocation(locInt);
                if(rc.canSenseLocation(loc)){
                    RobotInfo info = rc.senseRobotAtLocation(loc);
                    if(info == null || info.getType() != RobotType.ARCHON || info.getTeam() != robot.myTeam.opponent()){
                        // Archon was destroyed
                        locInt = 0;
                    }
                }
            }
            if(rc.canSenseRobot(id)){
                locInt = Util.mapLocationToInt(rc.senseRobot(id).getLocation());
            }
            enemyArchonLocs[i - 4] = locInt;
        }

        // Search for new archons
        RobotInfo[] info = rc.senseNearbyRobots(rc.getLocation(), robot.myType.visionRadiusSquared, robot.myTeam.opponent());
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
            if(enemyArchonIDs[i] != rc.readSharedArray(i + 4) - 1){ // NOTE: ARCHON IDs ARE SAVED AS ID + 1 since 0 is a possible ID
                writeSharedArray(i + 4, enemyArchonIDs[i] + 1); // NOTE: ARCHON IDs ARE SAVED AS ID + 1 since 0 is a possible ID
            }
            if(enemyArchonLocs[i] != rc.readSharedArray(i + 8)){
                writeSharedArray(i + 8, enemyArchonLocs[i]);
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
            writeSharedArray(MINER_COUNT_IDX, rc.readSharedArray(MINER_COUNT_IDX) + diff);
        }
        if(type == RobotType.SOLDIER){
            writeSharedArray(SOLDIER_COUNT_IDX, rc.readSharedArray(SOLDIER_COUNT_IDX) + diff);
        }
        if(type == RobotType.SAGE){
            writeSharedArray(SAGE_COUNT_IDX, rc.readSharedArray(SAGE_COUNT_IDX) + diff);
        }
        if(type == RobotType.BUILDER){
            writeSharedArray(BUILDER_COUNT_IDX, rc.readSharedArray(BUILDER_COUNT_IDX) + diff);
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

    public int getClosestFriendlyArchonIndex() throws GameActionException {
        int minDistanceSquared = Integer.MAX_VALUE;
        int index = 0;
        for(int i=4; i-- > 0; ) {
            int compressed_coordinate = rc.readSharedArray(i);
            if (compressed_coordinate != 0) {
                MapLocation currMapLocation = Util.intToMapLocation(rc.readSharedArray(i));
                int currDistanceSquared = rc.getLocation().distanceSquaredTo(currMapLocation);
                if(currDistanceSquared < minDistanceSquared){
                    index = i;
                    minDistanceSquared = currDistanceSquared;
                }
            }
        }
        return index;
    }

    public MapLocation getCurrAttackLoc() throws GameActionException {
        int currThreatLevel = rc.readSharedArray(BIGGEST_THREAT_LEVEL_IDX);
        int currThreatLocInt = rc.readSharedArray(BIGGEST_THREAT_LOC_IDX);
        Logger.Log("Curr Threat level: " + currThreatLevel);
        Logger.Log("Curr Threat Loc: " + currThreatLocInt);
        if(currThreatLocInt != 0 && currThreatLocInt != MAX_COMMS_VAL){
            return Util.intToMapLocation(currThreatLocInt);
        }
        else{
            return null;
        }
    }

    public void updateCurrAttackLoc(RobotInfo[] enemiesInVision, MapLocation enemyCOM) throws GameActionException {
        int threatLevel = 0;
        MapLocation threatLoc = enemyCOM;
        Logger.Log("After calculating COM: " + Clock.getBytecodesLeft());
        for(int i = 0; i < enemiesInVision.length; i++){
            RobotInfo info = enemiesInVision[i];
            if(info.getType() == RobotType.ARCHON){
                threatLevel += 50;
                threatLoc = info.getLocation();
            }
            else if(info.getType() == RobotType.SOLDIER){
                threatLevel += 5;
            }
        }
        if(rc.getType() == RobotType.ARCHON){
            threatLevel *= 2;
        }
        Logger.Log("After calculating threat level: " + Clock.getBytecodesLeft());
        int currThreatLevel = rc.readSharedArray(BIGGEST_THREAT_LEVEL_IDX);
        int currThreatLocInt = rc.readSharedArray(BIGGEST_THREAT_LOC_IDX);
        MapLocation currThreatLoc = null;
        if(currThreatLocInt != 0 && currThreatLocInt != MAX_COMMS_VAL){
            currThreatLoc = Util.intToMapLocation(currThreatLocInt);
        }
        Logger.Log("Previous threat level: " + currThreatLevel);
        Logger.Log("My threat level: " + threatLevel);
        if(currThreatLoc == null){
            if(threatLevel < THREAT_THRESHOLD){ // Not above threshold, no biggie
                return;
            }
            writeSharedArray(BIGGEST_THREAT_LEVEL_IDX, threatLevel); // Call for reinforcements
            writeSharedArray(BIGGEST_THREAT_LOC_IDX, Util.mapLocationToInt(threatLoc)); // Call for reinforcements
            Logger.Log("Writing: " + threatLevel + ", " + Util.mapLocationToInt(threatLoc));
        }
        else if(currThreatLoc != null){
            if(currThreatLoc.distanceSquaredTo(threatLoc) < 4){ // If you're near the current threat loc, update its value
                int threatLocInt = Util.mapLocationToInt(threatLoc);
                if(threatLevel < THREAT_THRESHOLD){ // Update threshold value
                    threatLevel = 0;
                    threatLocInt = 0;
                }
                writeSharedArray(BIGGEST_THREAT_LEVEL_IDX, threatLevel); // Call for reinforcements
                writeSharedArray(BIGGEST_THREAT_LOC_IDX, threatLocInt); // Call for reinforcements
                Logger.Log("Writing: " + threatLevel + ", " + threatLocInt);
            }
            else{ // New location, check if its more threatened than the old loc
                if(threatLevel > THREAT_THRESHOLD && threatLevel > currThreatLevel){ // Update threshold value
                    writeSharedArray(BIGGEST_THREAT_LEVEL_IDX, threatLevel); // Call for reinforcements
                    writeSharedArray(BIGGEST_THREAT_LOC_IDX, Util.mapLocationToInt(threatLoc)); // Call for reinforcements
                    Logger.Log("Writing: " + threatLevel + ", " + Util.mapLocationToInt(threatLoc));
                }
            }
        }
    }

    // TODO: Figure out how to use this properly
    public void requestHelp(RobotInfo[] nearby) throws GameActionException {
        int numEnemies = 0;
        for(int i = 0; i < nearby.length; i++){
            if(nearby[i].getTeam() == robot.myTeam.opponent()){
                if(nearby[i].getType() == RobotType.SOLDIER){
                    numEnemies++;
                }
                if(nearby[i].getType() == RobotType.ARCHON){
                    numEnemies += 5;
                }
            }
        }
        // Replace lowest spot in comms
        int myNum = numEnemies * 1000 + Util.mapLocationToInt(robot.myLoc);
        int lowest = Integer.MAX_VALUE;
        int lowestIdx = -1;
        for(int i = HELP_REQUEST_START_IDX; i <= HELP_REQUEST_END_IDX; i++){
            int val = rc.readSharedArray(i);
            if(val == 0){
                writeSharedArray(i, myNum);
                return;
            }
            else{
                if(val < lowest){
                    lowest = val;
                    lowestIdx = i;
                }
            }
        }
        if(lowest >= myNum){
            return;
        }
        writeSharedArray(lowestIdx, myNum);

    }

}