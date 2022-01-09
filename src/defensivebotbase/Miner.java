package defensivebotbase;

import battlecode.common.*;

public class Miner extends Robot {

    MapLocation archonLoc = null;
    MapLocation mineLocation = null;
    Direction spawnDir = null;
    MapLocation target = null;

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        Logger.Log("Running constructor!");
    }

    public void run() throws GameActionException {
        super.run();
//        Logger.Log("Round number: " + rc.getRoundNum());
        // Find the location of the archon that spawned you
        if(archonLoc == null) {
            for(int i = 0; i < numFriendlyArchons; i++){
                if(myLoc.distanceSquaredTo(friendlyArchons[i]) <= 2){
                    archonLoc = friendlyArchons[i];
                    spawnDir = archonLoc.directionTo(myLoc);
                }
            }
        }
        assert(archonLoc != null);

        // TODO: If you're blocking the way of a fellow miner, move forward so that he can help you mine
        // Movement strat: if you're right next to a miner and there's mineable blocks if you separate yourself, then separate yourself.

        // Calculate mineLocation
        comms.scanEnemyArchons();

        // If you're under attack (or you sense an enemy soldier), retreat!
        if(Util.countRobotTypes(nearby, RobotType.SOLDIER, myTeam.opponent()) > Util.countRobotTypes(nearby, RobotType.SOLDIER, myTeam)){
            nav.goTo(archonLoc);
        }

        MapLocation mineLocation = findClosestMine();

        // If there's nowhere that I can sense to mine, find a new mineLocation
        if(mineLocation == null){
            // Go scout for a mine location
            if(target != null && myLoc.distanceSquaredTo(target) <= myType.actionRadiusSquared){
                target = null; // Reset target since you've reached there
            }
            if(target == null){
                target = nav.getRandomMapLocation(); // Find new target
            }
            nav.goTo(target); // Go to target!
//            int currDistance = myLoc.distanceSquaredTo(archonLoc);
//            int scaledRadius = comms.getRobotCount(RobotType.SOLDIER)/numFriendlyArchons;
//
//            if(currDistance > scaledRadius/0.1 && Math.random() < 0.5)
//                nav.moveTowards(archonLoc);
//            else
//                nav.moveAwayFrom(archonLoc);
//                Util.tryMove(shuffled);

        }
        else if(myLoc.distanceSquaredTo(mineLocation) > 2){
            nav.goTo(mineLocation);
        }
        else if(rc.isMovementReady()){
            // Spreading out code
//            Logger.Log("Spreading out");
            int nearby = getAdjacentTeammatesCount(myLoc, true);
//            Logger.Log("Nearby: " + nearby);
            Direction bestMoveDir = Direction.CENTER;
            for(Direction dir : Navigation.directions){
                MapLocation newCenter = myLoc.add(dir);
                if(!rc.canMove(dir)){
                    continue;
                }
                if(!checkMineable(newCenter)){
                    continue;
                }
                int robotsNearNewCenter = getAdjacentTeammatesCount(newCenter, true);
                if(robotsNearNewCenter < nearby){
                    nearby = robotsNearNewCenter;
                    bestMoveDir = dir;
                }
            }
            if(bestMoveDir != Direction.CENTER){
                if(Util.tryMove(bestMoveDir)){
                    mineLocation = findClosestMine();
                }
            }
        }

        // Try to mine on squares around us.
        if(mineLocation != null){
            Logger.Log("Trying to mine at: " + mineLocation.toString());
            tryMine(mineLocation);
        }

    }

    public int getAdjacentTeammatesCount(MapLocation center, boolean cardinal) throws GameActionException {
        Direction[] dirs = Navigation.directions;
        if(cardinal){
            dirs = Navigation.cardinalDirections;
        }
        int count = 0;
//        Logger.Log("Dirs: " + dirs.length);
        for(int i = dirs.length; i-- > 0; ){
            MapLocation adjLoc = center.add(dirs[i]); // Can use rc.isLocationOccupied to save bytecode
//            Logger.Log("\tChecking adjacent location: " + adjLoc.toString());
            if(rc.canSenseLocation(adjLoc)){
                RobotInfo info = rc.senseRobotAtLocation(adjLoc);
                if(info == null){
                    continue;
                }
                if(info.team == myTeam){
                    count++;
                }
            }
        }
        return count;
    }

    // True if you can mine from a given area, false if you can't
    public boolean checkMineable(MapLocation center) throws GameActionException {
        for (int i = 0; i < Navigation.allDirections.length; i++) { // Maximum mine radius is 2
            MapLocation newLoc = center.add(Navigation.allDirections[i]);
            if(!rc.canSenseLocation(newLoc)){
                continue;
            }
            int lead = rc.senseLead(newLoc);
            int gold = rc.senseGold(newLoc);
            if(lead > 0 || gold > 0){
                return true;
            }
        }
        return false;
    }

    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestMine() throws GameActionException {
        MapLocation bestLoc = null;
        int bestDist = 100000;
        // Go to nearest gold mine
        MapLocation[] goldLocs = rc.senseNearbyLocationsWithGold(myType.visionRadiusSquared);
        for(int i = 0; i < goldLocs.length; i++){
            if(rc.senseGold(goldLocs[i]) == 1){ // Save for farming
                continue;
            }
            int dist = myLoc.distanceSquaredTo(goldLocs[i]);
            if(dist < bestDist){
                bestDist = dist;
                bestLoc = goldLocs[i];
            }
        }
        if(bestLoc != null){
            return bestLoc;
        }

        MapLocation[] leadLocs = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared);
//        Logger.Log("Num leadlocs found: " + leadLocs.length);
        for(int i = 0; i < leadLocs.length; i++){
            if(rc.senseLead(leadLocs[i]) == 1){ // Save for farming
                continue;
            }
            int dist = myLoc.distanceSquaredTo(leadLocs[i]);
            if(dist < bestDist){
                bestDist = dist;
                bestLoc = leadLocs[i];
            }
        }

        return bestLoc;
    }

    public void tryMine(MapLocation mineLocation) throws GameActionException {
        // TODO: If you reach the limit, then find a new spot to mine at
        while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {  // don't mine if therre's one lead so we can regenerate
            rc.mineLead(mineLocation);
        }
    }

}
