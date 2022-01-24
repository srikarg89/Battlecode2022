package cracked5;

import battlecode.common.*;

public class Comms {

    // Constants
    final int THREAT_THRESHOLD = 10;
    final int MAX_COMMS_VAL = 65535;
    final int ARCHON_DEATH_OFFSET = 10000;
    final int SYMMETRY_IDX = 12;
    final int MINER_COUNT_IDX = 13;
    final int SOLDIER_COUNT_IDX = 14;
    final int SAGE_COUNT_IDX = 15;
    final int BUILDER_COUNT_IDX = 16;
    final int LABORATORY_COUNT_IDX = 17;
    final int BIGGEST_THREAT_LEVEL_IDX_START = 18;
    final int BIGGEST_THREAT_LOC_IDX_START = 19;

    final int BIGGEST_THREAT_LEVEL_IDX_END = 18;
    final int BIGGEST_THREAT_LOC_IDX_END = 19;
    final int MINER_INSTRUCTION_START_IDX = 20; // 20-23
    final int ARCHON_HEALING_START_IDX = 24; // 24-27
    final int LEAD_SAVE_UP_IDX = 28;

//    final int BIGGEST_THREAT_LEVEL_IDX_END = 25;
//    final int BIGGEST_THREAT_LOC_IDX_END = 26;
//    final int MINER_INSTRUCTION_START_IDX = 27; // 19-23
    // Max of 100 lead things present in a given location

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
//        if(symmetry == 1 || symmetry == 2 || symmetry == 4){ // Already determined symmetry
//            return;
//        }
        int[] binVals = {1, 2, 4};
        for(int j = 0; j < 3; j++){
            int reflectionType = j + 1;
            int binVal = binVals[j];
            if((symmetry & binVal) != 0){
                MapLocation[] enemyArchonLocs = Util.reflect(robot.friendlyArchons, reflectionType);
                for(int i = 0; i < enemyArchonLocs.length; i++){
                    if(!rc.canSenseLocation(enemyArchonLocs[i])){
                        continue;
                    }
                    if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, robot.myTeam.opponent())){
                        if(!checkEnemyArchonDied(enemyArchonLocs[i])){
                            symmetry &= (7 ^ binVal);
                            System.out.println("Updating symmetry! New symmetry is: " + symmetry);
                            break;
                        }
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

//        System.out.println("Middle of scanning archons: " + Clock.getBytecodesLeft());

        // Search for new archons
        RobotInfo[] info = robot.nearbyEnemies;
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

//        System.out.println("End of scanning archons: " + Clock.getBytecodesLeft());

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
            case LABORATORY:
                return LABORATORY_COUNT_IDX;
        }
        return -1;
    }

