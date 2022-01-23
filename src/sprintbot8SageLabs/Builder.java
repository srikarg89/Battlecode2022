package sprintbot8SageLabs;

import battlecode.common.*;

public class Builder extends Robot {


    MapLocation archonLoc = null;
    int archonIndex = -1;

    MapLocation targetBuildSpot = null;
    RobotType buildType = null;
    boolean enoughLeadForBuild = false;


    RobotType[] repairPriorityOrder = {RobotType.ARCHON, RobotType.LABORATORY, RobotType.WATCHTOWER};
    final int MAX_LABORATORIES = 1;



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




        if(buildType == null){
            buildType = whatBotShouldIBuild();
            targetBuildSpot = findNearbyBuildSpot(buildType);
        }

        RobotType newBuildType = whatBotShouldIBuild(); // see if the bot we need to build has changed
        // probably better to do this from Comms, where Archon will give miner initial instruction
        if(newBuildType != buildType){
            buildType = newBuildType;
            targetBuildSpot = findNearbyBuildSpot(buildType);
        }


//        System.out.println(rc.getTeamLeadAmount(rc.getTeam()));
//        System.out.println(buildType);
//        System.out.println(buildType.buildCostLead);

        enoughLeadForBuild = rc.getTeamLeadAmount(rc.getTeam()) > buildType.buildCostLead * 2;

        // repairing / building
        if (rc.isActionReady()) {
            boolean repaired = repair();
            if(!repaired && targetBuildSpot != null && myLoc.distanceSquaredTo(targetBuildSpot) <= RobotType.BUILDER.actionRadiusSquared){      //time to build
                Direction dir = myLoc.directionTo(targetBuildSpot);
                Direction[] buildDirections = {dir};
                Direction build_dir = Util.tryBuild(buildType, buildDirections);
                if(build_dir.equals(dir)){       // we've built the watchtower
                    comms.addRobotCount(buildType, 1);
                    targetBuildSpot = null;     // find a new target buildSpot on the next turn
                }
            }
        }


        if (rc.isMovementReady()){
            if (myLoc.distanceSquaredTo(archonLoc) <= 2) {            // move out of the way of the spawning archon
                Logger.Log("Too close to archon, so moving away");
                Direction away = myLoc.directionTo(archonLoc).opposite();
                nav.goTo(myLoc.add(away).add(away).add(away));
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
            for(int i=Util.directions.length; i-->0; i++){
                MapLocation potLoc = archonLoc.add(Util.directions[i]);
                if(rc.canSenseLocation(potLoc)&& !rc.isLocationOccupied(potLoc)){
                    return potLoc;
                }
            }
            return archonLoc.add(Direction.EAST);
        }
        return null;
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
               indicatorString += "repairing building at " + toRepair.location.toString();
            }
        }
        return repaired;
    }

    public RobotType whatBotShouldIBuild() throws GameActionException{
        // tells you which type of bot (watchtower or laboratory) i should build
        if(rc.readSharedArray(comms.robotTypeToIndex(RobotType.LABORATORY)) < 1){
            return RobotType.LABORATORY;
        }
        else{
            return RobotType.WATCHTOWER;
        }
    }

}