package sprintbot2;

import battlecode.common.*;

public class Comms {

    // Constants
    final int MAX_COMMS_VAL = 65535;
    final int SYMMETRY_IDX = 12;
    final int MINER_COUNT_IDX = 13;
    final int SOLDIER_COUNT_IDX = 14;
    final int SAGE_COUNT_IDX = 15;
    final int BUILDER_COUNT_IDX = 16;
    final int BIGGEST_THREAT_LEVEL_IDX = 17;
    final int BIGGEST_THREAT_LOC_IDX = 18;
    final int THREAT_THRESHOLD = 10;
    final int ARCHON_DEATH_OFFSET = 10000;

    final int LEAD_HOTSPOT_START_IDX = 38; // 63 - 25

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
        robot.friendlyArchons = new MapLocation[4];
        robot.numFriendlyArchons = 0;
        for(int i = 4; i-- > 0; ){
            int val = rc.readSharedArray(i);
            if(val != 0){
                if(robot.numFriendlyArchons == 0){
                    robot.numFriendlyArchons = i + 1;
                    robot.friendlyArchons = new MapLocation[robot.numFriendlyArchons];
                }
                this.robot.friendlyArchons[i] = Util.intToMapLocation(val);
            }
        }
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

        int radiusToCheck = 2;
        MapLocation[] visionLocs = rc.getAllLocationsWithinRadiusSquared(robot.myLoc, radiusToCheck); // 100 bytecode
        if(robot.prevLoc == null){
            for(int i = visionLocs.length; i-- > 0; ){
                MapLocation loc = visionLocs[i];
                robot.rubbleMap[loc.x][loc.y] = rc.senseRubble(loc) + 1;
            }
        }
        else{
            for(int i = visionLocs.length; i-- > 0; ){
                MapLocation loc = visionLocs[i];
                if(robot.rubbleMap[loc.x][loc.y] != 0){ // Already visited this location
                    continue;
                }
                int rubble = rc.senseRubble(loc) + 1;
                robot.rubbleMap[loc.x][loc.y] = rubble;
                if((symmetry & 1) != 0) { // Check if symmetry is followed
                    int symRubble = robot.rubbleMap[loc.x][robot.mapHeight - loc.y - 1];
                    if (symRubble != 0 && symRubble != rubble) {
                        symmetry &= 6;
                    }
                }
                if((symmetry & 2) != 0) { // Check if symmetry is followed
                    int symRubble = robot.rubbleMap[robot.mapWidth - loc.x - 1][loc.y];
                    if (symRubble != 0 && symRubble != rubble) {
                        symmetry &= 5;
                    }
                }
                if((symmetry & 4) != 0) { // Check if symmetry is followed
                    int symRubble = robot.rubbleMap[robot.mapWidth - loc.x - 1][robot.mapHeight - loc.y - 1];
                    if (symRubble != 0 && symRubble != rubble) {
                        symmetry &= 3;
                    }
                }
            }
        }

