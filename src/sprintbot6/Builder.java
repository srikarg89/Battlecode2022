package sprintbot6;

import battlecode.common.*;

public class Builder extends Robot {


    MapLocation archonLoc = null;
    int archonIndex = -1;

    MapLocation targetBuildSpot = null;
    RobotType[] repairPriorityOrder = {RobotType.ARCHON, RobotType.WATCHTOWER, RobotType.LABORATORY};



    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }


    public void run() throws GameActionException {
        super.run();

        if (archonIndex == -1) {
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);

        // CODE TO BUILD LABORATORIES,
        // TODO: need to use comms to only have a few builders try to build a laboratory
//        // if we are near lab build location
//        if(laboratoryBuildLocation != null && myLoc.distanceSquaredTo(laboratoryBuildLocation)<4){
//            Direction dir = myLoc.directionTo(archonLoc);           // build in direction opposite of the home archon (closer to opposing soldiers)
//            Direction[] buildDirections = nav.closeDirections(dir.opposite());
//            Direction build_dir = Util.tryBuild(RobotType.LABORATORY, buildDirections);
//            if(build_dir != Direction.CENTER) {      // yay we built a laboratory :D
//                laboratoryBuildLocation = null;
//
//            }
//        }
//
//        // all existing builders try building a laboratory every 100 rounds if we have enough leead
//        //TODO: don't move all existing builders, only get one or two of alive builders
//        else if((laboratoryBuildLocation != null || rc.getRoundNum()%100 == 0 && rc.getTeamLeadAmount(rc.getTeam()) > RobotType.LABORATORY.buildCostLead*3)){
//            if(laboratoryBuildLocation == null) {
//                laboratoryBuildLocation = Util.getClosestCorner(archonLoc);
//            }
//            nav.minDistToSatisfy = 9;
//            nav.goTo(laboratoryBuildLocation);
//
//        }


        //TODO: make builders retreat from enemies if we are getting overpowered
        boolean enoughLeadForWatchTower = rc.getTeamLeadAmount(rc.getTeam()) > RobotType.WATCHTOWER.buildCostLead * 2;

        if (rc.isActionReady()) {
            boolean repaired = repair(); // TODO: we already find a potentialRepairSpot in movement code, so just use that instead of this method (which is kinda expensive)
            if(!repaired && targetBuildSpot != null && myLoc.distanceSquaredTo(targetBuildSpot) <= 2){      //time to build
                Direction dir = myLoc.directionTo(targetBuildSpot);
                Direction[] buildDirections = {dir};
                Direction build_dir = Util.tryBuild(RobotType.WATCHTOWER, buildDirections);
                if(build_dir.equals(dir)){       // we've built the watchtower
                    targetBuildSpot = null;     // find a new target buildSpot on the next turn
                }
            }
        }

        if (targetBuildSpot == null && enoughLeadForWatchTower) {
            targetBuildSpot = findNearbyBuildSpot();
        }

        // movement
        if (rc.isMovementReady()){
            if (myLoc.distanceSquaredTo(archonLoc) <= 2) {            // move out of the way of the spawning archon
                Logger.Log("Too close to archon, so moving away");
                Direction away = myLoc.directionTo(archonLoc).opposite();
                nav.goTo(myLoc.add(away).add(away).add(away));
            }
            else if (targetBuildSpot != null && myLoc.distanceSquaredTo(targetBuildSpot) > 2 && enoughLeadForWatchTower) {        //if we are currently travelling to targetBuildSpot
                nav.goTo(targetBuildSpot);}
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
    }



//        boolean repaired = repair();
//        if(!repaired){
//            // Check if your build location is still available
//                if(rc.getTeamLeadAmount(rc.getTeam()) > RobotType.WATCHTOWER.buildCostLead * 2){      // time to build a tower :D
//                    Logger.Log("Building watchtower");
//                    buildWatchtower();
//                }
//                else{
//                    Logger.Log("Repairing nearby");
//                    repairNearby();
//                }
//            }
//        }

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

//    public void buildWatchtower() throws GameActionException {
//        targetBuildSpot = findNearbyBuildSpot();
//        if (targetBuildSpot == null) {
//            Logger.Log("Didn't find a build spot! Circling around build spot"); // TODO: Change this to circling around Archon
//            nav.circle(archonLoc, 10, true);
//            return;
//        }
//        else{
//            Logger.Log("Calculated a target build spot: " + targetBuildSpot.toString());
//        }
//        rc.setIndicatorString("Target build spot: " + targetBuildSpot.toString());
//        if (myLoc.distanceSquaredTo(targetBuildSpot) > 2) {
//            Logger.Log("Going towards target build spot: " + targetBuildSpot.toString());
//            nav.goTo(targetBuildSpot);
//        } else {
//            Direction dir = myLoc.directionTo(targetBuildSpot);           // build in direction opposite of the home archon (closer to opposing soldiers)
//            Direction[] buildDirections = {dir};
//            Direction build_dir = Util.tryBuild(RobotType.WATCHTOWER, buildDirections);
//        }
//    }

    // Build in grid location
    public MapLocation findNearbyBuildSpot() throws GameActionException {
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
//                Logger.Log("Potential Test Loc: " + testLoc.toString());
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



    public RobotInfo findRepairTarget(RobotInfo[] nearbyFriendlies) throws GameActionException {
        MapLocation currLoc = rc.getLocation(); // Might've been updated cuz I might've just moved TODO update myLoc
        RobotInfo toRepair = null;
        int botTypeIndex = 20;
        int health = Integer.MAX_VALUE;

        for(int i = 0; i < nearbyFriendlies.length; i++){
            RobotInfo info = nearbyFriendlies[i];

            if(currLoc.distanceSquaredTo(info.location) > myType.actionRadiusSquared){
                continue;
            }

            int currBotTypeIndex = Util.getArrayIndex(repairPriorityOrder, info.type);
            int currHealth = info.getHealth();

            // curret bot is already at max health, no need to repair
            if(currHealth >= info.getType().getMaxHealth(info.getLevel())){
                continue;
            }

            // Find the best bot to repair based on priority order. Order: archon, watchtower, laboratory
            if (currBotTypeIndex < botTypeIndex || (currBotTypeIndex == botTypeIndex && currHealth < health)) {
                toRepair = info;
                botTypeIndex = currBotTypeIndex;
                health = currHealth;
            }
        }
        return toRepair;
    }

    public boolean repair () throws GameActionException {
        // Try repairing nearby buildings
        boolean repaired = false;
        RobotInfo[] potentialTowers = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam);

        RobotInfo toRepair = findRepairTarget(potentialTowers);
        if (toRepair != null) {
            int max_health = toRepair.getType().getMaxHealth(toRepair.getLevel());
            while (toRepair.getHealth() < max_health && rc.isActionReady()) {
                repaired = true;
                rc.repair(toRepair.location);
                rc.setIndicatorString("Repairing da boi");
            }
        }
        return repaired;
    }

//    public void retreat(MapLocation enemyCOM) throws GameActionException { // Movement method
//        Logger.Log("Retreating from: " + enemyCOM.toString());
//        if(enemyCOM.equals(myLoc)){ // No enemies nearby, just retreat back to base
//            nav.goTo(archonLoc);
//        }
//        else{
//            Direction away = myLoc.directionTo(enemyCOM).opposite();
//            nav.goTo(myLoc.add(away).add(away).add(away).add(away));
//        }
//    }
}
