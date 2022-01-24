package sprintbot11;

import battlecode.common.*;

public class WatchTower extends Robot {

    RobotType[] priorityOrder = {RobotType.SAGE, RobotType.SOLDIER, RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY};
    MapLocation currentTarget;
    MapLocation archonLoc = null;
    int archonIndex = -1;
    boolean enemyConfirmed = false;
    boolean[][] visited = new boolean[4][3];

//    int stationaryPeriod = 0;       // variable to keep track of how long we've been in turret mode
//    final int MOVEMENT_BUFFER = 20; // after how many rounds of turret mode should we move

    int mid_x = rc.getMapWidth()/2;
    int mid_y = rc.getMapHeight()/2;
    MapLocation middle = new MapLocation(mid_x, mid_y);


    public WatchTower(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        comms.scanEnemyArchons();
        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);

        if(shouldMove()){
            while(rc.getMode().equals(RobotMode.TURRET) && rc.canTransform()){
                rc.setIndicatorString("Transforming to portable");
                rc.transform();
            }
            runAttackMovement();
        }
        else{
            while(rc.getMode().equals(RobotMode.PORTABLE) && rc.canTransform()){
                rc.setIndicatorString("Transforming to turret");
                rc.transform();
            }
            attackEnemy(nearby);
        }
    }

    public boolean shouldMove() throws GameActionException {
        return rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent()).length == 0;
    }

    public boolean attackEnemy(RobotInfo[] nearby) throws GameActionException {
        Team opponent = myTeam.opponent();

        MapLocation toAttack = null;
        int oppTypeIndex = 20;
        int health = Integer.MAX_VALUE;

        for (int i = 0; i < nearby.length; i++) {
            RobotInfo info = nearby[i];
            if(info.team != opponent){
                continue; // Friendly
            }
            if(myLoc.distanceSquaredTo(info.getLocation()) > myType.actionRadiusSquared){
                continue; // Too far to attack
            }
            int currOppTypeIndex = Util.getArrayIndex(priorityOrder, info.type);
            int currHealth = info.getHealth();

            // Find the best enemy to attack based on priority order
            if (toAttack == null || currOppTypeIndex < oppTypeIndex || (currOppTypeIndex == oppTypeIndex && currHealth < health)) {
                toAttack = info.location;
                oppTypeIndex = currOppTypeIndex;
                health = currHealth;
            }
        }

        if (toAttack != null && rc.canAttack(toAttack)) {
            // Check if you have more friendly soldiers than enemy soldiers
            rc.attack(toAttack);
            rc.setIndicatorLine(myLoc, toAttack, 255, 0, 0);
            rc.setIndicatorString("Attacking: " + toAttack.toString());
            return true;
        }
        return false;
    }

    public void runAttackMovement() throws GameActionException {
        // Check comms for archon loc
        if(currentTarget != null && !enemyConfirmed){
            MapLocation temp = enemyArchonOnComms();
            if(temp != null){
                currentTarget = temp;
            }
        }
        // If you can sense an enemy (but I guess you can't attack it), go towards it
        // Technically this line shld only be running if you have more friendlies than enemies in the area, so we shld be gucci
        MapLocation bestLoc = null;
        int bestTypeIndex = 100;
        for (int i = 0; i < nearby.length; i++){
            if(nearby[i].getTeam() != myTeam){
                int currOppTypeIndex = Util.getArrayIndex(priorityOrder, nearby[i].type);
                if(currOppTypeIndex < bestTypeIndex){
                    bestLoc = nearby[i].getLocation();
                }
            }
        }
        if(bestLoc != null){
            currentTarget = bestLoc;
        }
        // Reset target if you alr reached your currentTarget
        if(currentTarget == null){
            resetTarget();
        }
        else if(myLoc.distanceSquaredTo(currentTarget) <= myType.actionRadiusSquared && rc.isActionReady()){
            resetTarget();
        }
        nav.minDistToSatisfy = myType.actionRadiusSquared;
        nav.goTo(currentTarget);
        rc.setIndicatorLine(myLoc, currentTarget, 0, 255, 0);
        rc.setIndicatorString("Going to: " + currentTarget.toString());
//        Logger.Log("Bytecode left at end of soldier class: " + Clock.getBytecodesLeft());
    }

    public void resetTarget() throws GameActionException {
//        Logger.Log("Resetting target");
        // Search for enemy on comms
        currentTarget = enemyArchonOnComms();
        if(currentTarget != null){
            enemyConfirmed = true;
//            Logger.Log("Got comms based target");
            return;
        }
        enemyConfirmed = false;
        MapLocation bestLoc = targetNearbyEnemy();
        if(bestLoc != null){
            currentTarget = bestLoc;
        }
        // Check if the squad is currently attacking anyone
        currentTarget = comms.getCurrAttackLoc();
        if(currentTarget != null){
            return;
        }
        // Scout for enemy based on symmetry
        currentTarget = scoutForEnemyArchons();
        if(currentTarget != null){
//            Logger.Log("Got scouting target");
            return;
        }
        // Run around randomly?
        currentTarget = middle;
//        Logger.Log("Got random target");
    }

    public MapLocation targetNearbyEnemy() throws GameActionException {
        // If you can sense an enemy (but I guess you can't attack it), go towards it
        // Technically this line shld only be running if you have more friendlies than enemies in the area, so we shld be gucci
        MapLocation bestLoc = null;
        int bestTypeIndex = 100;
        for (int i = 0; i < nearby.length; i++){
            if(nearby[i].getTeam() != myTeam){
                int currOppTypeIndex = Util.getArrayIndex(priorityOrder, nearby[i].type);
                if(currOppTypeIndex < bestTypeIndex){
                    bestLoc = nearby[i].getLocation();
                }
            }
        }
        return bestLoc;
    }

    public MapLocation enemyArchonOnComms() throws GameActionException {
        MapLocation closestEnemy = null;
        int closestDist = Integer.MAX_VALUE;
        for(int i = 8; i < 12; i++){
            int val = rc.readSharedArray(i);
            if(val != 0 && val != comms.MAX_COMMS_VAL){
                MapLocation enemyLoc = Util.intToMapLocation(val);
                int enemyDist = myLoc.distanceSquaredTo(enemyLoc);
                if(enemyDist < closestDist){
                    closestDist = enemyDist;
                    closestEnemy = enemyLoc;
                }
            }
        }
        return closestEnemy;
    }

    // TODO Copy in upgraded soldier methods into here

    public MapLocation scoutForEnemyArchons() throws GameActionException {
        Logger.Log("--------------------------------------");
        // Find the closest enemy we've detected
        MapLocation closestEnemy = null;
        int closestDist = Integer.MAX_VALUE;

        // Otherwise search for one based on symmetry
        int symmetry = rc.readSharedArray(comms.SYMMETRY_IDX); // Index for symmetry in shared array
        Logger.Log("Symmetry: " + symmetry);
        if(symmetry == 0){
            symmetry = 7; // Smth messed up XD
        }

        MapLocation[] potentialScoutingLocs = new MapLocation[12];
        int potentialScoutingCount = 0;

//        Logger.Log("Friendly archon: " + friendlyArchons[0].toString());
        int[] binVals = {1, 2, 4};
        for(int j = 0; j < 3; j++) {
            int reflectionType = j + 1;
            int binVal = binVals[j];
            if((symmetry & binVal) != 0){
                MapLocation[] enemyArchonLocs = Util.reflect(friendlyArchons, reflectionType);
                for(int i = 0; i < enemyArchonLocs.length; i++){
                    if(rc.canSenseLocation(enemyArchonLocs[i])){
                        if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, myTeam.opponent())){
                            visited[i][j] = true;
                        }
                    }
                    if(visited[i][j]){
                        continue;
                    }
                    potentialScoutingLocs[potentialScoutingCount] = enemyArchonLocs[i];
                    potentialScoutingCount++;
                    int dist = myLoc.distanceSquaredTo(enemyArchonLocs[i]);
                    if(dist < closestDist){
                        closestEnemy = enemyArchonLocs[i];
                        closestDist = dist;
                    }
                }
            }
        }

        // Pick a random location to scout
        potentialScoutingLocs = Util.shuffleArr(potentialScoutingLocs);
//        Logger.Log("Number of scouting locs: " + potentialScoutingCount);
//        for(MapLocation loc : potentialScoutingLocs){
//            if(loc != null){
//                Logger.Log("Potential Scouting Loc: " + loc.toString());
//            }
//        }
        for(int i = 0; i < potentialScoutingLocs.length; i++){
            if(potentialScoutingLocs[i] != null){
//                Logger.Log("Heading towards: " + potentialScoutingLocs[i].toString());
                return potentialScoutingLocs[i];
            }
        }

        return closestEnemy;
    }
}
