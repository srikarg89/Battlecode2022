package hopefullyiwin;

import battlecode.common.*;


class SoldierHeuristic {
    double friendlyDamage;
    double friendlyHP;
    double enemyDamage;
    double enemyHP;
    public SoldierHeuristic(double friendlyDamage, double friendlyHP, double enemyDamage, double enemyHP){
        this.friendlyDamage = friendlyDamage;
        this.friendlyHP = friendlyHP;
        this.enemyDamage = enemyDamage;
        this.enemyHP = enemyHP;
    }
}

public class Soldier extends Robot {
    MapLocation archonLoc = null;
    int archonIndex = -1;

    // The last enemy that you attacked (so that you can chase them if they get away from your sight)
    int turnsSinceAttack = 0;
    MapLocation lastAttackLoc = null;
    int turnsSinceRetreat = 0;
    MapLocation retreatLoc = null;
    boolean needHealing = false;
    int bestArchonForHealingIdx = -1;

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
//        Logger.Log("lead amount: " + rc.getTeamLeadAmount(this.myTeam));
        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);
        MapLocation enemyCOM = Util.calculateEnemySoldierCOM(nearbyEnemies);
        comms.updateCurrAttackLoc(nearbyEnemies, enemyCOM);

        // If your bois are attacking an enemy more than the enemy is attacking ur bois, then go for the attack
        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam);
        RobotInfo[] nearbyActionEnemies = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam.opponent());
        RobotInfo[] nearbyVisionEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());

        RobotInfo nearestEnemyInfo = getNearestEnemy(nearbyVisionEnemies);
        SoldierHeuristic heuristic = checkSafe(nearbyFriendlies, nearbyVisionEnemies, nearestEnemyInfo); // 2300 bytecode for 31 nearby
        boolean inSafeZone = true;
        if(heuristic != null){
            inSafeZone = yamSafe(heuristic);
        }

        RobotInfo attackTarget = findAttackTarget(nearbyActionEnemies);

        Logger.Log("Action cooldown: " + rc.getActionCooldownTurns());
        Logger.Log("Movement cooldown: " + rc.getMovementCooldownTurns());

        if(rc.getHealth() < 7 && !needHealing){
            needHealing = true; // Go back to get healed
            bestArchonForHealingIdx = -1;
        }
        if(rc.getHealth() == myType.getMaxHealth(rc.getLevel()) && needHealing){
            needHealing = false; // Stop healing when ur at full health
            comms.writeSharedArray(bestArchonForHealingIdx + comms.ARCHON_HEALING_START_IDX, rc.readSharedArray(bestArchonForHealingIdx + comms.ARCHON_HEALING_START_IDX) - 1);
            bestArchonForHealingIdx = -1;
        }

