package defensivebotbase;

import battlecode.common.*;

public class Soldier extends Robot {
    MapLocation archonLoc = null;
    int archonIndex = -1;

    // Proportion of soldiers that are defensive (offensive will go to enemy, defensive will stay close to spawning archon)
    // might be smart to have a few created for each archon in the beginning of each game

    Direction[] shuffled = Util.shuffleArr(Util.directions);

    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.SAGE, RobotType.SOLDIER, RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY};
    boolean[][] visited = new boolean[4][3];
    MapLocation currentTarget;
    boolean enemyConfirmed = false;

    // Defending soldier variables
    boolean circleCW = true;
    int previousFriendlies = 0;
    int[] previousFriendlyIDs = new int[50];
    int[] previousFriendlyLocs = new int[50];
    int targetLevel = 0;

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        Logger.Log("Start of soldier run method: " + Clock.getBytecodesLeft());
        Logger.Log("Current Location: " + rc.getLocation());
//        Logger.Log("lead amount: " + rc.getTeamLeadAmount(this.myTeam));
        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);
        MapLocation enemyCOM = Util.calculateEnemySoldierCOM(rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent()));
        comms.updateCurrAttackLoc(enemyCOM);

        // If your bois are attacking an enemy more than the enemy is attacking ur bois, then go for the attack
        Logger.Log("Before checking attack: " + Clock.getBytecodesLeft());
        boolean shouldAttack = goForAttack(nearby, enemyCOM); // 2300 bytecode for 31 nearby
        Logger.Log("After checking attack: " + Clock.getBytecodesLeft());
        if(!shouldAttack){ // Check if you should go for the attack
            // RETREAT BACKWARDS
//            Logger.Log("RETREATING");
            // Run away from soldiers (move in opposite direction of soldier COM)
            Direction targetDir = myLoc.directionTo(enemyCOM).opposite(); // Find direction opposite of enemy COM
            if(enemyCOM.equals(myLoc)){
                targetDir = Direction.NORTH;
            }
            boolean retreated = nav.goTo(myLoc.add(targetDir).add(targetDir).add(targetDir)); // Move in that direction
            if(retreated){
                rc.setIndicatorString("Retreating! COM: " + enemyCOM.toString() + ", Moving: " + targetDir.toString());
                return;
            }
            else{
                rc.setIndicatorString("Can't retreat, mighgt as well try attacking in");
                attackEnemy(nearby);
            }
        }
        else{
            // Try to attack
            boolean attacked = attackEnemy(nearby);
            // Movement
            if(!attacked) {
                runAttackMovement();
            }
        }

    }

    public boolean checkFriendlyRetreating(RobotInfo[] nearby, MapLocation enemyCOM){
        Logger.Log("-----------------------------------------------------");
        Logger.Log("Cringe: " + Clock.getBytecodesLeft());
        boolean shouldRetreat = false;
        RobotInfo[] friendlies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam);
        Logger.Log("Count: " + nearby.length + ", vs: " + friendlies.length);
        int currFriendlies = 0;
        int[] currFriendlyIDs = new int[50];
        int[] currFriendlyLocs = new int[50];
        for(int i = 0; i < friendlies.length; i++){
            if(friendlies[i].team == myTeam && friendlies[i].type == RobotType.SOLDIER){
                int id = friendlies[i].getID();
                int index = -1;
                for(int j = 0; j < previousFriendlies; j++){
                    if(previousFriendlyIDs[j] == id){
                        index = j;
                        break;
                    }
                }
                int locInt = Util.mapLocationToInt(friendlies[i].getLocation());
                if(index != -1){
                    int prevLocInt = previousFriendlyLocs[index];
                    MapLocation prevLoc = Util.intToMapLocation(prevLocInt);
                    MapLocation currLoc = friendlies[i].getLocation();
                    // If he's closer to the enemy and he's retreating, you should retreat too
                    if(currLoc.distanceSquaredTo(enemyCOM) < myLoc.distanceSquaredTo(enemyCOM) && currLoc.distanceSquaredTo(enemyCOM) > prevLoc.distanceSquaredTo(enemyCOM)){
                        shouldRetreat = true;
                    }
                }
                Logger.Log("Test: " + Clock.getBytecodesLeft());
                currFriendlyIDs[currFriendlies] = id;
                currFriendlyLocs[currFriendlies] = locInt;
                Logger.Log("Calculated: " + Clock.getBytecodesLeft());
                currFriendlies++;
            }
            else{
                Logger.Log("Not calculated: " + Clock.getBytecodesLeft());
            }
        }
        previousFriendlyIDs = currFriendlyIDs;
        previousFriendlyLocs = currFriendlyLocs;
        return shouldRetreat;
    }

    public boolean goForAttack(RobotInfo[] nearby, MapLocation enemyCOM){ // TODO: Maybe only check # of attackers on the robot closest to you?
        // If friendly that is closer to enemy is retreating, maybe u shld too!
//        if(checkFriendlyRetreating(nearby, enemyCOM)){
//            return false;
//        }

        int ourBestAttack = 0;
        int enemyBestAttack = 0;
        int enemyAttackOnMe = Util.countRobotTypes(rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam.opponent()), RobotType.SOLDIER);
