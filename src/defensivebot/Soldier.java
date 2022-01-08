package defensivebot;

import battlecode.common.*;

public class Soldier extends Robot {
    MapLocation archonLoc = null;
    int archonIndex = -1;

    // Proportion of soldiers that are defensive (offensive will go to enemy, defensive will stay close to spawning archon)
    // might be smart to have a few created for each archon in the beginning of each game

    // allow us to have different types of soldiers with different movement behaviour
    String type = "offensive"; // defensive, scouting
    Direction[] shuffled = Util.shuffleArr(Util.directions);


    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.SAGE, RobotType.SOLDIER, RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY};
    boolean[][] visited = new boolean[4][3];
    MapLocation currentTarget;
    boolean enemyConfirmed = false;

    // Defending soldier variables
    boolean circleCW = true;

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
//        if(rc.getID() % 2 == 0){
        if(false){
            this.type = "defensive";
        }
        else{
            this.type = "offensive";
        }
    }

    public void run() throws GameActionException {
        super.run();
//        Logger.Log("lead amount: " + rc.getTeamLeadAmount(this.myTeam));
        //lifted from miner code

        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
//            Logger.Log("found my archonIndex and archonLoc");
        }
        assert (archonLoc != null);

        // Attacking (same for both offensive and defensive soldiers)
        // 0 = no enemy to attack, 1 = successfully attacked enemy, 2 = NEED TO RETREAT
        int friendlySoldiers = Util.countRobotTypes(nearby, RobotType.SOLDIER, myTeam);
        int enemySoldiers = Util.countRobotTypes(nearby, RobotType.SOLDIER, myTeam.opponent());
        if(enemySoldiers > friendlySoldiers + 1){ // + 1 because you count as a soldier :))
            // RETREAT BACK TO BASE
            Logger.Log("RETREATING");
            nav.goTo(archonLoc);
        }
        else{
            // Try to attack
            boolean attacked = attackEnemy(nearby);
            // Movement
            if(!attacked) {
                if (this.type == "defensive") {
                    runDefensiveMovement();
                } else {
                    runAttackMovement();
                }
            }
        }

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

    public void runDefensiveMovement() throws GameActionException {
        // movement code for defensive soldier (
        // explore area around spawned archon, but stay close
        // defensive bubble increases as rounds progress so we don't crowd up area around archon

        // check if there is a center of mass which we should navigate to
        int compressedCenterOfMass = rc.readSharedArray(comms.CENTER_OF_ATTACKING_MASS_IDX + archonIndex);

        if(compressedCenterOfMass != comms.MAX_COMMS_VAL){
            Logger.Log("DEFEND THE KINGDOM!!!!"); // LMFAO I LOVE THIS
            int[] coordinates = Util.compressedToXAndY(compressedCenterOfMass);
            nav.moveTowards(new MapLocation(coordinates[0], coordinates[1]));
        }

        else{
            int currDistance = myLoc.distanceSquaredTo(archonLoc);
            int scaledRadius = comms.getRobotCount(RobotType.SOLDIER) / numFriendlyArchons;

            int DEFENSIVE_BUBBLE_CONSTANT = 3;
            if (currDistance < (scaledRadius / DEFENSIVE_BUBBLE_CONSTANT)) {  // units surround spawning archon with specified radius
                nav.moveAwayFrom(archonLoc);
            } else if (currDistance < scaledRadius) {    // explore defensive bubblee
//                nav.circle(archonLoc);
//                Util.tryMove(shuffled);
            } else {   // move back towards spawning archon
                nav.moveTowards(archonLoc);
            }
        }
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
//            Logger.Log("Symmetry 1!");
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
//            Logger.Log("Symmetry 2!");
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
//            Logger.Log("Symmetry 3!");
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
//        Logger.Log("Number of scouting locs: " + potentialScoutingCount);
        for(MapLocation loc : potentialScoutingLocs){
            if(loc != null){
//                Logger.Log("Potential Scouting Loc: " + loc.toString());
            }
        }
        for(int i = 0; i < potentialScoutingLocs.length; i++){
            if(potentialScoutingLocs[i] != null){
//                Logger.Log("Heading towards: " + potentialScoutingLocs[i].toString());
                return potentialScoutingLocs[i];
            }
        }

        return closestEnemy;
    }

}

