package defensivebot;

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
    final int THREAT_THRESHOLD = 15;

    // Properties
    RobotController rc;
    Robot robot;

    public Comms(RobotController rc, Robot robot){
        this.rc = rc;
        this.robot = robot;
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
        if((symmetry & 1) != 0){
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
        if((symmetry & 2) != 0){
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
        if((symmetry & 4) != 0){
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
            if(enemyArchonIDs[i] != rc.readSharedArray(i + 4) - 1){ // NOTE: ARCHON IDs ARE SAVED AS ID + 1 since 0 is a possible ID
                rc.writeSharedArray(i + 4, enemyArchonIDs[i] + 1); // NOTE: ARCHON IDs ARE SAVED AS ID + 1 since 0 is a possible ID
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

    public void updateCenterOfAttackingMass(int index) throws GameActionException{
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int x_avg = 0; int y_avg = 0;
        int n = 1;
        for(RobotInfo info: nearbyRobots){
            MapLocation loc = info.getLocation();
           x_avg += loc.x; y_avg += loc.y;
           if(n >= 15) break;   // so we don't use too much bytecode
           n+= 1;
        }
        x_avg /= n; y_avg /= n;
        if(x_avg == 0 && y_avg == 0) {
            rc.writeSharedArray(CENTER_OF_ATTACKING_MASS_IDX + index, MAX_COMMS_VAL);
        }
        else {
            rc.writeSharedArray(CENTER_OF_ATTACKING_MASS_IDX + index, Util.xAndYToCompressed(x_avg, y_avg));
        }
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
        int threatLevel = 0;
        MapLocation threatLoc = null;
        for(int i = 0; i < robot.nearby.length; i++){
            RobotInfo info = robot.nearby[i];
            if(info.getTeam() == robot.opponent){
                if(info.getType() == RobotType.ARCHON){
                    threatLevel += 50;
                    threatLoc = info.getLocation();
                }
                else if(info.getType() == RobotType.SOLDIER){
                    threatLevel += 5;
                    if(threatLoc == null){
                        threatLoc = info.getLocation();
                    }
                }
            }
        }
        int currThreatLevel = rc.readSharedArray(BIGGEST_THREAT_LEVEL_IDX);
        int currThreatLocInt = rc.readSharedArray(BIGGEST_THREAT_LOC_IDX);
        MapLocation currThreatLoc = null;
        if(currThreatLocInt != 0 && currThreatLocInt != MAX_COMMS_VAL){
            currThreatLoc = Util.intToMapLocation(currThreatLocInt);
        }
        if(threatLoc == null){
            return currThreatLoc;
        }
        if(currThreatLoc != null && robot.myLoc.distanceSquaredTo(currThreatLoc) < 4){ // Reached the global currThreatLoc
            if(currThreatLevel != threatLevel){
                if(threatLevel < THREAT_THRESHOLD){ // No significant threat here
                    rc.writeSharedArray(BIGGEST_THREAT_LEVEL_IDX, 0);// Update sharedArray value
                    rc.writeSharedArray(BIGGEST_THREAT_LOC_IDX, 0);// Update sharedArray value
                }
                else{
                    rc.writeSharedArray(BIGGEST_THREAT_LEVEL_IDX, threatLevel);// Update sharedArray value
                    rc.writeSharedArray(BIGGEST_THREAT_LOC_IDX, currThreatLevel);// Update sharedArray value
                }
            }
        }
        else{
            if(threatLevel > currThreatLevel && threatLevel > THREAT_THRESHOLD){ // This threat is clearly more massive than the previous one T_T
                rc.writeSharedArray(BIGGEST_THREAT_LEVEL_IDX, threatLevel);// Update sharedArray value
                rc.writeSharedArray(BIGGEST_THREAT_LOC_IDX, currThreatLevel);// Update sharedArray value
            }
        }
        // Stick with the most updated loc
        int locInt = rc.readSharedArray(BIGGEST_THREAT_LOC_IDX);;
        if(locInt == 0 || locInt == MAX_COMMS_VAL){
            return null;
        }
        return Util.intToMapLocation(locInt);

    }

}