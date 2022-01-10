package sprintbot;

import battlecode.common.*;

public class Soldier extends Robot {
    MapLocation archonLoc = null;
    int archonIndex = -1;

    // Proportion of soldiers that are defensive (offensive will go to enemy, defensive will stay close to spawning archon)
    // might be smart to have a few created for each archon in the beginning of each game

    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.SAGE, RobotType.SOLDIER, RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY};
    boolean[][] visited = new boolean[4][3];
    MapLocation currentTarget;
    boolean enemyConfirmed = false;

    // Defending soldier variables
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
        RobotInfo[] enemiesInVision = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());
        MapLocation enemyCOM = Util.calculateEnemySoldierCOM(enemiesInVision);
        comms.updateCurrAttackLoc(enemiesInVision, enemyCOM);

        // If your bois are attacking an enemy more than the enemy is attacking ur bois, then go for the attack
        Logger.Log("Before checking if safe: " + Clock.getBytecodesLeft());
        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam);
        RobotInfo[] nearbyActionEnemies = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam.opponent());
        RobotInfo[] nearbyVisionEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());
        RobotInfo nearestEnemyInfo = getNearestEnemy(nearbyActionEnemies);
        boolean inSafeZone = checkSafe(nearbyFriendlies, nearbyActionEnemies, nearestEnemyInfo); // 2300 bytecode for 31 nearby
        Logger.Log("After checking if safe: " + Clock.getBytecodesLeft());
        RobotInfo attackTarget = findAttackTarget(nearbyActionEnemies);
        Logger.Log("Action cooldown: " + rc.getActionCooldownTurns());
        Logger.Log("Movement cooldown: " + rc.getMovementCooldownTurns());
        // The strat is to get two shots for the enemy's one shot. ie, you try to stay on the boundary of the enemy's vision. Then, you push, attack, (they get a turn so they attack), then you attack again, then you retreat (back out of their range)
        if(inSafeZone){
            Logger.Log("In safe zone!");
            if(attackTarget != null){
                // If I can both move and attack, the check if I can move to a better loc and attack from there
                if(rc.isActionReady() && rc.isMovementReady()){
                    // If I can move to a lower cooldown loc and still attack from there, do that
                    boolean movedToLowerCooldown = attackFromLowerCooldown(attackTarget);
                    if(movedToLowerCooldown) {
                        Logger.Log("Moved lower coooldown");
                        attack(attackTarget);
                    }
                    else { // Attack and back up (you know the drill)
                        Logger.Log("Punch and pull back");
                        attack(attackTarget);
//                        if(attackTarget.type != RobotType.SOLDIER && attackTarget.type != RobotType.WATCHTOWER){
//                            retreat(enemyCOM);
//                        }
                        retreat(enemyCOM);
                    }
                }
                else if(rc.isActionReady()) { // If you can attack, just attack
                    Logger.Log("Can't move, just attacking");
                    attack(attackTarget);
                }
                else{
                    // Well you can't attack, so back up so that they can't attack you
                    Logger.Log("Can't attack, jus retreating");
                    retreat(enemyCOM);
                }
            }
            else{
                // If you can both attack and move, do that
                if(rc.isMovementReady() && rc.isActionReady() && nearestEnemyInfo != null){ // If you can see an enemy push him and attack him
                    Logger.Log("Push and punch");
                    push(enemyCOM, nearestEnemyInfo.location);
                    nearbyActionEnemies = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam);
                    attackTarget = findAttackTarget(nearbyActionEnemies);
                    attack(attackTarget);
                }
                else if(rc.isMovementReady()){ // Run regular scouting / comms movement
                    Logger.Log("Running regular movement");
                    runMovement();
                }
            }
        }
        else{
            Logger.Log("Not in safe zone!");
            attack(attackTarget);
            retreat(enemyCOM); // Get the hell out of there
        }

    }

    public void retreat(MapLocation enemyCOM) throws GameActionException { // Movement method
        Logger.Log("Retreating from: " + enemyCOM.toString());
        rc.setIndicatorString("Retreating from: " + enemyCOM.toString());
        if(enemyCOM.equals(myLoc)){ // No enemies nearby, just retreat back to base
            nav.goTo(archonLoc);
        }
        else{
            Direction away = myLoc.directionTo(enemyCOM).opposite();
            nav.goTo(myLoc.add(away).add(away).add(away).add(away));
        }
    }

    public void push(MapLocation enemyCOM, MapLocation bestAttackTarget) throws GameActionException { // Movement method
        Logger.Log("Pushing towards: " + enemyCOM.toString());
        rc.setIndicatorString("Pushing towards: " + enemyCOM.toString());
        if(enemyCOM.equals(myLoc)){ // No enemy soldiers nearby, go after whatever else there is
            nav.goTo(bestAttackTarget);
        }
        else{
            moveForwardSafely(enemyCOM);
        }
    }

    public void attack(RobotInfo toAttack) throws GameActionException { // Action method
        if(toAttack == null){
            return;
        }
        Logger.Log("Trying to attack: " + toAttack.location.toString());
        if(!rc.isActionReady()){
            return;
        }
        if (rc.canAttack(toAttack.location)) { // TODO: If you can move to a better spot and then continue attacking, do that instead
            // Check if you have more friendly soldiers than enemy soldiers
            rc.attack(toAttack.location);
            rc.setIndicatorString("Attacking: " + toAttack.location.toString());
        }
    }

    public RobotInfo findAttackTarget(RobotInfo[] nearbyEnemies) throws GameActionException {
        RobotInfo toAttack = null;
        int oppTypeIndex = 20;
        int health = Integer.MAX_VALUE;
        for(int i = 0; i < nearbyEnemies.length; i++){
            RobotInfo info = nearbyEnemies[i];
            if(!rc.canAttack(info.location)){
                continue; // Can't attack (prolly cuz too far)
            }
            int currOppTypeIndex = Util.getArrayIndex(priorityOrder, info.type);
            int currHealth = info.getHealth();

            // Find the best enemy to attack based on priority order
            if (currOppTypeIndex < oppTypeIndex || (currOppTypeIndex == oppTypeIndex && currHealth < health)) {
                toAttack = info;
                oppTypeIndex = currOppTypeIndex;
                health = currHealth;
            }
        }
        return toAttack;
    }

    public RobotInfo getNearestEnemy(RobotInfo[] nearbyEnemies) throws GameActionException {
        // Find nearest enemy
        RobotInfo nearestEnemyInfo = null;
        int minDist = Integer.MAX_VALUE;
        for(int i = 0; i < nearbyEnemies.length; i++){
            int dist = myLoc.distanceSquaredTo(nearbyEnemies[i].location);
            if(dist < minDist){
                minDist = dist;
                nearestEnemyInfo = nearbyEnemies[i];
            }
        }
        return nearestEnemyInfo;
    }

    public boolean checkSafe(RobotInfo[] nearbyFriendlies, RobotInfo[] dangeorusEnemies, RobotInfo nearestEnemyInfo) throws GameActionException { // TODO: Maybe only check # of attackers on the robot closest to you?
        // your attack isn't ready, then don't engage

        if(nearestEnemyInfo == null){ // No enemies nearby, we safe
            return true;
        }

        Logger.Log("Nearest enemy Info: " + nearestEnemyInfo.location.toString());

        double friendlyDamage = 0.0;
        double enemyDamage = 0.0;
        double friendlyHP = 0.0;
        double enemyHP = 0.0;

        // Calculate enemies attacking you
        for(int i = 0; i < dangeorusEnemies.length; i++){
            RobotInfo info = dangeorusEnemies[i];
            if(info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER){
                double attackCooldown = rc.senseRubble(info.location) + 10;
                enemyDamage += info.type.damage / attackCooldown;
                enemyHP += info.getHealth();
            }
        }

        // Calculate friendlies attacking the enemy
        for(int i = 0; i < nearbyFriendlies.length; i++){
            RobotInfo info = nearbyFriendlies[i];
            if(info.getLocation().distanceSquaredTo(nearestEnemyInfo.getLocation()) > info.type.actionRadiusSquared){
                continue; // Only count friendlies that can attack said enemy
            }
            if(info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER){
                double attackCooldown = rc.senseRubble(info.location) + 10;
                friendlyDamage += info.type.damage / attackCooldown;
                friendlyHP += info.getHealth();
            }
        }

        double myAttackCooldown = rc.senseRubble(myLoc) + 10;
        friendlyDamage += myType.damage / myAttackCooldown;
        friendlyHP += rc.getHealth();

        double myTurnsNeeded = friendlyDamage / enemyHP;
        double enemyTurnsNeeded = enemyDamage / friendlyHP;

        // 1.5 simply because im ballsy and wanna go for it
        return myTurnsNeeded <= enemyTurnsNeeded * 1.5; // If you can kill them faster than they can kill you, return true
    }

    public boolean moveForwardSafely(MapLocation target) throws GameActionException { // Maybe instead do smth else
        int currRubble = rc.senseRubble(myLoc);
        int lowestRubble = Integer.MAX_VALUE;
        MapLocation lowestLoc = null;
        Direction targetDir = myLoc.directionTo(target);
        Direction[] testDirs = {targetDir, targetDir.rotateLeft(), targetDir.rotateRight(), targetDir.rotateLeft().rotateLeft(), targetDir.rotateRight().rotateRight()};
        for(int i = 0; i < testDirs.length; i++){
            MapLocation testLoc = myLoc.add(testDirs[i]);
            if(!rc.canSenseLocation(testLoc)){
                continue;
            }
            if(!rc.canMove(myLoc.directionTo(testLoc))){
                continue;
            }
            int rubble = rc.senseRubble(testLoc);
            if(rubble < lowestRubble){
                lowestLoc = testLoc;
                lowestRubble = rubble;
            }
        }
        if(lowestRubble > currRubble){ // Only move towards a direction w/ less than or equal to rubble
            return false;
        }
        rc.move(myLoc.directionTo(lowestLoc));
        return true;
    }

    public boolean attackFromLowerCooldown(RobotInfo attackTarget) throws GameActionException {
        MapLocation targetLoc = attackTarget.location;
        int currRubble = rc.senseRubble(myLoc);
        int lowestRubble = Integer.MAX_VALUE;
        MapLocation lowestLoc = null;
        Direction targetDir = myLoc.directionTo(targetLoc);
        Direction[] testDirs = {targetDir, targetDir.rotateLeft(), targetDir.rotateRight(), targetDir.rotateLeft().rotateLeft(), targetDir.rotateRight().rotateRight()};
        for(int i = 0; i < testDirs.length; i++){
            MapLocation testLoc = myLoc.add(testDirs[i]);
            if(!rc.canSenseLocation(testLoc)){
                continue;
            }
            if(!rc.canMove(myLoc.directionTo(testLoc))){
                continue;
            }
            if(myLoc.distanceSquaredTo(targetLoc) > myType.actionRadiusSquared){ // Make sure you can still attack the enemy troop
                continue;
            }
            int rubble = rc.senseRubble(testLoc);
            if(rubble < lowestRubble){
                lowestLoc = testLoc;
                lowestRubble = rubble;
            }
        }
        // TODO: Check if its safe to move in that direction
        if(lowestRubble >= currRubble){ // Only move towards a direction w/ less than or equal to rubble
            return false;
        }
        rc.move(myLoc.directionTo(lowestLoc));
        return true;
    }

    public void runMovement() throws GameActionException { // Assumes there's no enemies near you
        if(currentTarget != null && myLoc.distanceSquaredTo(currentTarget) <= 4){
            targetLevel = 0; // Reached target and nothing there, so find a new target
        }

        Logger.Log("Before resetting target: " + Clock.getBytecodesLeft());
        resetTarget(); // Find best target at current round. 500 bytecode
        Logger.Log("After resetting target: " + Clock.getBytecodesLeft());
        assert(currentTarget != null);
//        Logger.Log("Current target: " + currentTarget.toString());
        nav.goTo(currentTarget);
        rc.setIndicatorLine(myLoc, currentTarget, 0, 255, 0);
        rc.setIndicatorString("Fuzzynav to location: " + currentTarget.toString());
//        Logger.Log("Bytecode left at end of soldier class: " + Clock.getBytecodesLeft());
    }

    public void resetTarget() throws GameActionException {
        // Search for enemy on comms
        // Check if the squad is currently attacking anyone
        MapLocation tempLoc = comms.getCurrAttackLoc(); // TODO: Let archon updateCurrAttackLoc so that soldiers will go back to help defend archon
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

