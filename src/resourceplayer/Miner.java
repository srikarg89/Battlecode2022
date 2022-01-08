package resourceplayer;

import battlecode.common.*;

public class Miner extends Robot {

    MapLocation archonLoc = null;
    MapLocation mineLocation = null;
    Direction spawnDir = null;

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
//        Logger.Log("Running constructor!");
    }

    public void run() throws GameActionException {
        super.run();
        //lifted from miner code
        if (archonLoc == null) {
            for (Direction dir : Navigation.directions) {
                MapLocation testLoc = myLoc.add(dir);
                if (rc.canSenseRobotAtLocation(testLoc)) {
                    RobotInfo info = rc.senseRobotAtLocation(testLoc);
                    if (info.getType() == RobotType.ARCHON && info.getTeam() == myTeam) {
                        archonLoc = testLoc;
//                        System.out.println("Found my archon loc: " + archonLoc.toString());
                    }
                }
            }
        }
        assert(archonLoc != null);

        // TODO: If you're blocking the way of a fellow miner, move forward so that he can help you mine
        // Movement strat: if you're right next to a miner and there's mineable blocks if you separate yourself, then separate yourself.

        // Calculate mineLocation
//        Logger.Log("MINER BYTECODE BEFORE: " + Clock.getBytecodesLeft());
        MapLocation mineLocation = findClosestMine();
//        Logger.Log("MINER BYTECODE HERE: " + Clock.getBytecodesLeft());

        // If there's nowhere that I can sense to mine, find a new mineLocation
        if(mineLocation == null){
//            Logger.Log("MineLocation is null");
            // Do some sort of intelligent scouting stuff
            MapLocation target = Util.multiplyDirection(myLoc, spawnDir, Math.max(rc.getMapWidth(), rc.getMapHeight()));
            // Move away from spawning archon
            nav.goTo(target);
        }
        else if(myLoc.distanceSquaredTo(mineLocation) > 2){
//            Logger.Log("Going towards mineLocation");
            nav.goTo(mineLocation);
        }
        else if(rc.isMovementReady()){
            // Spreading out code
//            int nearby = rc.senseNearbyRobots(2, myTeam).length;
            int nearby = getAdjacentTeammatesCount(myLoc, true);
            Direction bestMoveDir = Direction.CENTER;
            for(Direction dir : Navigation.directions){
                MapLocation newCenter = myLoc.add(dir);
                if(!rc.canMove(dir)){
                    continue;
                }
                if(!checkMineable(newCenter)){
                    continue;
                }
//                Logger.Log("BYTECODE TEST A: " + Clock.getBytecodesLeft());
//                int robotsNearNewCenter = rc.senseNearbyRobots(newCenter, 2, myTeam).length;
                int robotsNearNewCenter = getAdjacentTeammatesCount(newCenter, true);
                if(robotsNearNewCenter < nearby){
                    nearby = robotsNearNewCenter;
                    bestMoveDir = dir;
                }
//                Logger.Log("BYTECODE TEST B: " + Clock.getBytecodesLeft());
            }
            if(bestMoveDir != Direction.CENTER){
                Util.tryMove(bestMoveDir);
            }
        }

//        Logger.Log("WHATS YEETING THE MINER BYTECODE: " + Clock.getBytecodesLeft());

        // Try to mine on squares around us.
//        Logger.Log("Miner bytecode left: " + Clock.getBytecodesLeft());
        if(mineLocation != null){
//            Logger.Log("Trying to mine at: " + mineLocation.toString());
            tryMine(mineLocation);
        }

    }

    public int getAdjacentTeammatesCount(MapLocation center, boolean cardinal) throws GameActionException {
        Direction[] dirs = Navigation.directions;
        if(cardinal){
            dirs = Navigation.cardinalDirections;
        }
        int count = 0;
        for(int i = dirs.length; i-- > 0; ){
            MapLocation adjLoc = center.add(dirs[i]); // Can use rc.isLocationOccupied to save bytecode
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
//        Logger.Log("Bytecode at start of tryMine: " + Clock.getBytecodesLeft());
        while (rc.canMineGold(mineLocation)) {
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation)) {
            rc.mineLead(mineLocation);
        }
    }

}