//        int enemyAttackOnMe = Util.countRobotTypesWithinLocation(nearby, RobotType.SOLDIER, myTeam.opponent(), myLoc, RobotType.SOLDIER.actionRadiusSquared);
        Logger.Log("Before start: " + Clock.getBytecodesLeft());
        RobotInfo[] totalFriendlies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam);
        if(totalFriendlies.length < enemyAttackOnMe){ // Shortcut to save bytecode in certain scenarios
            return false;
        }
        Logger.Log("Start: " + Clock.getBytecodesLeft());
        Logger.Log("Num nearby: " + nearby.length);
        for(int i = 0; i < nearby.length; i++){
            if(nearby[i].getType() != RobotType.SOLDIER){
                continue;
            }
            if(nearby[i].getTeam() == myTeam.opponent()){
//                int temp = Util.countRobotTypesWithinLocation(nearby, RobotType.SOLDIER, myTeam, nearby[i].getLocation(), RobotType.SOLDIER.actionRadiusSquared);
//                int temp = 0;
                RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(nearby[i].getLocation(), RobotType.SOLDIER.actionRadiusSquared, myTeam);
                int temp = nearbyFriendlies.length; // Assume all friendlies are soldiers?
//                for(int j = nearby.length; j-- > 0; ){
//                    if(nearby[j].type == RobotType.SOLDIER && nearby[j].team == myTeam && nearby[j].location.distanceSquaredTo(nearby[i].location) <= nearby[j].type.actionRadiusSquared){
//                        temp++;
//                    }
//                }
                if(myLoc.distanceSquaredTo(nearby[i].getLocation()) <= RobotType.SOLDIER.actionRadiusSquared){
                    temp++; // Factor in the fact that you can also attack that enemy robot
                }
                ourBestAttack = Math.max(ourBestAttack, temp); // Find your best attack on the enemy
//                Logger.Log("Temp: " + Clock.getBytecodesLeft());
                if(ourBestAttack >= enemyAttackOnMe){
                    return true;
                }
            }
//            else{
//                int temp = Util.countRobotTypesWithinLocation(nearby, RobotType.SOLDIER, myTeam.opponent(), nearby[i].getLocation(), RobotType.SOLDIER.actionRadiusSquared);
//                enemyBestAttack = Math.max(enemyBestAttack, temp); // Find the enemy's best attack on you
//            }
        }
        Logger.Log("END: " + Clock.getBytecodesLeft());
        // Factor in yourself
