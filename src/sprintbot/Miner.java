package sprintbot;

import battlecode.common.*;

// TODO: Revamp miner class. Just make a massive map of heuristics for each spot leadAvailable / (rubble + 10) + distance + (50 if adjacent friendly else 0)
// And then just go to the spot with the best heuristic

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

        // Bytecode here is 5800 (1700 used before this line)
        Logger.Log("A: " + Clock.getBytecodesLeft());

        // TODO: If you're blocking the way of a fellow miner, move forward so that he can help you mine
        // Movement strat: if you're right next to a miner and there's mineable blocks if you separate yourself, then separate yourself.

        // Calculate mineLocation
        comms.scanEnemyArchons(); // 500 bytecode

        Logger.Log("B: " + Clock.getBytecodesLeft());
        // If you're under attack (or you sense an enemy soldier), retreat!
        if(Util.countRobotTypes(nearby, RobotType.SOLDIER, myTeam.opponent()) > Util.countRobotTypes(nearby, RobotType.SOLDIER, myTeam)){ // 100 bytecode
            nav.goTo(archonLoc);
            return;
        }

        Logger.Log("C: " + Clock.getBytecodesLeft());
        MapLocation mineLocation = findClosestMine(); // 1700 bytecode for 56 leadlocs
        Logger.Log("D: " + Clock.getBytecodesLeft());

        // TODO: Better Scouting
        // If there's nowhere that I can sense to mine, find a new mineLocation
        if(mineLocation == null){
            // Go scout for a mine location
            if(target != null && myLoc.distanceSquaredTo(target) <= myType.actionRadiusSquared){
                target = null; // Reset target since you've reached there
            }
            if(target == null){
                target = nav.getRandomMapLocation(); // Find new target
            }
            nav.goTo(target); // Go towards target!
            Logger.Log("D1: " + Clock.getBytecodesLeft());
            return;
        }
        else if(myLoc.distanceSquaredTo(mineLocation) > 2){
            nav.goTo(mineLocation);
            Logger.Log("D2: " + Clock.getBytecodesLeft());
            return;
        }
        else if(rc.isMovementReady()){ // 300 byetecode (doesn't check if the location has led)
            // Spreading out code
            Logger.Log("D3: " + Clock.getBytecodesLeft());
            Direction bestMoveDir = moveAwayFromTeammates(); // 800 bytecode
            if(bestMoveDir != null){
//                    if(Util.tryMove(Util.getDirectionsGoingTowards(bestMoveDir))){
                if(nav.goTo(myLoc.add(bestMoveDir).add(bestMoveDir).add(bestMoveDir))){
                    return; // Moved away from adjacent teammates
                }
            }
            Logger.Log("D4: " + Clock.getBytecodesLeft());
        }

        // Try to mine on squares around us.
        if(mineLocation != null){
            // If you can go to a better spot and mine from there, do that
            double currCooldown = rc.senseRubble(myLoc) + 10.0;
            double bestCooldown = currCooldown;
            Direction bestDir = null;
            for(int i = 0; i < Navigation.directions.length; i++){
                MapLocation newLoc = myLoc.add(Navigation.directions[i]);
                if(!rc.canSenseLocation(newLoc)) {
                    continue;
                }
                double newCooldown = rc.senseRubble(newLoc) + 10.0;
                if(newCooldown >= bestCooldown){
                    continue;
                }
                if(!checkMineable(newLoc)){
                    continue;
                }
                bestCooldown = newCooldown;
                bestDir = Navigation.directions[i];
            }
            if(bestCooldown < currCooldown / 1.5){
                Util.tryMove(bestDir);
            }
            // Otherwise, just mine
            Logger.Log("Trying to mine at: " + mineLocation.toString());
            tryMine(mineLocation);
        }
        Logger.Log("E: " + Clock.getBytecodesLeft());

    }

    // Just move away from ppl in general, whether they're teammates or not
    public Direction moveAwayFromTeammates() throws GameActionException {
        // West: [-2, 0] : [-1, 1]
        // East: [0, 2] : [-1, 1]
        // North: [-1, 1] : [0, 2]
        // South: [-1, 1] : [-2, 0]
        // Northwest: [-2, 0] : [0, 2]
        // Northeast: [0, 2] : [0, ]
        // Southwest: [-2, 0] : [-2, 0]
        // Southeast: [0, 2] : [-2, 0]
//        boolean[][] valid = new boolean[5][5];
//        boolean[] validMoveDirs = new boolean[8];
//
//        for(int dx = -2; dx <= 2; dx++){
//            for(int dy = -2; dy <= 2; dy++){
//                MapLocation testLoc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
//                if(rc.canSenseLocation(testLoc)){
//                    boolean valid = false;
//                    if(rc.senseLead(testLoc) > 1){
//                        valid = true;
//                    }
//                    else if(rc.senseGold(testLoc) > 0){
//                        valid = true;
//                    }
//                    if(valid){
//                        if(dx >= -2 && dx <= 0)
//                    }
//                }
//            }
//        }

        int northAdj = 0;
        int southAdj = 0;
        int westAdj = 0;
        int eastAdj = 0;

        Logger.Log("Bytecode before dxdy: " + Clock.getBytecodesLeft());

        int myAdj = 0;
        if(friendlyRobotAtLocationDxDy(1, 0)){
            myAdj++;
        }
        if(friendlyRobotAtLocationDxDy(-1, 0)){
            myAdj++;
        }
        if(friendlyRobotAtLocationDxDy(0, 1)){
            myAdj++;
        }
        if(friendlyRobotAtLocationDxDy(0, -1)){
            myAdj++;
        }

        if(friendlyRobotAtLocationDxDy(-2, 0)){
            westAdj++;
        }
        if(friendlyRobotAtLocationDxDy(-1, 1)){
            westAdj++;
            northAdj++;
        }
        if(friendlyRobotAtLocationDxDy(0, 2)){
            northAdj++;
        }
        if(friendlyRobotAtLocationDxDy(1, 1)){
            northAdj++;
            eastAdj++;
        }
        if(friendlyRobotAtLocationDxDy(2, 0)){
            eastAdj++;
        }
        if(friendlyRobotAtLocationDxDy(1, -1)){
            eastAdj++;
            southAdj++;
        }
        if(friendlyRobotAtLocationDxDy(0, -2)){
            southAdj++;
        }
        if(friendlyRobotAtLocationDxDy(-1, -1)){
            southAdj++;
            westAdj++;
        }

        Logger.Log("Bytecode after dxdy: " + Clock.getBytecodesLeft());

        if(myAdj == 0){
            return null;
        }

        // Go in the best direction
        if(eastAdj < northAdj && eastAdj < southAdj && eastAdj < westAdj && eastAdj < myAdj){
            return Direction.EAST;
        }
        if(westAdj < northAdj && westAdj < southAdj && westAdj < eastAdj && westAdj < myAdj){
            return Direction.WEST;
        }
        if(southAdj < northAdj && southAdj < eastAdj && southAdj < westAdj && southAdj < myAdj){
            return Direction.SOUTH;
        }
        if(northAdj < eastAdj && northAdj < southAdj && northAdj < westAdj && northAdj < myAdj){
            return Direction.NORTH;
        }

        return null;
    }

    public boolean friendlyRobotAtLocationDxDy(int dx, int dy) throws GameActionException {
        MapLocation newLoc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
        if(!rc.canSenseLocation(newLoc)){
            return false;
        }
        RobotInfo info = rc.senseRobotAtLocation(newLoc);
        if(info == null){
            return false;
        }
        return info.team == myTeam;
    }


    // TODO: Better miner movement in general. They get stuck in corners and don't explore (copy over soldier code)

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
        Logger.Log("Checkmineable start: " + Clock.getBytecodesLeft());
        for (int i = 0; i < Navigation.allDirections.length; i++) { // Maximum mine radius is 2
            MapLocation newLoc = center.add(Navigation.allDirections[i]);
            if(!rc.canSenseLocation(newLoc)){
                continue;
            }
            int lead = rc.senseLead(newLoc);
            int gold = rc.senseGold(newLoc);
            if(lead > 1 || gold > 0){
                return true;
            }
        }
        Logger.Log("Checkmineable end: " + Clock.getBytecodesLeft());
        return false;
    }

    // True if you can mine from a given area, false if you can't
    public MapLocation getAdjacentMine(MapLocation center) throws GameActionException {
        for (int i = 0; i < Navigation.allDirections.length; i++) { // Maximum mine radius is 2
            MapLocation newLoc = center.add(Navigation.allDirections[i]);
            if(!rc.canSenseLocation(newLoc)){
                continue;
            }
            int lead = rc.senseLead(newLoc);
            int gold = rc.senseGold(newLoc);
            if(lead > 1 || gold > 0){
                return newLoc;
            }
        }
        return null;
    }


    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestMine() throws GameActionException {
        MapLocation bestLoc = null;
        int bestDist = 100000;
        // Go to nearest gold mine
        MapLocation[] goldLocs = rc.senseNearbyLocationsWithGold(myType.visionRadiusSquared);
        for(int i = 0; i < goldLocs.length; i++){
            int dist = myLoc.distanceSquaredTo(goldLocs[i]);
            if(dist < bestDist){
                bestDist = dist;
                bestLoc = goldLocs[i];
            }
        }
        if(bestLoc != null){
            return bestLoc;
        }

//        Logger.Log("Intermediary: " + Clock.getBytecodesLeft());

        MapLocation[] leadLocs = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared);
        Logger.Log("Num leadlocs found: " + leadLocs.length);
        for(int i = 0; i < leadLocs.length; i++){
            int dist = myLoc.distanceSquaredTo(leadLocs[i]);
            if(dist >= bestDist){
                continue;
            }
            if(rc.senseLead(leadLocs[i]) == 1){ // Save for farming
                continue;
            }
            bestDist = dist;
            bestLoc = leadLocs[i];
//            Logger.Log("LMAO: " + Clock.getBytecodesLeft()); // 25 bytecode per loop * 70 max loops = 2450 max bytecode
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
