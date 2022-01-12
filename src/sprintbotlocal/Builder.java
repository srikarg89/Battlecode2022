package sprintbot-dev;

import battlecode.common.*;

public class Builder extends Robot {


    MapLocation archonLoc = null;
    MapLocation lastBuiltTower = null;
    int archonIndex = -1;
    boolean needToRepair = false;

    MapLocation targetBuildSpot = null;

    MapLocation[] watchTowerBuildSpots = new MapLocation[8];
    int numSpots = 0;
    int spotIndex = 0;  // index value used to mark which build spot we're navigating to
    boolean canBuild = false;
    int direction_multiplier = rc.getRoundNum()/100*4;

    int mid_x = rc.getMapWidth()/2;
    int mid_y = rc.getMapHeight()/2;
    MapLocation middle = new MapLocation(mid_x, mid_y);



    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }


    public void run() throws GameActionException {
        super.run();
//        System.out.println(numSpots +", " + spotIndex);
//        rc.setIndicatorString(""+needToRepair);
//        desiredDistanceFromArchonSquared = rc.getRobotCount() / 5;  //arbitrary, need to fix


        if(archonIndex == -1){
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

        if(myLoc.distanceSquaredTo(archonLoc) <= 2){
            Logger.Log("Too close to archon, so moving away (towards center)");
            MapLocation map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
            nav.goTo(map_center);
        }
        else{
            boolean repaired = repair();
            // TODO: Move out of the archon's way when spawned
            if(!repaired){
                // Check if your build location is still available
                if(rc.getTeamLeadAmount(rc.getTeam()) > RobotType.WATCHTOWER.buildCostLead * 2){      // time to build a tower :D
                    Logger.Log("Building watchtower");
                    bulidWatchtower();
                }
                else{
                    Logger.Log("Repairing nearby");
                    repairNearby();
                }
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

    public void bulidWatchtower() throws GameActionException {
//        if(rc.canSenseLocation(targetBuildSpot) && rc.isLocationOccupied(targetBuildSpot)){
//            targetBuildSpot = null;
//        }
//        if(targetBuildSpot == null){ // Choose a new build spot
//            targetBuildSpot = findNearbyBuildSpot();
//            if(targetBuildSpot == null){
//                MapLocation map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
//                targetBuildSpot = Util.multiplyDirection(this.archonLoc, myLoc.directionTo(map_center), 10);
//            }
//        }
        targetBuildSpot = findNearbyBuildSpot();
        if (targetBuildSpot == null) {
            Logger.Log("Didn't find a build spot! Going to center instead"); // TODO: Change this to circling around Archon
            nav.circle(archonLoc, 10, true);
            return;
//            MapLocation map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
//            targetBuildSpot = Util.multiplyDirection(this.archonLoc, myLoc.directionTo(map_center), 10);
        }
        else{
            Logger.Log("Calculated a target build spot: " + targetBuildSpot.toString());
        }
        rc.setIndicatorString("Target build spot: " + targetBuildSpot.toString());
        if (myLoc.distanceSquaredTo(targetBuildSpot) > 2) {
            Logger.Log("Going towards target build spot: " + targetBuildSpot.toString());
            nav.goTo(targetBuildSpot);
        } else {
            Direction dir = myLoc.directionTo(targetBuildSpot);           // build in direction opposite of the home archon (closer to opposing soldiers)
            Direction[] buildDirections = {dir};
//            Direction[] buildDirections = nav.closeDirections(dir);
            Direction build_dir = Util.tryBuild(RobotType.WATCHTOWER, buildDirections);
            if (build_dir != Direction.CENTER) {      // yay we built a tower
                needToRepair = true;                // need to get the tower we just built to max health
                lastBuiltTower = myLoc.add(build_dir);
                canBuild = false;                   // set can build to false so we move to the next build location
                spotIndex++;                        // increment index in array to next spot
            }
        }
    }

    // Build in grid location
    public MapLocation findNearbyBuildSpot () throws GameActionException {
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

    // Build a 3x3 spaced out watchtower fortress
    public void generateBuildSpots ( int directionMultiplier) throws GameActionException {
        //generates potential build locations in all cardinal directions going outwards from home archon
        // builder can go to every one and try to build a watchtower there
//        System.out.println("hello");
        Direction[] directions = Util.closeDirections(myLoc.directionTo(middle)); // build towers towards the middle of the map
        // TODO: need to change to the direction of enemy archons
        MapLocation closestArchon = enemyArchonOnComms();
        if (closestArchon != null) {
            directions = Util.closeDirections(myLoc.directionTo(closestArchon)); // build towers towards the middle of the map

        }
        this.numSpots = 0;
        for (int i = 2; i < 7; i++) {        // go in the first 4 best directions
            Direction direction = directions[i];
            MapLocation potentialBuildSpot = Util.multiplyDirection(this.archonLoc, direction, directionMultiplier);

            // check if this is a valid mapLocation
            if (potentialBuildSpot.x < rc.getMapWidth() && potentialBuildSpot.x > 0 && potentialBuildSpot.y < rc.getMapHeight() && potentialBuildSpot.y > 0) {
                this.watchTowerBuildSpots[numSpots] = potentialBuildSpot;
                numSpots++;
            }
        }
//        System.out.println(numSpots);
    }


    public boolean repair () throws GameActionException {
        // Try repairing nearby watchtowers
        boolean repaired = false;
        RobotInfo[] potentialTowers = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam);
        for (int i = 0; i < potentialTowers.length; i++) {
            if (potentialTowers[i].type != RobotType.WATCHTOWER) {
                continue;
            }
            int max_health = RobotType.WATCHTOWER.getMaxHealth(potentialTowers[i].getLevel());
            if (potentialTowers[i].getHealth() >= max_health) {
                continue; // No need to repair this boi
            }
            while (potentialTowers[i].getHealth() < max_health && rc.isActionReady()) {
                repaired = true;
                rc.repair(potentialTowers[i].location);
                rc.setIndicatorString("Repairing da boi");
                ;
            }
        }
        return repaired;
    }

    public MapLocation enemyArchonOnComms () throws GameActionException {
        MapLocation closestEnemy = null;
        int closestDist = Integer.MAX_VALUE;
        for (int i = 8; i < 12; i++) {
            int val = rc.readSharedArray(i);
            if (val != 0 && val != comms.MAX_COMMS_VAL) {
                MapLocation enemyLoc = Util.intToMapLocation(val);
                int enemyDist = myLoc.distanceSquaredTo(enemyLoc);
                if (enemyDist < closestDist) {
                    closestDist = enemyDist;
                    closestEnemy = enemyLoc;
                }
            }
        }
        return closestEnemy;
    }
}