    public int getClosestFriendlyArchonIndex() throws GameActionException {
        int minDistanceSquared = Integer.MAX_VALUE;
        int index = -1;
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

    public MapLocation getClosestEnemyArchonOnComms() throws GameActionException {
        MapLocation closestEnemy = null;
        int closestDist = Integer.MAX_VALUE;
        for(int i = 8; i < 12; i++){
            int val = rc.readSharedArray(i);
            if(val != 0 && val < ARCHON_DEATH_OFFSET){
                MapLocation enemyLoc = Util.intToMapLocation(val);
                int enemyDist = robot.myLoc.distanceSquaredTo(enemyLoc);
                if(enemyDist < closestDist){
                    closestDist = enemyDist;
                    closestEnemy = enemyLoc;
                }
            }
        }
        return closestEnemy;
    }

    public MapLocation getCurrAttackLoc() throws GameActionException {
        MapLocation potentialLoc = null;
        double bestHeuristic = 0.0;
        for(int i = BIGGEST_THREAT_LOC_IDX_START; i <= BIGGEST_THREAT_LOC_IDX_END; i += 2){
            int currThreatLocInt = rc.readSharedArray(i);
            if(currThreatLocInt != 0 && currThreatLocInt != MAX_COMMS_VAL){
//                System.out.println("CURR LEVEL: " + rc.readSharedArray(i - 1));
//                System.out.println("CURR LOC INT: " + currThreatLocInt);
                MapLocation loc = Util.intToMapLocation(currThreatLocInt);
                int threat_level = rc.readSharedArray(i - 1);
                double heuristic = threat_level / (double)Math.sqrt(robot.myLoc.distanceSquaredTo(loc));
                if(heuristic > bestHeuristic){
                    bestHeuristic = heuristic;
                    potentialLoc = loc;
                }
            }
        }
        return potentialLoc;
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

        // Update current threat loc if you can sense that location
        for(int i = BIGGEST_THREAT_LEVEL_IDX_START; i <= BIGGEST_THREAT_LEVEL_IDX_END; i += 2){
            int currThreatLocInt = rc.readSharedArray(i + 1);
            if(currThreatLocInt == 0 || currThreatLocInt == MAX_COMMS_VAL){
                continue;
            }
            MapLocation currThreatLoc = Util.intToMapLocation(currThreatLocInt);
            if(currThreatLoc.distanceSquaredTo(threatLoc) <= 4){ // If you're near the current threat loc, update its value
                if(threatLevel < THREAT_THRESHOLD){ // Update threshold value
                    threatLevel = 0;
                    threatLocInt = 0;
                }
                writeSharedArray(i, threatLevel); // Call for reinforcements
                writeSharedArray(i + 1, threatLocInt); // Call for reinforcements
                System.out.println("Writing threat level: " + threatLevel);
                System.out.println("Writing threat loc int: " + threatLocInt);
                return;
            }
        }

        if(threatLevel >= THREAT_THRESHOLD){
            int lowestThreatVal = Integer.MAX_VALUE;
            int lowestThreatIdx = -1;
            for(int i = BIGGEST_THREAT_LEVEL_IDX_START; i <= BIGGEST_THREAT_LEVEL_IDX_END; i += 2){
                int currThreatLevel = rc.readSharedArray(i);
                int currThreatLocInt = rc.readSharedArray(i + 1);
                if(currThreatLocInt == 0 || currThreatLocInt == MAX_COMMS_VAL){
                    lowestThreatIdx = i;
                    lowestThreatVal = 0;
                    break;
                }
                if(currThreatLevel < lowestThreatVal){
                    lowestThreatVal = currThreatLevel;
                    lowestThreatIdx = i;
                }
            }

            if(threatLevel > lowestThreatVal){
                writeSharedArray(lowestThreatIdx, threatLevel);
                writeSharedArray(lowestThreatIdx + 1, threatLocInt);
                System.out.println("Writing threat level: " + threatLevel);
                System.out.println("Writing threat loc int: " + threatLocInt);
            }
        }

    }

    public void updateMinerInstruction(int archonCommsIdx, MapLocation targetLoc, int minerType) throws GameActionException {
        int targetLocNum = Util.mapLocationToInt(targetLoc);
        int toWrite = targetLocNum * 10 + minerType;
        writeSharedArray(MINER_INSTRUCTION_START_IDX + archonCommsIdx, toWrite);
    }

    public MapLocation getArchonScoutingLocation(int archonCommsIdx) throws GameActionException {
        int minerInstruction = rc.readSharedArray(MINER_INSTRUCTION_START_IDX + archonCommsIdx);
        int scoutingLocNum = minerInstruction / 10;
        System.out.println("SCOUTING LOC NUM: " + scoutingLocNum);
        if(scoutingLocNum == 0){
            assert(false);
            return null;
        }
        return Util.intToMapLocation(scoutingLocNum);
    }

    public int getMinerType(int archonCommsIdx) throws GameActionException {
        int minerInstruction = rc.readSharedArray(MINER_INSTRUCTION_START_IDX + archonCommsIdx);
        int minerType = minerInstruction % 10;
        return minerType;
    }

    public int findBestArchonForHealing() throws GameActionException { // TODO Need to keep track of which friendly archons have died
        double bestHeuristic = Integer.MAX_VALUE;
        int bestIdx = -1;
        for(int i = 0; i < robot.numFriendlyArchons; i++){
            double numBusy = rc.readSharedArray(ARCHON_HEALING_START_IDX + i);
            MapLocation archonLoc = Util.intToMapLocation(rc.readSharedArray(i));
            double distance = Math.sqrt(robot.myLoc.distanceSquaredTo(archonLoc));
            // Heal rate is 2 and soldier health is 50, so if ur wasting 25 turns (10 turns there, 10 turns back) per numBusy don't go for it
            double heuristic = numBusy + distance * 2;
            if(heuristic < bestHeuristic){
                bestHeuristic = heuristic;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    // Returns potential enemy archon locations based on symmetry
    public MapLocation[] getPotentialEnemyArchonLocations() throws GameActionException {
        Logger.Log("--------------------------------------");

        // Search for potential enemy locations based on symmetry
        int symmetry = rc.readSharedArray(SYMMETRY_IDX); // Index for symmetry in shared array
        Logger.Log("Symmetry: " + symmetry);
        if(symmetry == 0){
            symmetry = 7; // Smth messed up XD, set symmetry back to 7
        }

        int num_symmetries_to_check = 0;
        int[] binVals = {1, 2, 4};
        for(int i = 0; i < binVals.length; i++){
            if((symmetry & binVals[i]) != 0){
                num_symmetries_to_check++;
            }
        }

        MapLocation[] potentialScoutingLocs = new MapLocation[num_symmetries_to_check * robot.friendlyArchons.length];
        int potentialScoutingCount = 0;

        for(int j = 0; j < 3; j++) {
            int reflectionType = j + 1;
            if((symmetry & binVals[j]) != 0){
                MapLocation[] enemyArchonLocs = Util.reflect(robot.friendlyArchons, reflectionType);
                for(int i = 0; i < enemyArchonLocs.length; i++){
                    potentialScoutingLocs[potentialScoutingCount] = enemyArchonLocs[i];
                    potentialScoutingCount++;
                }
            }
        }

        return potentialScoutingLocs;
    }

    // Symmetry stored as an integer, starting at 7 (111 in binary).
    // Third binary number: whether or not vertical symmetry is possible (0b001)
    // Second binary number: whether or not horizontal symmetry is possible (0b010)
    // First binary number: whether or not 180 degree rotational symmetry is possible (0b100)

    public void runRubbleBasedSymmetry() throws GameActionException {
        int initial_symmetry = rc.readSharedArray(SYMMETRY_IDX);
        int symmetry = initial_symmetry;
        if(symmetry == 0){
            symmetry = 7;
        }
        if(symmetry == 1 || symmetry == 2 || symmetry == 4){
            return;
        }

        int centerY = rc.getMapHeight() / 2;
        int centerX = rc.getMapWidth() / 2;

        int[] binVals = {1, 2, 4};
        boolean checkedSymmetry = false;
        for(int j = 2; j >= 0; j--){
            int reflectionType = j + 1;
            int binVal = binVals[j];
            if((symmetry & binVal) != 0){
                if((binVal == 1 || binVal == 4) && Math.abs(robot.myLoc.y - centerY) > 3){
                    continue;
                }
                if((binVal == 2 || binVal == 4) && Math.abs(robot.myLoc.x - centerX) > 3){
                    continue;
                }
                MapLocation[] nearbyLocs = rc.getAllLocationsWithinRadiusSquared(robot.myLoc, robot.myType.visionRadiusSquared);
                boolean valid = true;
                int ydiff = robot.myLoc.y - centerY;
                int xdiff = robot.myLoc.x - centerX;
                int numRunningOn = 0;
                System.out.println("Bin val: " + binVal);
                for(int i = nearbyLocs.length; i-- > 0; ){
                    switch(binVal){
                        case 1:
                            if(ydiff * (nearbyLocs[i].y - centerY) >= 0){
                                continue;
                            }
                            break;
                        case 2:
                        case 4:
                            if(xdiff * (nearbyLocs[i].x - centerX) >= 0){
                                continue;
                            }
                            break;
                    }
                    numRunningOn++;
                    MapLocation reflectLoc = Util.reflect(nearbyLocs[i], reflectionType);
                    if(binVal == 4 && !rc.canSenseLocation(reflectLoc)){
                        continue;
                    }
                    if(rc.senseRubble(nearbyLocs[i]) != rc.senseRubble(reflectLoc)){
                        valid = false;
                    }
                }
                checkedSymmetry = true;
                if(!valid){
                    System.out.println("Invalid! " + binVal);
                    symmetry &= (7 ^ binVal);
                }
            }
            if(checkedSymmetry){
                break;
            }
        }
        writeSharedArray(SYMMETRY_IDX, symmetry);
    }

    public boolean saveUpLead(int leadAmount) throws GameActionException {
        int currSaveUp = rc.readSharedArray(LEAD_SAVE_UP_IDX);
        if(currSaveUp != 0){
            return false;
        }
        writeSharedArray(LEAD_SAVE_UP_IDX, leadAmount);
        return true;
    }

    public void removeLeadSaveUp() throws GameActionException {
        writeSharedArray(LEAD_SAVE_UP_IDX, 0);
    }

    public int getLeadSaveUp() throws GameActionException {
        return rc.readSharedArray(LEAD_SAVE_UP_IDX);
    }


}
