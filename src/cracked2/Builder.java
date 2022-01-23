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
        else{
            targetBuildSpot = findNearbyBuildSpot(buildType);
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
        if(buildType != null && savingUp == 0){
            int buildCost = buildType.buildCostLead;
            if(comms.saveUpLead(buildCost)){
                savingUp = buildCost;
            }
        }

        boolean repaired = repair();
        if(!repaired && targetBuildSpot != null && myLoc.distanceSquaredTo(targetBuildSpot) <= RobotType.BUILDER.actionRadiusSquared){
            Direction dir = myLoc.directionTo(targetBuildSpot);
            if(Util.tryBuild(buildType, dir)){
                comms.addRobotCount(buildType, 1);
                targetBuildSpot = null;
            }
        }
    }

    public void runMovement() throws GameActionException {
        if (myLoc.distanceSquaredTo(archonLoc) <= 8){            // move out of the way of the spawning archon
            Direction away = myLoc.directionTo(archonLoc).opposite();
            nav.goTo(myLoc.add(away).add(away).add(away).add(away).add(away).add(away));
        }
        else if (targetBuildSpot != null && myLoc.distanceSquaredTo(targetBuildSpot) > RobotType.BUILDER.actionRadiusSquared && enoughLeadForBuild) {        //if we are currently travelling to targetBuildSpot
            nav.goTo(targetBuildSpot);
        }
        else if (targetBuildSpot == null) {       // we can't build a watchtower, so we will either try to repair a nearby building or circle our home archon
            MapLocation potentialRepairSpot = getNearbyRepairSpot();
            if(potentialRepairSpot == null){
                nav.circle(archonLoc, 10, true);
            }
            else{
                nav.goTo(potentialRepairSpot);
            }
        }

    }

    public void repairNearby() throws GameActionException {
        // Search for nearby robot to repair
        int closestDist = 10000;
        MapLocation closest = null;
        for(int i = 0; i < nearby.length; i++){
            RobotInfo info = nearby[i];
            if(info.type == RobotType.ARCHON || info.type == RobotType.WATCHTOWER || info.type == RobotType.LABORATORY){
                int health = info.getHealth();
                int maxHealth = info.type.getMaxHealth(info.getLevel());
                int dist = myLoc.distanceSquaredTo(info.getLocation());
                if(health <= maxHealth && dist < closestDist){
                    closestDist = dist;
                    closest = info.getLocation();
                }
            }
        }
        if(closest == null){
            return;
        }
        nav.goTo(closest);
    }

    public MapLocation getNearbyRepairSpot() throws GameActionException {
        // Search for nearby robot to repair and move to that location
        int closestDist = 10000;
        MapLocation closest = null;
        for(int i = 0; i < nearby.length; i++){
            RobotInfo info = nearby[i];
            if(info.type == RobotType.ARCHON || info.type == RobotType.WATCHTOWER || info.type == RobotType.LABORATORY){
                int health = info.getHealth();
                int maxHealth = info.type.getMaxHealth(info.getLevel());
                int dist = myLoc.distanceSquaredTo(info.getLocation());
                if(health <= maxHealth && dist < closestDist){
                    closestDist = dist;
                    closest = info.getLocation();
                }
            }
        }
        return closest;
    }

    // Build in grid location
    public MapLocation findNearbyBuildSpot(RobotType botType) throws GameActionException {
        // where should I build a watchtower?
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

        // where should I build a laboratory
        else if(botType == RobotType.LABORATORY){
            System.out.println("deciding building spot");
            for(int i = Util.directions.length; i-->0; ){
                MapLocation potLoc = archonLoc.add(Util.directions[i]);
                if(rc.canSenseLocation(potLoc)&& !rc.isLocationOccupied(potLoc)){
                    return potLoc;
                }
            }
            return archonLoc.add(Direction.EAST);
        }
        return null;
    }

    public boolean repair() throws GameActionException {
        // Try repairing nearby watchtowers
        boolean repaired = false;
        RobotInfo[] potentialTowers = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam);
        MapLocation bestToRepair = null;
        int bestHealth = -1;
        boolean repairingPrototype = false;
        for (int i = 0; i < potentialTowers.length; i++) {
            RobotInfo info = potentialTowers[i];
            if (info.type != RobotType.WATCHTOWER) {
                continue;
            }
            if(!rc.canRepair(info.location)){
                continue;
            }
            int max_health = RobotType.WATCHTOWER.getMaxHealth(info.getLevel());
            if (info.getHealth() >= max_health) {
                continue; // No need to repair this boi
            }
            if(repairingPrototype && info.mode != RobotMode.PROTOTYPE){
                continue;
            }
            if(!repairingPrototype){
                if(info.mode == RobotMode.PROTOTYPE){ // Prefer rebuilding prototype watchtowers
                    repairingPrototype = true;
                    bestToRepair = info.location;
                    bestHealth = info.getHealth();
                }
                else{
                    int currHealth = info.getHealth();
                    if(currHealth < bestHealth){ // Prioritize repairing non-prototype watchtowers with lower health
                        bestHealth = currHealth;
                        bestToRepair = info.location;
                    }
                }
            }
            else{
                int currHealth = info.getHealth();
                if(currHealth > bestHealth){ // Prioritize repairing prototype watchtowers with higher health (so that they can start attacking ASAP)
                    bestHealth = currHealth;
                    bestToRepair = info.location;
                }
            }
        }
        if(bestToRepair == null){
            return false;
        }
        rc.repair(bestToRepair);
        indicatorString += "Repairing: " + bestToRepair.toString();

        return repaired;
    }

    public RobotType whatBotShouldIBuild() throws GameActionException{
        if(comms.getRobotCount(RobotType.LABORATORY) == 0){
            return RobotType.LABORATORY;
        }
        return null;
    }

}
