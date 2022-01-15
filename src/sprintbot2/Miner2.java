package sprintbot2;

import battlecode.common.*;

// And then just go to the spot with the best heuristic

class MinerHeuristic {

    double leadMineable;
    double numTeammates;
    double numEnemies;
    double cooldown;

    public MinerHeuristic(double leadMineable, double numTeammates, double numEnemies, double cooldown){
        this.leadMineable = leadMineable;
        this.numTeammates = numTeammates;
        this.numEnemies = numEnemies;
        this.cooldown = cooldown;
    }

    public double calcHeuristic(){
//       if(leadMineable == 0) { // Not mining any lead
//            return 0.0;
//        }
//        if(leadMineable >= 5){
//            leadMineable = 5;
//        }
        return (leadMineable - numTeammates * 5 - numEnemies * 15) / cooldown;
//        return (leadMineable - numTeammates * 2.0) / cooldown;
    }

}

public class Miner2 extends Robot {

    MapLocation archonLoc = null;
    MapLocation mineLocation = null;
    Direction spawnDir = null;
    MapLocation currentTarget = null;

    int leadVal00 = 0; int leadVal01 = 0; int leadVal02 = 0; int leadVal03 = 0; int leadVal04 = 0; int leadVal10 = 0; int leadVal11 = 0; int leadVal12 = 0; int leadVal13 = 0; int leadVal14 = 0; int leadVal20 = 0; int leadVal21 = 0; int leadVal22 = 0; int leadVal23 = 0; int leadVal24 = 0; int leadVal30 = 0; int leadVal31 = 0; int leadVal32 = 0; int leadVal33 = 0; int leadVal34 = 0; int leadVal40 = 0; int leadVal41 = 0; int leadVal42 = 0; int leadVal43 = 0; int leadVal44 = 0;

    public Miner2(RobotController rc) throws GameActionException {
        super(rc);
        // Logger.Log("Running constructor!");
    }