        writeSharedArray(SYMMETRY_IDX, symmetry);

    }

    public boolean checkEnemyArchonDied(MapLocation loc) throws GameActionException {
        for(int i = 8; i < 12; i++){
            int locInt = rc.readSharedArray(i);
            if(locInt >= ARCHON_DEATH_OFFSET){
                MapLocation diedLoc = Util.intToMapLocation(locInt - ARCHON_DEATH_OFFSET);
                if(diedLoc.distanceSquaredTo(loc) <= 9){ // If it died within that area, return true
                    return true;
                }
            }
        }
        return false;
    }

    // Update the locInt of an enemy archon when it dies. NOTE: Only works if archon has alr been detected and added to comms (which it should've been previously)
    public void updateEnemyArchonDeath(int archonID, MapLocation archonLoc) throws GameActionException {
        System.out.println("Updating archon death: " + archonID + ", " + archonLoc.toString());
        for(int i = 4; i < 8; i++){
            int id = rc.readSharedArray(i) - 1;
            if(id != archonID){
                continue;
            }
            int locInt = Util.mapLocationToInt(archonLoc) + ARCHON_DEATH_OFFSET;
            writeSharedArray(i + 4, locInt);
            return;
        }
    }

    // Search for any nearby enemy archons
    public void scanEnemyArchons() throws GameActionException {
        // Also update any enemy archon locs that you find to the comms array
        int[] enemyArchonIDs = {-1, -1, -1, -1};
        int[] enemyArchonLocs = new int[4];
        int known = 0;
        // Update existing enemy archon data w/ knowledge of what we can sense
        for(int i = 4; i < 8; i++){
            int id = rc.readSharedArray(i) - 1; // NOTE: ARCHON IDs ARE SAVED AS ID + 1 since 0 is a possible ID
            if(id == -1){
                break;
            }
            enemyArchonIDs[known] = id;
            int locInt = rc.readSharedArray(i + 4);
            if(rc.canSenseRobot(id)){
                locInt = Util.mapLocationToInt(rc.senseRobot(id).getLocation());
            }
            // if locInt is greater than 10000, that means that we have killed that enemy archon
            else if(rc.canSenseRobotAtLocation(Util.intToMapLocation(locInt)) && locInt < ARCHON_DEATH_OFFSET){ // If i can sense the location but not the robot, then robot must not be at that location
                // Enemy archon must have been destroyed or moved
                locInt = 0;
            }
            enemyArchonLocs[known] = locInt;
            known++;
        }

        // Search for new archons
        RobotInfo[] info = rc.senseNearbyRobots(rc.getLocation(), robot.myType.visionRadiusSquared, robot.myTeam.opponent());
        for(int i = 0; i < info.length; i++){
            if(info[i].getType() == RobotType.ARCHON && info[i].getTeam() == robot.myTeam.opponent()){
                int id = info[i].getID();
                if(Util.getArrayIndex(enemyArchonIDs, id) == -1){ // If you found a new enemy archon
                    int locInt = Util.mapLocationToInt(info[i].getLocation());
                    enemyArchonIDs[known] = id;
                    enemyArchonLocs[known] = locInt;
                    known++;
                }
            }
        }
        // Update shared array
        for(int i = 0; i < 4; i++){
            writeSharedArray(i + 4, enemyArchonIDs[i] + 1); // NOTE: ARCHON IDs ARE SAVED AS ID + 1 since 0 is a possible ID
            writeSharedArray(i + 8, enemyArchonLocs[i]);
        }
    }

    public void addRobotCount(RobotType type, int diff) throws GameActionException {
        int idx = robotTypeToIndex(type);
        int newVal = rc.readSharedArray(idx) + diff;
        if(newVal < 0){
            newVal = 0;
        }
        writeSharedArray(idx, newVal);
    }

    public int getRobotCount(RobotType type) throws GameActionException {
        return rc.readSharedArray(robotTypeToIndex(type));
    }

    public int robotTypeToIndex(RobotType type){
        switch(type){
            case MINER:
                return MINER_COUNT_IDX;
            case SOLDIER:
                return SOLDIER_COUNT_IDX;
            case SAGE:
                return SAGE_COUNT_IDX;
            case BUILDER:
                return BUILDER_COUNT_IDX;
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
        // Calculate current threat level
        int threatLevel = 0;
        MapLocation threatLoc = enemyCOM;
        int threatLocInt = Util.mapLocationToInt(threatLoc);
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
        if(rc.getType() == RobotType.ARCHON){ // Archons require double protection :)
            threatLevel *= 1.5;
        }

        Logger.Log("After calculating threat level: " + Clock.getBytecodesLeft());
        int currThreatLevel = rc.readSharedArray(BIGGEST_THREAT_LEVEL_IDX);
        int currThreatLocInt = rc.readSharedArray(BIGGEST_THREAT_LOC_IDX);
        MapLocation currThreatLoc = null;
        if(currThreatLocInt != 0 && currThreatLocInt != MAX_COMMS_VAL){
            currThreatLoc = Util.intToMapLocation(currThreatLocInt);
        }
        if(currThreatLoc == null){
            if(threatLevel < THREAT_THRESHOLD){ // Not above threshold, no biggie
                return;
            }
            writeSharedArray(BIGGEST_THREAT_LEVEL_IDX, threatLevel); // Call for reinforcements
            writeSharedArray(BIGGEST_THREAT_LOC_IDX, threatLocInt); // Call for reinforcements
            Logger.Log("Writing: " + threatLevel + ", " + threatLoc.toString());
        }
        else{
            if(currThreatLoc.distanceSquaredTo(threatLoc) <= 4){ // If you're near the current threat loc, update its value
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
                    writeSharedArray(BIGGEST_THREAT_LOC_IDX, threatLocInt); // Call for reinforcements
                    Logger.Log("Writing: " + threatLevel + ", " + threatLoc.toString());
                }
            }
        }
    }

    // Lead hotspot functions

    public MapLocation getBlockToExplore(){
        return null;
    }

    public MapLocation getBlockTopLeft(int x, int y){
        return new MapLocation(robot.comms_block_width * x, robot.comms_block_height * y);
    }

    public int getBlockWidth(int x){
        if(x == 4){
            return rc.getMapWidth() - robot.comms_block_width * 4;
        }
        return robot.comms_block_width;
    }

    public int getBlockHeight(int y){
        if(y == 4){
            return rc.getMapHeight() - robot.comms_block_height * 4;
        }
        return robot.comms_block_height;
    }

}