//        needHealing = false;
        if(needHealing){
            indicatorString += "NH; ";
            if(bestArchonForHealingIdx == -1){
                bestArchonForHealingIdx = comms.findBestArchonForHealing();
                comms.writeSharedArray(bestArchonForHealingIdx + comms.ARCHON_HEALING_START_IDX, rc.readSharedArray(bestArchonForHealingIdx + comms.ARCHON_HEALING_START_IDX) + 1);
            }
            MapLocation targetLoc = Util.intToMapLocation(rc.readSharedArray(bestArchonForHealingIdx));
            indicatorString += targetLoc.toString() + "; ";
            if(myLoc.distanceSquaredTo(targetLoc) > RobotType.ARCHON.actionRadiusSquared){
                indicatorString += "GOTO; ";
                nav.goTo(targetLoc);
            }
            else{
                indicatorString += "CIRC; ";
                nav.circle(targetLoc, 4, RobotType.ARCHON.actionRadiusSquared - 1, true); // TODO Change this to some sort of a healingxaz queue
            }
        }
        else if(inSafeZone){
          // The strat is to get two shots for the enemy's one shot. ie, you try to stay on the boundary of the enemy's attack radius. Then, you push, attack, (they get a turn so they attack), then you attack again, then you retreat (back out of their range)
            indicatorString += "S; ";
            Logger.Log("In safe zone!");
            if(attackTarget != null){
                indicatorString += "AT; ";
                // If I can both move and attack, the check if I can move to a better loc and attack from there
                if(rc.isActionReady() && rc.isMovementReady()){
                    // If I can move to a lower cooldown loc and still attack from there, do that
                    boolean movedToLowerCooldown = attackFromLowerCooldown(attackTarget);
                    if(movedToLowerCooldown) {
                        Logger.Log("Moved lower coooldown");
                        indicatorString += "LC; ";
                        attack(attackTarget);
                    }
                    else { // Attack and back up (you know the drill)
                        attack(attackTarget);
                        if(attackTarget.type == RobotType.SOLDIER || attackTarget.type == RobotType.WATCHTOWER){
                            Direction dirToEnemyCOM = myLoc.directionTo(enemyCOM);
                            MapLocation awayFromEnemyCOM = myLoc.subtract(dirToEnemyCOM).subtract(dirToEnemyCOM).subtract(dirToEnemyCOM);
                            boolean retreated = moveForwardSafely(awayFromEnemyCOM);
                            if(retreated){
                                Logger.Log("Successfully retreated!");
                                indicatorString += "BACKUP1; ";
                            }
                            else{
                                Logger.Log("Couldn't move back safely");
                            }
//                            retreat(enemyCOM);
                        }
//                        retreat(enemyCOM);
                    }
                }
                else if(rc.isActionReady()) { // If you can attack, just attack
                    Logger.Log("Can't move, just attacking");
                    attack(attackTarget);
                }
                else{
                    // Well you can't attack, so back up so that they can't attack you
                    Logger.Log("Can't attack, jus retreating");
//                    retreat(enemyCOM);
                    // Retreat safely
                    Direction dirToEnemyCOM = myLoc.directionTo(enemyCOM);
                    MapLocation awayFromEnemyCOM = myLoc.subtract(dirToEnemyCOM).subtract(dirToEnemyCOM).subtract(dirToEnemyCOM);
                    boolean retreated = moveForwardSafely(awayFromEnemyCOM);
                    if(retreated){
                        indicatorString += "BACKUP2; ";
                    }
                }
            }
            else{
                indicatorString += "NOAT; ";
                // If you can both attack and move, do that
//                if(rc.isMovementReady() && rc.isActionReady() && (nearestEnemyInfo != null || (turnsSinceAttack < 3 && lastAttackLoc != null && turnsSinceRetreat > 3))){ // If you can see an enemy push him and attack him
                if(rc.isMovementReady() && rc.isActionReady() && (nearestEnemyInfo != null || (turnsSinceAttack < 3 && lastAttackLoc != null && turnsSinceRetreat > 3))){ // If you can see an enemy push him and attack him
                    indicatorString += "PUSH; ";
                    MapLocation pushLoc = lastAttackLoc;
                    if(nearestEnemyInfo != null){
                        pushLoc = nearestEnemyInfo.location;
                    }
                    push(enemyCOM, pushLoc, heuristic);
                    nearbyActionEnemies = rc.senseNearbyRobots(myType.actionRadiusSquared, opponent);
                    indicatorString += nearbyActionEnemies.length + "; ";
                    attackTarget = findAttackTarget(nearbyActionEnemies);
                    if(attackTarget == null){
                        indicatorString += "NULL; ";
                    }
                    else{
                        indicatorString += attackTarget.toString() + "; ";
                    }
                    attack(attackTarget);
                }
                else if(rc.isMovementReady()){ // Run regular scouting / comms movement
                    Logger.Log("Running regular movement");
                    if(turnsSinceRetreat < 3){
                        // Find friendly soldiers to pair up w/
                        goToNearestFriendly();
                    }
                    else{
                        runMovement();
                    }
                }
            }
        }
        else{
            indicatorString += "NS; ";
            Logger.Log("Not in safe zone!");
            attack(attackTarget);
            // Get the hell outta there
            retreat(enemyCOM);
            if(!inSafeZone){
                turnsSinceRetreat = 0;
                retreatLoc = enemyCOM;
            }
            // TODO Consider retreating towards friendlies as well as away from enemies
        }
        checkPossibleDeath();
        turnsSinceAttack++;
        turnsSinceRetreat++;
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
            if(Math.sqrt(dist) < (double)Math.max(rc.getMapWidth(), rc.getMapHeight()) / 6.0){
                continue;
            }
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

    public MapLocation nearestFriendlyDamageUnit() throws GameActionException {
        MapLocation nearestFriendly = null;
        int closestDist = 0;
        for(int i = nearbyFriendlies.length; i-- > 0; ){
            if(nearbyFriendlies[i].type != RobotType.SOLDIER || nearbyFriendlies[i].type != RobotType.WATCHTOWER){
                continue;
            }
            int dist = myLoc.distanceSquaredTo(nearbyFriendlies[i].location);
            if(dist > closestDist){
                closestDist = dist;
                nearestFriendly = nearbyFriendlies[i].location;
            }
        }
        if(nearestFriendly != null){
            return nearestFriendly;
        }
        int tempArchonIndex = comms.getClosestFriendlyArchonIndex();
        MapLocation closestArchonLoc = Util.intToMapLocation(rc.readSharedArray(tempArchonIndex));
        return closestArchonLoc;
    }

    // TODO MINERS SHOULD CALL FOR HELP!!

    public void goToNearestFriendly() throws GameActionException {
//        runMovement();
        nav.goTo(nearestFriendlyDamageUnit());
    }

    public void retreat(MapLocation enemyCOM) throws GameActionException { // Movement method
        Logger.Log("Retreating from: " + enemyCOM.toString());
        indicatorString += "RTR!; ";
        if(enemyCOM.equals(myLoc)){ // No enemies nearby, just retreat back to base
            nav.goTo(archonLoc);
        }
        else{
            // Try retreating safely
            Direction away = myLoc.directionTo(enemyCOM).opposite();
            MapLocation retreatTarget = myLoc;
            for(int i = 4; i-- > 0; ){
                if(!Util.onTheMap(retreatTarget.add(away))){
                    break;
                }
                retreatTarget = retreatTarget.add(away);
            }
            Direction retreatDir = nav.getBFSDirection(retreatTarget);
            if(retreatDir == null){
                System.out.print("RETREAT: ");
                System.out.println(retreatDir);
                System.out.println(myLoc.toString());
                System.out.println(retreatTarget.toString());
                int closestArchonIdx = comms.getClosestFriendlyArchonIndex();
                MapLocation closestArchonLoc = Util.intToMapLocation(rc.readSharedArray(closestArchonIdx));
                nav.goToFuzzy(closestArchonLoc);
            }
            else if(myLoc.distanceSquaredTo(enemyCOM) < myLoc.add(retreatDir).distanceSquaredTo(enemyCOM)){
                Util.tryMove(retreatDir);
            }
//            boolean retreated = moveForwardSafely(retreatTarget);
//            if(!retreated){ // Else, just get the hell outta there
////                nav.goTo(retreatTarget);
//            }
        }
    }

    public void push(MapLocation enemyCOM, MapLocation bestAttackTarget, SoldierHeuristic heuristic) throws GameActionException { // Movement method
        Logger.Log("Pushing towards: " + enemyCOM.toString());
        if(enemyCOM.equals(myLoc)){ // No enemy soldiers nearby, go after whatever else there is
            nav.goTo(bestAttackTarget);
        }
        else{
            moveForwardSafely(enemyCOM);
        }
//            double currCooldown = rc.senseRubble(myLoc) + 10.0;
//            double teammateDamage = heuristic.friendlyDamage - (myType.damage / currCooldown);
//            MapLocation lowestLoc = null;
//            Direction targetDir = myLoc.directionTo(enemyCOM);
//            Direction[] testDirs = {targetDir, targetDir.rotateLeft(), targetDir.rotateRight(), targetDir.rotateLeft().rotateLeft(), targetDir.rotateRight().rotateRight()};
//            for(int i = 0; i < testDirs.length; i++){
//                MapLocation testLoc = myLoc.add(testDirs[i]);
//                if(!rc.canSenseLocation(testLoc)){
//                    continue;
//                }
//                if(!rc.canMove(myLoc.directionTo(testLoc))){
//                    continue;
//                }
//                double newCooldown = rc.senseRubble(testLoc) + 10.0;
//                double newFriendlyDamage = teammateDamage + (myType.damage / newCooldown);
//                SoldierHeuristic newHeuristic = new SoldierHeuristic(newFriendlyDamage, heuristic.friendlyHP, heuristic.enemyDamage, heuristic.enemyHP);
//                if(yamSafe(newHeuristic) && lowestLoc != null){
//                    lowestLoc = testLoc;
//                }
//            }
//            if(lowestLoc == null){ // Only move towards a direction w/ less than or equal to rubble
//                return;
//            }
//            rc.move(myLoc.directionTo(lowestLoc));
//        }
    }

    public boolean yamSafe(SoldierHeuristic heuristic){
        double myTurnsNeeded = heuristic.enemyHP / heuristic.friendlyDamage;
        double enemyTurnsNeeded = heuristic.friendlyHP / heuristic.enemyDamage;
//        System.out.println("Friendly HP: " + friendlyHP + ", DMG: " + friendlyDamage + ", Enemy HP: " + enemyHP + ", DMG: " + enemyDamage);
        indicatorString += "MT: " + (int)myTurnsNeeded + ", ET: " + (int)enemyTurnsNeeded + "; ";

        // 1.2 simply because im ballsy and wanna go for it
        boolean inSafeZone = myTurnsNeeded <= enemyTurnsNeeded * 1.2; // If you can kill them faster than they can kill you, return true
        return inSafeZone;
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
            turnsSinceAttack = 0;
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
        MapLocation currLoc = rc.getLocation(); // Might've been updated cuz I might've just moved TODO update myLoc
        RobotInfo toAttack = null;
        int oppTypeIndex = 20;
        int health = Integer.MAX_VALUE;
        for(int i = 0; i < nearbyEnemies.length; i++){
            RobotInfo info = nearbyEnemies[i];
//            if(!rc.canAttack(info.location)){
//                continue; // Can't attack (prolly cuz too far)
//            }
            if(currLoc.distanceSquaredTo(info.location) > myType.actionRadiusSquared){
                continue;
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

    // TODO Count the # of soldiers on their front lines? I'm alr kinda doing that, but maybe comm that info so that everyone's aware of how fucked you are?

    public SoldierHeuristic checkSafe(RobotInfo[] nearbyFriendlies, RobotInfo[] dangeorusEnemies, RobotInfo nearestEnemyInfo) throws GameActionException { // TODO: Maybe only check # of attackers on the robot closest to you?
        // your attack isn't ready, then don't engage

        if(nearestEnemyInfo == null){ // No enemies nearby, we safe
            indicatorString += "NE1; ";
            return null;
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
            else if(info.type == RobotType.ARCHON){
//                friendlyDamage -= info.type.damage / repairCooldown;
                double damageDiff = 2.0 / (rc.senseRubble(info.location) + 10.0);
                if(friendlyDamage <= damageDiff){
                    friendlyDamage = 0;
                }
                else{
                    friendlyDamage -= damageDiff;
                }
//                enemyHP += info.getHealth();
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
            if(info.type == RobotType.ARCHON){ // NOTE: Archons can't attack, but this just makes you more likely to wanna protect your own archon
//                friendlyHP += info.getHealth();
                double damageDiff = 2.0 / (rc.senseRubble(info.location) + 10.0);
                if(enemyDamage <= damageDiff){
                    enemyDamage = 0;
                }
                else{
                    enemyDamage -= damageDiff;
                }
            }
        }

        if(enemyHP == 0 || enemyDamage == 0){
            indicatorString += "NE2; ";
            return null;
        }

        double myAttackCooldown = rc.senseRubble(myLoc) + 10;
        friendlyDamage += myType.damage / myAttackCooldown;
        friendlyHP += rc.getHealth();

        SoldierHeuristic heuristic = new SoldierHeuristic(friendlyDamage, friendlyHP, enemyDamage, enemyHP);
        return heuristic;
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

    // TODO: Fix this
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
            double newEnemies = numNewEnemiesAttackableAfterMoving(testDirs[i]) + 1;
            double cooldown = rc.senseRubble(testLoc) + 10;
            double heuristic = newEnemies * cooldown;
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
//        tempLoc = comms.getClosestEnemyArchonOnComms();
        tempLoc = nearestMinerLoc();
//        tempLoc = null;
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
//        tempLoc = nearestMinerLoc();
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

    int numNewEnemiesAttackableAfterMoving(Direction dir) throws GameActionException {
        MapLocation[] checkLocs = Util.getSenseableLocsAfterMoving(dir);
        int newBots = 0;
        for(int i = 0; i < checkLocs.length; i++){
            MapLocation loc = checkLocs[i];
            if(loc == null){
                continue;
            }
            if(!rc.canSenseLocation(loc)){
                continue;
            }
            RobotInfo info = rc.senseRobotAtLocation(loc);
            if(info == null){
                continue;
            }
            if(info.team == opponent && info.type == RobotType.SOLDIER){
                newBots++;
            }
        }
        return newBots;
    }


}