    public void run() throws GameActionException {
        super.run();
//        // Logger.Log("Round number: " + rc.getRoundNum());
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

        // Calculate mineLocation
        comms.scanEnemyArchons(); // Costs 500 bytecode

        indicatorString += "C " + rc.getMovementCooldownTurns() + ", " + rc.getActionCooldownTurns() + "; ";
        boolean movedTowardsGold = goToClosestGold();
        if(!movedTowardsGold){
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());
            // If there's a nearby threat, run away (NOTE, TRIED IMPLEMENTING THIS AND IT MADE IT SO MUCH WORSE FOR SOME RZN T_T)
            fillMapsUnrolled2();
            MinerHeuristic currHeuristicObj = getHeuristic(myLoc, nearbyEnemies);
            double currHeuristic = currHeuristicObj.calcHeuristic(); // Costs 800 bytecode
            double bestHeuristic = currHeuristic;
            if(bestHeuristic > 0){ // Can currently mine from this location
                Direction bestDir = null;
                for(int i = 0; i < Util.directions.length; i++){ // If you can move to a better spot, do so
                    Direction dir = Util.directions[i];
                    if(!rc.canMove(dir)){
                        continue;
                    }
                    MapLocation newLoc = myLoc.add(dir);
                    MinerHeuristic newHeuristicObj = getHeuristic(newLoc, nearbyEnemies);
                    double newHeuristic = newHeuristicObj.calcHeuristic(); // Costs 800 bytecode
                    if(newHeuristic > bestHeuristic){
                        bestHeuristic = newHeuristic;
                        bestDir = dir;
                    }
//                        System.out.println("Next heuristic: " + Clock.getBytecodesLeft());
                }
                if(bestDir != null){ // If you can move in a better direction, do so
                    rc.move(bestDir);
                    indicatorString += "CH: " + (int)currHeuristic + ",BH: " + (int)bestHeuristic + ", Dir:" + bestDir + "; ";
                }
            }
            else{ // Find nearest mine
//                RobotInfo nearestThreat = getNearestThreat(nearbyEnemies);
                MapLocation targetLoc = findClosestLeadMine();
//                if(nearestThreat != null){
//                    targetLoc = Util.multiplyDirection(myLoc, myLoc.directionTo(nearestThreat.location).opposite(), 7);
//                }
                if(targetLoc == null){
                    if(currentTarget == null || myLoc.distanceSquaredTo(currentTarget) <= 8){
                        currentTarget = nav.getRandomMapLocation();
                    }
                    targetLoc = currentTarget;
                }
//                    System.out.println("Target loc bytecode: " + Clock.getBytecodesLeft());
                nav.goTo(targetLoc);
                indicatorString += "NAV " + Util.mapLocationToInt(targetLoc) + "; ";
            }
        }
        tryMineAllDirections();
        checkPossibleDeath();
        // Logger.Log("After mining: " + Clock.getBytecodesLeft());
    }

    public RobotInfo getNearestThreat(RobotInfo[] nearbyEnemies) throws GameActionException {
        RobotInfo nearest = null;
        int closestDist = Integer.MAX_VALUE;
        for(int i = nearbyEnemies.length; i-- > 0; ){
            RobotInfo info = nearbyEnemies[i];
            if(info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER){
                if(info.type == RobotType.WATCHTOWER && info.mode == RobotMode.PORTABLE){
                    continue;
                }
//                System.out.println("ENEMY REEEEE: " + info.getID());
                int dist = info.location.distanceSquaredTo(myLoc);
                if(dist > info.type.actionRadiusSquared){
                    continue;
                }
                if(dist < closestDist){
                    closestDist = dist;
                    nearest = info;
                }
            }
        }
        return nearest;
    }

    public boolean checkSafe(RobotInfo[] nearbyFriendlies, RobotInfo[] nearbyEnemies) throws GameActionException {
        double enemyDist = 0.0;
        for(int i = nearbyEnemies.length; i-- > 0; ){
            RobotInfo info = nearbyEnemies[i];
            if(info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER){
                if(info.type == RobotType.WATCHTOWER && info.mode == RobotMode.PORTABLE){
                    continue;
                }
                int dist = info.location.distanceSquaredTo(myLoc);
                if(dist > info.type.actionRadiusSquared){
                    continue;
                }
                enemyDist += 1.0 / dist;
            }
        }

        double friendlyDist = 0.0;
        for(int i = nearbyFriendlies.length; i-- > 0; ){
            RobotInfo info = nearbyFriendlies[i];
            if(info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER){
                if(info.type == RobotType.WATCHTOWER && info.mode == RobotMode.PORTABLE){
                    continue;
                }
                int dist = info.location.distanceSquaredTo(myLoc);
                if(dist > info.type.actionRadiusSquared){
                    continue;
                }
                friendlyDist += 1.0 / dist;
            }
        }
        return friendlyDist >= enemyDist;
    }

    public boolean goToClosestGold() throws GameActionException {
        MapLocation[] goldLocs = rc.senseNearbyLocationsWithGold();
        if(goldLocs.length == 0){
            return false;
        }
        int closestDist = 100000;
        MapLocation closest = null;
        for(int i = 0; i < goldLocs.length; i++){
            int dist = myLoc.distanceSquaredTo(goldLocs[i]);
            if(dist < closestDist){
                closestDist = dist;
                closest = goldLocs[i];
            }
        }
        if(closestDist <= 2){ // Can alr mine the gold
            return true;
        }
        nav.goTo(closest);
        return true;
    }

    public MinerHeuristic getHeuristic(MapLocation center, RobotInfo[] nearbyEnemies) throws GameActionException { // 1000 bytecode
        if(!rc.canSenseLocation(center)){
            return null;
        }
        double numTeammates = rc.senseNearbyRobots(center, 2, myTeam).length; // 100 bytecode
        double numEnemies = 0.0;
        for(int i = nearbyEnemies.length; i-- > 0; ){
            RobotInfo info = nearbyEnemies[i];
            if(info.type.damage > 0 && myLoc.distanceSquaredTo(info.location) <= info.type.actionRadiusSquared){
                numEnemies++;
//                numEnemies += 10 / myLoc.distanceSquaredTo(info.location);
            }
        }
        int dx = center.x - myLoc.x;
        int dy = center.y - myLoc.y;
        double leadMineable = numLeadMineableUnrolled(dx, dy); // 50 bytecode baby.
        double cooldown = 10.0 + rc.senseRubble(center);
        return new MinerHeuristic(leadMineable, numTeammates, numEnemies, cooldown);
    }

    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestLeadMine() throws GameActionException {
        MapLocation bestLoc = null;
        int bestHeuristic = 100000;
        // Go to nearest lead mine
        MapLocation[] leadLocs = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared, 2);
        // Logger.Log("Num leadlocs found: " + leadLocs.length);
        for(int i = 0; i < leadLocs.length; i++){
            int dist = myLoc.distanceSquaredTo(leadLocs[i]);
            int cooldown = 10 + rc.senseRubble(leadLocs[i]);
            int heuristic = dist + cooldown;
            if(heuristic >= bestHeuristic){
                continue;
            }
            bestHeuristic = heuristic;
            bestLoc = leadLocs[i];
        }

        return bestLoc;
    }

    public void tryMineAllDirections() throws GameActionException {
        for(int i = 0; i < Util.directions.length; i++){
            Direction dir = Util.directions[i];
            MapLocation loc = myLoc.add(dir);
            while (rc.canMineGold(loc) && rc.senseGold(loc) > 0) {
                rc.mineGold(loc);
                indicatorString += "G;";
            }
        }

        for(int i = 0; i < Util.directions.length; i++){
            Direction dir = Util.directions[i];
            MapLocation loc = myLoc.add(dir);
            while (rc.canMineLead(loc) && rc.senseLead(loc) > 1) {
                rc.mineLead(loc);
                indicatorString += "L;";
            }
        }
    }

    public void fillMapsUnrolled2() throws GameActionException {
        leadVal00 = 0; leadVal01 = 0; leadVal02 = 0; leadVal03 = 0; leadVal04 = 0; leadVal10 = 0; leadVal11 = 0; leadVal12 = 0; leadVal13 = 0; leadVal14 = 0; leadVal20 = 0; leadVal21 = 0; leadVal22 = 0; leadVal23 = 0; leadVal24 = 0; leadVal30 = 0; leadVal31 = 0; leadVal32 = 0; leadVal33 = 0; leadVal34 = 0; leadVal40 = 0; leadVal41 = 0; leadVal42 = 0; leadVal43 = 0; leadVal44 = 0;
        MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(8, 2);
        int x = myLoc.x;
        int y = myLoc.y;
        for(int i = nearbyLead.length; i-- > 0; ){
            int dx = nearbyLead[i].x - x;
            int dy = nearbyLead[i].y - y;
            switch(dx){
                case -2:
                    switch(dy){
                        case -2:
                            leadVal00 = rc.senseLead(nearbyLead[i]);
                            break;
                        case -1:
                            leadVal01 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 0:
                            leadVal02 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 1:
                            leadVal03 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 2:
                            leadVal04 = rc.senseLead(nearbyLead[i]);
                            break;
                    }
                    break;
                case -1:
                    switch(dy){
                        case -2:
                            leadVal10 = rc.senseLead(nearbyLead[i]);
                            break;
                        case -1:
                            leadVal11 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 0:
                            leadVal12 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 1:
                            leadVal13 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 2:
                            leadVal14 = rc.senseLead(nearbyLead[i]);
                            break;
                    }
                    break;
                case 0:
                    switch(dy){
                        case -2:
                            leadVal20 = rc.senseLead(nearbyLead[i]);
                            break;
                        case -1:
                            leadVal21 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 0:
                            leadVal22 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 1:
                            leadVal23 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 2:
                            leadVal24 = rc.senseLead(nearbyLead[i]);
                            break;
                    }
                    break;
                case 1:
                    switch(dy){
                        case -2:
                            leadVal30 = rc.senseLead(nearbyLead[i]);
                            break;
                        case -1:
                            leadVal31 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 0:
                            leadVal32 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 1:
                            leadVal33 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 2:
                            leadVal34 = rc.senseLead(nearbyLead[i]);
                            break;
                    }
                    break;
                case 2:
                    switch(dy){
                        case -2:
                            leadVal40 = rc.senseLead(nearbyLead[i]);
                            break;
                        case -1:
                            leadVal41 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 0:
                            leadVal42 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 1:
                            leadVal43 = rc.senseLead(nearbyLead[i]);
                            break;
                        case 2:
                            leadVal44 = rc.senseLead(nearbyLead[i]);
                            break;
                    }
                    break;
            }
        }
    }

    public int numLeadMineableUnrolled(int dx, int dy){
        // TODO Use switch statements instead
        switch(dx){
            case -1:
                switch(dy){
                    case -1:
                        return leadVal00 + leadVal01 + leadVal02 + leadVal10 + leadVal11 + leadVal12 + leadVal20 + leadVal21 + leadVal22;
                    case 0:
                        return leadVal01 + leadVal02 + leadVal03 + leadVal11 + leadVal12 + leadVal13 + leadVal21 + leadVal22 + leadVal23;
                    case 1:
                        return leadVal02 + leadVal03 + leadVal04 + leadVal12 + leadVal13 + leadVal14 + leadVal22 + leadVal23 + leadVal24;
                }
                break;
            case 0:
                switch(dy){
                    case -1:
                        return leadVal10 + leadVal11 + leadVal12 + leadVal20 + leadVal21 + leadVal22 + leadVal30 + leadVal31 + leadVal32;
                    case 0:
                        return leadVal11 + leadVal12 + leadVal13 + leadVal21 + leadVal22 + leadVal23 + leadVal31 + leadVal32 + leadVal33;
                    case 1:
                        return leadVal12 + leadVal13 + leadVal14 + leadVal22 + leadVal23 + leadVal24 + leadVal32 + leadVal33 + leadVal34;
                }
                break;
            case 1:
                switch(dy){
                    case -1:
                        return leadVal20 + leadVal21 + leadVal22 + leadVal30 + leadVal31 + leadVal32 + leadVal40 + leadVal41 + leadVal42;
                    case 0:
                        return leadVal21 + leadVal22 + leadVal23 + leadVal31 + leadVal32 + leadVal33 + leadVal41 + leadVal42 + leadVal43;
                    case 1:
                        return leadVal22 + leadVal23 + leadVal24 + leadVal32 + leadVal33 + leadVal34 + leadVal42 + leadVal43 + leadVal44;
                }
                break;
        }
        assert(false);
        return 0;
    }


}
