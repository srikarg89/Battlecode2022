package rushbot;

import battlecode.common.*;

public class Soldier extends Robot {
    MapLocation archonLoc = null;

    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.SAGE, RobotType.SOLDIER, RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY};
    boolean[][] visited = new boolean[4][3];
    MapLocation currentTarget;
    boolean enemyConfirmed = false;

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        //lifted from miner code
        if (archonLoc == null) {
            for (Direction dir : Navigation.directions) {
                MapLocation testLoc = myLoc.add(dir);
                if (rc.canSenseRobotAtLocation(testLoc)) {
                    RobotInfo info = rc.senseRobotAtLocation(testLoc);
                    if (info.getType() == RobotType.ARCHON && info.getTeam() == myTeam) {
                        archonLoc = testLoc;
                        Logger.Log("Found my archon loc: " + archonLoc.toString());
                    }
                }
            }
        }
        assert (archonLoc != null);

        boolean attacked = attackEnemy();
        if(!attacked){
            MapLocation nearbyEnemy = targetNearbyEnemy();
            if(nearbyEnemy != null){
                currentTarget = nearbyEnemy;
                Logger.Log("Targetting nearby enemy");
            }
            // Apparently there's nothing there to attack, so go find another location to scout
            else if(currentTarget != null && !enemyConfirmed){
                MapLocation temp = enemyArchonOnComms();
                if(temp != null){
                    currentTarget = temp;
                    Logger.Log("Got comms based target");
                }
            }
            if(currentTarget == null){
                resetTarget();
            }
            else if(myLoc.distanceSquaredTo(currentTarget) <= myType.actionRadiusSquared && rc.isActionReady()){
                Logger.Log("REEEEEEEEEEEEEEEEEEEE IM CLOSE ENOUGH AND ACTION IS READY BUT NOT ATTACKING REEEEEE");
                resetTarget();
            }
            nav.minDistToSatisfy = myType.actionRadiusSquared;
            nav.goTo(currentTarget);
            rc.setIndicatorLine(myLoc, currentTarget, 0, 255, 0);
            rc.setIndicatorString("Going to: " + currentTarget.toString());
//        Logger.Log("Bytecode left at end of soldier class: " + Clock.getBytecodesLeft());
        }
    }

    public boolean attackEnemy() throws GameActionException {
        int radius = myType.actionRadiusSquared;
        Team opponent = myTeam.opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);

        MapLocation toAttack = null;
        int oppTypeIndex = 20;
        int health = Integer.MAX_VALUE;

        for (int i = 0; i < enemies.length; i++) {
            RobotInfo info = enemies[i];
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
            rc.attack(toAttack);
            rc.setIndicatorLine(myLoc, toAttack, 255, 0, 0);
            rc.setIndicatorString("Attacking: " + toAttack.toString());
            return true;
        }
        return false;
    }

    public void resetTarget() throws GameActionException {
        // Find an archon to target
//        Logger.Log("Resetting target");
        // Search for enemy on comms
        currentTarget = enemyArchonOnComms();
        if(currentTarget != null){
            enemyConfirmed = true;
//            Logger.Log("Got comms based target");
            return;
        }
        enemyConfirmed = false;
        currentTarget = targetNearbyEnemy();
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
        currentTarget = nav.getRandomMapLocation();
//        Logger.Log("Got random target");
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

    public MapLocation scoutForEnemyArchons() throws GameActionException {
        Logger.Log("--------------------------------------");
        // Find the closest enemy we've detected
        MapLocation closestEnemy = null;
        int closestDist = Integer.MAX_VALUE;

        // Otherwise search for one based on symmetry
        int symmetry = rc.readSharedArray(comms.SYMMETRY_IDX); // Index for symmetry in shared array
        // TODO: FIGURE THIS OUT
        symmetry = 7;

        MapLocation[] potentialScoutingLocs = new MapLocation[12];
        int potentialScoutingCount = 0;

        Logger.Log("Friendly archon: " + friendlyArchons[0].toString());
        if((symmetry & 1) != 0 || symmetry == 0){
            Logger.Log("Symmetry 1!");
            MapLocation[] enemyArchonLocs = Util.reflect(friendlyArchons, 1);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                if(rc.canSenseLocation(enemyArchonLocs[i])){
                    if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, myTeam.opponent())){
                        visited[i][0] = true;
                    }
                }
                if(visited[i][0]){
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
        if((symmetry & 2) != 0 || symmetry == 0){
            Logger.Log("Symmetry 2!");
            MapLocation[] enemyArchonLocs = Util.reflect(friendlyArchons, 2);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                if(rc.canSenseLocation(enemyArchonLocs[i])){
                    if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, myTeam.opponent())){
                        visited[i][1] = true;
                    }
                }
                if(visited[i][1]){
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
        if((symmetry & 4) != 0 || symmetry == 0){
            Logger.Log("Symmetry 3!");
            MapLocation[] enemyArchonLocs = Util.reflect(friendlyArchons, 3);
            for(int i = 0; i < enemyArchonLocs.length; i++){
                if(rc.canSenseLocation(enemyArchonLocs[i])){
                    if(!Util.checkRobotPresent(enemyArchonLocs[i], RobotType.ARCHON, myTeam.opponent())){
                        visited[i][2] = true;
                    }
                }
                if(visited[i][2]){
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

        // Pick a random location to scout
        potentialScoutingLocs = Util.shuffleArr(potentialScoutingLocs);
        Logger.Log("Number of scouting locs: " + potentialScoutingCount);
        for(MapLocation loc : potentialScoutingLocs){
            if(loc != null){
                Logger.Log("Potential Scouting Loc: " + loc.toString());
            }
        }
        for(int i = 0; i < potentialScoutingLocs.length; i++){
            if(potentialScoutingLocs[i] != null){
                Logger.Log("Heading towards: " + potentialScoutingLocs[i].toString());
                Logger.Log("--------------------------------------");
                return potentialScoutingLocs[i];
            }
        }

        Logger.Log("--------------------------------------");
        return closestEnemy;
    }

}
