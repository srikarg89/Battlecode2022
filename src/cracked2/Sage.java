package cracked2;

import battlecode.common.*;

public class Sage extends Robot {
    MapLocation archonLoc = null;
    int archonIndex = -1;

    // The last enemy that you attacked (so that you can chase them if they get away from your sight)
    MapLocation lastAttackLoc = null;

    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.SAGE, RobotType.SOLDIER, RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY};
    boolean[][] visited = new boolean[4][3];
    MapLocation currentTarget;
    boolean enemyConfirmed = false;
    boolean needHealing = false;
    final int health_to_retreat = 20;
    int bestArchonForHealingIdx = -1;
    int healingLeft = -1;

    // Defending soldier variables
    int targetLevel = 0;

    public Sage(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
//        Logger.Log("lead amount: " + rc.getTeamLeadAmount(this.myTeam));
        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);

        indicatorString += "AC:" + rc.getActionCooldownTurns() + ";MC:" + rc.getMovementCooldownTurns() + ";";

        // If your bois are attacking an enemy more than the enemy is attacking ur bois, then go for the attack
        RobotInfo[] nearbyActionEnemies = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam.opponent());
        RobotInfo[] nearbyVisionEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());

        MapLocation enemySoldierCOM = Util.calculateEnemySoldierCOM(nearbyVisionEnemies);
        RobotInfo nearestEnemyInfo = getNearestEnemy(nearbyVisionEnemies);
        RobotInfo attackTarget = findAttackTarget(nearbyActionEnemies);

        comms.updateCurrAttackLoc(nearbyEnemies, enemySoldierCOM);

        if(rc.getHealth() <= health_to_retreat){
            needHealing = true;
        }

        if(needHealing && rc.getHealth() == myType.getMaxHealth(rc.getLevel())){
            needHealing = false;
            int currVal = rc.readSharedArray(comms.ARCHON_HEALING_START_IDX + bestArchonForHealingIdx);
            int maxHealth = myType.getMaxHealth(rc.getLevel());
            int prevHealingLeft = healingLeft;
            healingLeft = maxHealth - rc.getHealth();
            comms.writeSharedArray(comms.ARCHON_HEALING_START_IDX + bestArchonForHealingIdx, currVal - prevHealingLeft + healingLeft);
        }

        if(needHealing){
            attack(attackTarget);
            if(enemySoldierCOM.equals(myLoc)){
                bestArchonForHealingIdx = comms.findBestArchonForHealing();
                MapLocation targetLoc = Util.intToMapLocation(rc.readSharedArray(bestArchonForHealingIdx));
                if(myLoc.distanceSquaredTo(targetLoc) > RobotType.ARCHON.actionRadiusSquared - 10){
                    nav.goTo(targetLoc);
                }
                else{
                    nav.circle(targetLoc, 8, true);
                }
                int currVal = rc.readSharedArray(comms.ARCHON_HEALING_START_IDX + bestArchonForHealingIdx);
                int maxHealth = myType.getMaxHealth(rc.getLevel());
                int prevHealingLeft = healingLeft;
                healingLeft = maxHealth - rc.getHealth();
                comms.writeSharedArray(comms.ARCHON_HEALING_START_IDX + bestArchonForHealingIdx, currVal - prevHealingLeft + healingLeft);
            }
            else{
                retreat(enemySoldierCOM);
            }

        }
        else if(rc.isActionReady()){
            if(attackTarget != null){
                boolean movedToLowerCooldown = attackFromLowerCooldown(attackTarget);
                if(movedToLowerCooldown) {
                    Logger.Log("Moved lower coooldown");
                    indicatorString += "LC; ";
                    attack(attackTarget);
                }
                else{
                    attack(attackTarget);
                    retreat(enemySoldierCOM);
                }
            }
            else if(!enemySoldierCOM.equals(myLoc)){
                push(enemySoldierCOM);
            }
            else if(nearestEnemyInfo != null){
                push(nearestEnemyInfo.location);
            }
            else{
                runMovement();
            }
            nearbyActionEnemies = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam.opponent());
            attackTarget = findAttackTarget(nearbyActionEnemies);
            attack(attackTarget);
        }
        else{
            if(!enemySoldierCOM.equals(myLoc)){
                retreat(enemySoldierCOM);
            }
        }

        checkPossibleDeath();
    }


    // Follow the miner thats farthest away from archon
    public MapLocation nearestMinerLoc() throws GameActionException {
        int tempArchonIndex = comms.getClosestFriendlyArchonIndex();
        MapLocation closestArchonLoc = Util.intToMapLocation(rc.readSharedArray(tempArchonIndex));
        MapLocation nearestMiner = null;
        int closestDist = 0;
        MapLocation minerLoc;
        for(int i = nearbyFriendlies.length; i-- > 0; ){
            if(nearbyFriendlies[i].type != RobotType.MINER){
                continue;
            }
            minerLoc = nearbyFriendlies[i].location;

            int dist = closestArchonLoc.distanceSquaredTo(minerLoc);
            if(dist > closestDist){
                closestDist = dist;
                Direction awayFromArchon = closestArchonLoc.directionTo(minerLoc);
                // Change direction so that you don't run into a wall
                MapLocation targetLoc = minerLoc.add(awayFromArchon).add(awayFromArchon).add(awayFromArchon);
                boolean outOfBoundsX = targetLoc.x < 0 || targetLoc.x >= mapWidth;
                boolean outOfBoundsY = targetLoc.y < 0 || targetLoc.y >= mapHeight;
                if(outOfBoundsX && outOfBoundsY){
                    awayFromArchon = awayFromArchon.opposite();
                }
                else if(outOfBoundsX){
                    if(minerLoc.y > closestArchonLoc.y){
                        awayFromArchon = Direction.NORTH;
                    }
                    else{
                        awayFromArchon = Direction.SOUTH;
                    }
                }
                else if(outOfBoundsY){
                    if(minerLoc.x > closestArchonLoc.x){
                        awayFromArchon = Direction.EAST;
                    }
                    else{
                        awayFromArchon = Direction.WEST;
                    }
                }
                targetLoc = minerLoc.add(awayFromArchon).add(awayFromArchon).add(awayFromArchon);
                int x = targetLoc.x; int y = targetLoc.y;
                if(x < 4){
                    x = 4;
                }
                else if(x >= mapWidth - 4){
                    x = mapWidth - 4;
                }
                if(y < 4){
                    y = 4;
                }
                else if(y >= mapHeight - 4){
                    y = mapHeight - 4;
                }
                targetLoc = new MapLocation(x, y);
                nearestMiner = targetLoc;
            }
        }
        return nearestMiner;
    }

    public void retreat(MapLocation enemyCOM) throws GameActionException { // Movement method
        Logger.Log("Retreating from: " + enemyCOM.toString());
        indicatorString += "RTR!; ";
        if(enemyCOM.equals(myLoc)){ // No enemies nearby, just retreat back to base
            System.out.println("Enemy COM is my loc but im still retreating? That's weird");
            nav.goTo(archonLoc);
        }
        else{
            // Determine if moving backwards is worth it
            int maxRubbleAllowed = rc.senseRubble(myLoc);
            Direction away = myLoc.directionTo(enemyCOM).opposite();
            MapLocation retreatTarget = myLoc.add(away).add(away).add(away).add(away);
            boolean retreated = moveForwardSafely(retreatTarget, maxRubbleAllowed);
            if(!retreated){ // Else, just get the hell outta there
                nav.goTo(retreatTarget);
            }
        }
    }

    public void push(MapLocation pushLoc) throws GameActionException { // Movement method
        Logger.Log("Pushing towards: " + pushLoc.toString());
        // Determine if moving backwards is worth it
        int maxRubbleAllowed = rc.senseRubble(myLoc);
        moveForwardSafely(pushLoc, maxRubbleAllowed);
    }

    public boolean attack(RobotInfo toAttack) throws GameActionException { // Action method
        if(toAttack == null){
            return false;
        }
        Logger.Log("Trying to attack: " + toAttack.location.toString());
        if(!rc.isActionReady()){
            indicatorString += "NATK; ";
            return false;
        }
        if (rc.canAttack(toAttack.location)) {
            // Check if you have more friendly soldiers than enemy soldiers
            rc.attack(toAttack.location);
            indicatorString += "ATK: " + toAttack.location.toString() + "; ";
            lastAttackLoc = toAttack.getLocation();
            // Check if you killed an archon, and if you did, update comms
            if(toAttack.type == RobotType.ARCHON && toAttack.team == myTeam.opponent() && !rc.canSenseRobot(toAttack.ID)){
                System.out.println("HOLY SHIT I JUST KILLED THE ARCHON");
                comms.updateEnemyArchonDeath(toAttack.ID, toAttack.location);
            }
            return true;
        }
        return false;
    }

    public RobotInfo findAttackTarget(RobotInfo[] nearbyEnemies) throws GameActionException {
        RobotInfo toAttack = null;
        int oppTypeIndex = 20;
        int health = Integer.MIN_VALUE;
        for(int i = 0; i < nearbyEnemies.length; i++){
            RobotInfo info = nearbyEnemies[i];
            if(!rc.canAttack(info.location)){
                continue; // Can't attack (prolly cuz too far)
            }
            int currOppTypeIndex = Util.getArrayIndex(priorityOrder, info.type);
            int currHealth = info.getHealth();

            // Find the best enemy to attack based on priority order
            if (currOppTypeIndex < oppTypeIndex || (currOppTypeIndex == oppTypeIndex && currHealth > health)) {
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

    public boolean moveForwardSafely(MapLocation target, int maxRubbleAllowed) throws GameActionException { // Maybe instead do smth else
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
        if(lowestRubble > maxRubbleAllowed){ // Only move towards a direction w/ less than or equal to rubble
            return false;
        }
        rc.move(myLoc.directionTo(lowestLoc));
        indicatorString += "RS;";
        return true;
    }

    public boolean attackFromLowerCooldown(RobotInfo attackTarget) throws GameActionException {
        MapLocation targetLoc = attackTarget.location;
        double lowestHeuristic = rc.senseRubble(myLoc) + 10;
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
            double cooldown = rc.senseRubble(testLoc) + 10;
            double heuristic = cooldown;
            // If i can attack twice as fast, its worth walking into another enemy
            if(heuristic < lowestHeuristic){
                lowestLoc = testLoc;
                lowestHeuristic = heuristic;
            }
        }
        if(lowestLoc == null){ // Only move towards a direction w/ less than or equal to rubble
            return false;
        }
        rc.move(myLoc.directionTo(lowestLoc));
        return true;
    }

    public void runMovement() throws GameActionException { // Assumes there's no enemies near you
        if(currentTarget != null && myLoc.distanceSquaredTo(currentTarget) <= 4){
            targetLevel = 0; // Reached target and nothing there, so find a new target
        }

        resetTarget(); // Find best target at current round. 500 bytecode
        assert(currentTarget != null);
//        Logger.Log("Current target: " + currentTarget.toString());
        nav.goTo(currentTarget);
        rc.setIndicatorLine(myLoc, currentTarget, 0, 255, 0);
        indicatorString += "NAV: " + currentTarget.toString() + " becuz " + targetLevel;
    }

    public void resetTarget() throws GameActionException {
        // Check if the squad is currently attacking anyone
        MapLocation tempLoc = comms.getCurrAttackLoc();
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
        tempLoc = comms.getClosestEnemyArchonOnComms();
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
        targetLevel = 1;
        currentTarget = nav.getRandomMapLocation();
        Logger.Log("New current target: " + currentTarget.toString() + ", randomly chosen location");
    }

    public MapLocation scoutForEnemyArchons() throws GameActionException {
        // Otherwise search for one based on symmetry
        int symmetry = rc.readSharedArray(comms.SYMMETRY_IDX); // Index for symmetry in shared array
        if(symmetry == 0){
            symmetry = 7; // Smth messed up XD
        }

        MapLocation[] potentialScoutingLocs = new MapLocation[12];
        int potentialScoutingCount = 0;

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

        return null;
    }

}