//        enemyBestAttack = Math.max(enemyBestAttack, enemyAttackOnMe);
        return ourBestAttack >= enemyAttackOnMe;
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

        if (toAttack != null && rc.canAttack(toAttack)) { // TODO: If you can move to a better spot and then continue attacking, do that instead
            // Check if you have more friendly soldiers than enemy soldiers
            rc.attack(toAttack);
            rc.setIndicatorString("Attacking: " + toAttack.toString());
            return true;
        }

        return false;
    }

    public void runAttackMovement() throws GameActionException {
        // If you can sense an enemy (but I guess you can't attack it), go towards it
        // Technically this line shld only be running if you have more friendlies than enemies in the area, so we shld be gucci
        if(currentTarget != null && myLoc.distanceSquaredTo(currentTarget) <= myType.actionRadiusSquared && rc.isActionReady()){
            targetLevel = 0; // Reset target
        }
//        Logger.Log("Target level: " + targetLevel);
        Logger.Log("Before resetting target: " + Clock.getBytecodesLeft());
        resetTarget(); // Find best target at current round. 500 bytecode
        Logger.Log("After resetting target: " + Clock.getBytecodesLeft());
        assert(currentTarget != null);
//        Logger.Log("Current target: " + currentTarget.toString());
//        if(rc.canSenseLocation(currentTarget) && rc.isLocationOccupied(currentTarget) && rc.senseRobotAtLocation(currentTarget).team != myTeam){
        if(targetLevel == 5){ // Bugnav if going for visible enemy
//            nav.bug0nav(currentTarget);
//            nav.goTo(currentTarget);
//            nav.moveTowardsSafe(currentTarget); // TODO: If you have overwhelming forces, then just move in that direction anyways
            Logger.Log("Bytecode before: " + Clock.getBytecodesLeft());
            moveForwardSafely(currentTarget); // 1k bytecode for 24 nearby
            rc.setIndicatorLine(myLoc, currentTarget, 255, 0, 0);
            rc.setIndicatorString("Safenav to enemy at: " + currentTarget.toString());
            if(Util.getArrayIndex(Logger.allowedIDs, rc.getID()) != -1){
                rc.setIndicatorLine(myLoc, archonLoc, 0, 0, 255);
            }
            Logger.Log("Bytecode after: " + Clock.getBytecodesLeft());
        }
        else{ // Fuzzy nav if going to general area
            nav.goTo(currentTarget);
            rc.setIndicatorLine(myLoc, currentTarget, 0, 255, 0);
            rc.setIndicatorString("Fuzzynav to location: " + currentTarget.toString());
        }
//        Logger.Log("Bytecode left at end of soldier class: " + Clock.getBytecodesLeft());
    }

    public void moveForwardSafely(MapLocation attackTarget) throws GameActionException {
        double friendlyDPS = 0.0;
        double enemyDPS = 0.0;
        Logger.Log("Nearby length: " + nearby.length);
        for(int i = 0; i < nearby.length; i++){
            RobotInfo info = nearby[i];
            if(info.type != RobotType.SOLDIER || info.type != RobotType.WATCHTOWER){
                continue;
            }
            double cooldown = rc.senseRubble(info.location) + 10.0;
            double dps = info.type.damage / cooldown;
            if(info.team == myTeam){
                friendlyDPS += dps;
            }
            else{
                enemyDPS += dps;
            }
            Logger.Log("Temp: " + Clock.getBytecodesLeft());
        }

        int lowestRubble = Integer.MAX_VALUE;
        MapLocation lowestLoc = null;
        Direction targetDir = myLoc.directionTo(attackTarget);
        MapLocation[] testLocs = {myLoc.add(targetDir), myLoc.add(targetDir.rotateLeft()), myLoc.add(targetDir.rotateRight()), myLoc.add(targetDir.rotateLeft().rotateLeft()), myLoc.add(targetDir.rotateRight().rotateRight())};
        for(int i = 0; i < testLocs.length; i++){
            if(!rc.canSenseLocation(testLocs[i])){
                continue;
            }
            if(!rc.canMove(myLoc.directionTo(testLocs[i]))){
                continue;
            }
            int rubble = rc.senseRubble(testLocs[i]);
            if(rubble < lowestRubble){
                lowestLoc = testLocs[i];
                lowestRubble = rubble;
            }
        }
        if(lowestLoc == null){
            return;
        }
        double cooldown = rc.senseRubble(lowestLoc) + 10.0;
        double dps = myType.damage / cooldown;
        friendlyDPS += dps;

        if(friendlyDPS >= enemyDPS){
            rc.move(myLoc.directionTo(lowestLoc));
        }

    }

    public void resetTarget() throws GameActionException {
        // Search for enemy on comms
        Logger.Log("Current target level: " + targetLevel);
        MapLocation tempLoc = targetNearbyEnemy();
        if(tempLoc != null){
            currentTarget = tempLoc;
            targetLevel = 5;
            Logger.Log("New current target: " + currentTarget.toString() + ", found cuz i can sense the mans");
        }
        else{
            targetLevel = 0; // Enemy is no longer present
        }
        if(targetLevel >= 5){
            return;
        }
        // Check if the squad is currently attacking anyone
        tempLoc = comms.getCurrAttackLoc(); // TODO: Let archon updateCurrAttackLoc so that soldiers will go back to help defend archon
        if(tempLoc != null){
            currentTarget = tempLoc;
            targetLevel = 4;
            Logger.Log("New current target: " + currentTarget.toString() + ", using comms attack loc");
        }
        else{
            targetLevel = 0; // Squad is no longer there
        }
        if(targetLevel >= 4){
            return;
        }
        tempLoc = enemyArchonOnComms();
        if(tempLoc != null){
            currentTarget = tempLoc;
            enemyConfirmed = true;
            targetLevel = 3;
            Logger.Log("New current target: " + currentTarget.toString() + ", found archon via comms");
        }
        else{
            targetLevel = 0; // Archon is no longer there
        }
        if(targetLevel >= 2){
            return;
        }
        enemyConfirmed = false;
        // Scout for enemy based on symmetry
        tempLoc = scoutForEnemyArchons();
        if(tempLoc != null){
            currentTarget = tempLoc;
            targetLevel = 2;
            Logger.Log("New current target: " + currentTarget.toString() + ", scouting for enemy archon");
        }
        if(targetLevel >= 1){
            return;
        }
        // Run around randomly?
        targetLevel = 1;
        currentTarget = nav.getRandomMapLocation();
        Logger.Log("New current target: " + currentTarget.toString() + ", randomly chosen location");
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
        for(int i = 0; i < potentialScoutingLocs.length; i++){
            if(potentialScoutingLocs[i] != null){
                return potentialScoutingLocs[i];
            }
        }

        return closestEnemy;
    }

}

