package cracked2;

import battlecode.common.*;

public class Builder extends Robot {

    MapLocation archonLoc = null;
    int archonIndex = -1;
    int savingUp = 0;
    RobotType buildType = null;
    MapLocation targetBuildSpot = null;

    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);

        buildType = whatBotShouldIBuild(); // see if the bot we need to build has changed
        // probably better to do this from Comms, where Archon will give miner initial instruction
        if(buildType == null){
            targetBuildSpot = null;
        }
        // Find the best build spot. Find a new one if that spot is now occupied.
        else if(targetBuildSpot == null || (rc.canSenseLocation(targetBuildSpot) && rc.isLocationOccupied(targetBuildSpot))){
            targetBuildSpot = findNearbyBuildSpot(buildType);
            System.out.println("TARGET BUILD SPOT: " + targetBuildSpot);
        }

        if(rc.isMovementReady()){
            runMovement();
        }
        if(rc.isActionReady()){
            runAction();
        }

        checkPossibleDeath();
    }

    public void runAction() throws GameActionException {
        if(buildType != null && targetBuildSpot != null && myLoc.distanceSquaredTo(targetBuildSpot) <= 2){
            if(savingUp == 0){
                int buildCost = buildType.buildCostLead;
                if(comms.saveUpLead(buildCost)){
                    savingUp = buildCost;
                }
            }
            int teamLead = rc.getTeamLeadAmount(myTeam);
            int leadAvailableToUse = teamLead + comms.getLeadSaveUp() - savingUp;
            if(leadAvailableToUse >= buildType.buildCostLead){
                if(Util.tryBuild(buildType, myLoc.directionTo(targetBuildSpot))){
                    if(savingUp != 0){
                        comms.removeLeadSaveUp();
                        savingUp = 0;
                    }
                    comms.addRobotCount(buildType, 1);
                    buildType = null;
                    targetBuildSpot = null;
                }
            }
        }

        repair();
    }

    public void runMovement() throws GameActionException {
        if(targetBuildSpot != null){
            if(myLoc.distanceSquaredTo(targetBuildSpot) > 2){
                nav.goTo(targetBuildSpot);
            }
        }
        else{
            MapLocation potentialRepairSpot = getNearbyRepairSpot();
            if(potentialRepairSpot != null){
                if(myLoc.distanceSquaredTo(potentialRepairSpot) < myType.actionRadiusSquared){
                    nav.goTo(potentialRepairSpot);
                }
                else{
                    nav.circle(potentialRepairSpot, 3, false);
                }
            }
            else{
                nav.circle(archonLoc, myType.visionRadiusSquared - 5, false);
            }
        }
    }

    public MapLocation getNearbyRepairSpot() throws GameActionException {
        // Search for nearby robot to repair and move to that location
        int closestDist = 10000;
        MapLocation closest = null;
        for(int i = 0; i < nearby.length; i++){
            RobotInfo info = nearby[i];
            int repairPriority = getRepairPriority(info.type);
            if(repairPriority == -1){
                continue;
            }
            int health = info.getHealth();
            int maxHealth = info.type.getMaxHealth(info.getLevel());
            if(health == maxHealth) {
                continue;
            }
            int dist = myLoc.distanceSquaredTo(info.getLocation());
            if(dist > closestDist) {
                continue;
            }
            closest = info.getLocation();
        }
        return closest;
    }

    // Build in grid location
    public MapLocation findNearbyBuildSpot(RobotType botType) throws GameActionException {
        if(botType == RobotType.WATCHTOWER) {
            int closestDist = 10000;
            MapLocation closestLoc = null;
            int startdx = -6;
            int startdy = -6;
            if (Math.abs(myLoc.x - archonLoc.x) % 3 == 1) {
                startdx = -7;
            }
            if (Math.abs(myLoc.x - archonLoc.x) % 3 == 2) {
                startdx = -5;
            }
            if (Math.abs(myLoc.y - archonLoc.y) % 3 == 1) {
                startdy = -7;
            }
            if (Math.abs(myLoc.y - archonLoc.y) % 3 == 2) {
                startdy = -5;
            }
            int enddx = startdx + 12;
            int enddy = startdy + 12;
            // Loop through nearby grid indices and check if any of them are available
            for (int dx = startdx; dx <= enddx; dx += 3) {
                for (int dy = startdy; dy <= enddy; dy += 3) {
                    MapLocation testLoc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                    if (rc.canSenseLocation(testLoc) && !rc.isLocationOccupied(testLoc)) {
                        int dist = myLoc.distanceSquaredTo(testLoc);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closestLoc = testLoc;
                        }
                    }
                }
            }
            return closestLoc;
        }
        else if(botType == RobotType.LABORATORY){
            MapLocation[] potentialPlotLocations = rc.getAllLocationsWithinRadiusSquared(myLoc, myType.visionRadiusSquared);
            MapLocation bestLoc = null;
            int bestDist = Integer.MAX_VALUE;
            int bestCooldown = Integer.MAX_VALUE;
            for(int i = 0; i < potentialPlotLocations.length; i++){
                MapLocation loc = potentialPlotLocations[i];
                if(!rc.canSenseLocation(loc)){
                    continue;
                }
                if(rc.isLocationOccupied(loc)){
                    continue;
                }
                int cooldown = rc.senseRubble(loc);
                int dist = myLoc.distanceSquaredTo(loc);
                if(cooldown < bestCooldown){
                    bestLoc = loc;
                    bestCooldown = cooldown;
                    bestDist = dist;
                }
                else if(cooldown == bestCooldown && dist < bestDist){
                    bestLoc = loc;
                    bestCooldown = cooldown;
                    bestDist = dist;
                }
            }
            return bestLoc;
        }
        return null;
    }

    public int getRepairPriority(RobotType type) throws GameActionException {
        switch(type){
            case ARCHON:
                return 0;
            case LABORATORY:
                return 1;
            case WATCHTOWER:
                return 2;
        }
        return -1;
    }

    public boolean repair() throws GameActionException {
        // Try repairing nearby watchtowers
        RobotInfo[] potentialTowers = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam);
        RobotInfo bestInfo = null;
        for (int i = 0; i < potentialTowers.length; i++) {
            RobotInfo info = potentialTowers[i];
            if(!rc.canRepair(info.location)){
                continue;
            }
            if(info.getHealth() == info.getType().getMaxHealth(info.getLevel())){
                continue;
            }
            if(bestInfo == null || isHigherRepairPriority(info, bestInfo)){
                bestInfo = info;
            }
        }
        if(bestInfo == null){
            return false;
        }
        rc.repair(bestInfo.location);
        indicatorString += "Repairing: " + bestInfo.location.toString();

        return true;
    }

    // Returns true if repairing robot a is higher in priority than repairing robot b
    public boolean isHigherRepairPriority(RobotInfo a, RobotInfo b) throws GameActionException {
        int repairPriorityA = getRepairPriority(a.type);
        int repairPriorityB = getRepairPriority(b.type);
        if(repairPriorityA < repairPriorityB){
            return true;
        }
        if(repairPriorityA > repairPriorityB){
            return false;
        }

        if(a.mode == RobotMode.PROTOTYPE && b.mode != RobotMode.PROTOTYPE){
            return true;
        }
        if(a.mode != RobotMode.PROTOTYPE && b.mode == RobotMode.PROTOTYPE){
            return false;
        }

        int healthDiffA = a.type.getMaxHealth(a.getLevel()) - a.getHealth();
        int healthDiffB = b.type.getMaxHealth(b.getLevel()) - b.getHealth();
        return healthDiffA > healthDiffB;
    }

    public RobotType whatBotShouldIBuild() throws GameActionException{
        if(comms.getRobotCount(RobotType.LABORATORY) == 0){
            return RobotType.LABORATORY;
        }
        return null;
    }

}
