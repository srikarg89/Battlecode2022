package sprintbot4;

import battlecode.common.*;

// And then just go to the spot with the best heuristic

public class Miner extends Robot {

    MapLocation archonLoc = null;
    MapLocation mineLocation = null;
    Direction spawnDir = null;
    MapLocation currentTarget = null;
    int minerSpawnNumber = 0;

    int[][] leadMap = new int[5][5];

    int leadVal00 = 0; int leadVal01 = 0; int leadVal02 = 0; int leadVal03 = 0; int leadVal04 = 0; int leadVal10 = 0; int leadVal11 = 0; int leadVal12 = 0; int leadVal13 = 0; int leadVal14 = 0; int leadVal20 = 0; int leadVal21 = 0; int leadVal22 = 0; int leadVal23 = 0; int leadVal24 = 0; int leadVal30 = 0; int leadVal31 = 0; int leadVal32 = 0; int leadVal33 = 0; int leadVal34 = 0; int leadVal40 = 0; int leadVal41 = 0; int leadVal42 = 0; int leadVal43 = 0; int leadVal44 = 0;

    public Miner(RobotController rc) throws GameActionException {
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

        // System.out.println("Bytecode A: " + Clock.getBytecodesLeft()); // 6500 bytecode

        // Calculate mineLocation
        comms.scanEnemyArchons(); // Costs 500 bytecode

        indicatorString += "C " + rc.getMovementCooldownTurns() + ", " + rc.getActionCooldownTurns() + "; ";
        boolean movedTowardsGold = goToClosestGold();
        // System.out.println("Bytecode B: " + Clock.getBytecodesLeft()); // 5800 bytecode
        if(!movedTowardsGold) {
            // System.out.println("Bytecode C: " + Clock.getBytecodesLeft()); // 5700
            fillMapsUnrolled2(); // Costs 350 bytecode
            // System.out.println("Bytecode D: " + Clock.getBytecodesLeft()); // 4600
            double currHeuristic = getHeursitic(myLoc, nearbyEnemies); // Costs 500 bytecode
            double bestHeuristic = currHeuristic;
            if(currHeuristic != 0){
                // System.out.println("Bytecode E: " + Clock.getBytecodesLeft()); // 4100
                Direction bestDir = null;
                for (int i = 0; i < Util.directions.length; i++) { // If you can move to a better spot, do so
                    Direction dir = Util.directions[i];
                    if (!rc.canMove(dir)) {
                        continue;
                    }
                    MapLocation newLoc = myLoc.add(dir);
                    double newHeuristic = getHeursitic(newLoc, nearbyEnemies);
                    // System.out.println("Bytecode F: " + Clock.getBytecodesLeft()); // 550 per loop
                    if (newHeuristic > bestHeuristic) {
                        bestHeuristic = newHeuristic;
                        bestDir = dir;
                    }
                }
                if (bestDir != null) { // If you can move in a better direction, do so
                    rc.move(bestDir);
                    indicatorString += "CH: " + (int) (currHeuristic * 100) + ",BH: " + (int) (bestHeuristic * 100) + ", Dir: " + bestDir + "; ";
                }
                else{
                    indicatorString += "STAY; ";
                }
            }
            else { // Find nearest mine
                // System.out.println("Bytecode G: " + Clock.getBytecodesLeft());
                MapLocation targetLoc = findClosestLeadMine();
                // System.out.println("Bytecode H: " + Clock.getBytecodesLeft());
                if(targetLoc == null){
                    if(currentTarget == null){
                        currentTarget = nav.getRandomMapLocation();
                    }
                    else if(myLoc.distanceSquaredTo(currentTarget) <= 4){
                        currentTarget = nav.getRandomMapLocation();
                    }
                    targetLoc = currentTarget;
                }
                nav.goTo(targetLoc);
                // System.out.println("Bytecode I: " + Clock.getBytecodesLeft());
                indicatorString += "NAV " + Util.mapLocationToInt(targetLoc) + "; ";
            }
        }
        // System.out.println("J: " + Clock.getBytecodesLeft());
        tryMineAllDirections(); // 700 bytecode
        // System.out.println("K: " + Clock.getBytecodesLeft());
        checkPossibleDeath();
        // System.out.println("L: " + Clock.getBytecodesLeft());
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

    // TODO: Move away from enemies
    // 300 bytecode method
    public double getHeursitic(MapLocation center, RobotInfo[] nearbyEnemies) throws GameActionException { // 1000 bytecode
        if(!rc.canSenseLocation(center)){
            return -10000;
        }
//        double numEnemies = 0.0;
        double leadMineable = numLeadMineableUnrolled(center.x - myLoc.x, center.y - myLoc.y); // 50 bytecode baby.
        double numTeammates = 0;

        if(leadMineable > 0){
            // Only move away from adjacent teammates if ur busy mining lead
            numTeammates = rc.senseNearbyRobots(center, 1, myTeam).length; // 100 bytecode
        }

        double cooldown = 10.0 + rc.senseRubble(center);
        // TODO maybe instead of couting enemies in attack radius, count how far away you are from said enemy? And then you could do like 1 / distance so that larger distance = lower heuristic
//        return (leadMineable - numTeammates * 5 - numEnemies * 40) / cooldown; // Larger heuristic is better
        return (leadMineable - numTeammates * 5) / cooldown; // Larger heuristic is better
    }

    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestLeadMine() throws GameActionException {
        MapLocation bestLoc = null;
        int bestHeuristic = 100000;
        int dist; int cooldown; int heuristic;
        // Go to nearest lead mine
        MapLocation[] leadLocs = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared, 2);
        for(int i = leadLocs.length; i -- > 0; ){
            dist = myLoc.distanceSquaredTo(leadLocs[i]);
            cooldown = 10 + rc.senseRubble(leadLocs[i]);
            heuristic = dist * cooldown;
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
            while (rc.canMineGold(loc)) {
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
        // System.out.println("YEET: " + Clock.getBytecodesLeft());
        int x = myLoc.x;
        int y = myLoc.y;
        MapLocation leadLoc;
        for(int i = nearbyLead.length; i-- > 0; ){
            leadLoc = nearbyLead[i];
            switch(leadLoc.x - x){
                case -2:
                    switch(leadLoc.y - y){
                        case -2:
                            leadVal00 = rc.senseLead(leadLoc);
                            break;
                        case -1:
                            leadVal01 = rc.senseLead(leadLoc);
                            break;
                        case 0:
                            leadVal02 = rc.senseLead(leadLoc);
                            break;
                        case 1:
                            leadVal03 = rc.senseLead(leadLoc);
                            break;
                        case 2:
                            leadVal04 = rc.senseLead(leadLoc);
                            break;
                    }
                    break;
                case -1:
                    switch(leadLoc.y - y){
                        case -2:
                            leadVal10 = rc.senseLead(leadLoc);
                            break;
                        case -1:
                            leadVal11 = rc.senseLead(leadLoc);
                            break;
                        case 0:
                            leadVal12 = rc.senseLead(leadLoc);
                            break;
                        case 1:
                            leadVal13 = rc.senseLead(leadLoc);
                            break;
                        case 2:
                            leadVal14 = rc.senseLead(leadLoc);
                            break;
                    }
                    break;
                case 0:
                    switch(leadLoc.y - y){
                        case -2:
                            leadVal20 = rc.senseLead(leadLoc);
                            break;
                        case -1:
                            leadVal21 = rc.senseLead(leadLoc);
                            break;
                        case 0:
                            leadVal22 = rc.senseLead(leadLoc);
                            break;
                        case 1:
                            leadVal23 = rc.senseLead(leadLoc);
                            break;
                        case 2:
                            leadVal24 = rc.senseLead(leadLoc);
                            break;
                    }
                    break;
                case 1:
                    switch(leadLoc.y - y){
                        case -2:
                            leadVal30 = rc.senseLead(leadLoc);
                            break;
                        case -1:
                            leadVal31 = rc.senseLead(leadLoc);
                            break;
                        case 0:
                            leadVal32 = rc.senseLead(leadLoc);
                            break;
                        case 1:
                            leadVal33 = rc.senseLead(leadLoc);
                            break;
                        case 2:
                            leadVal34 = rc.senseLead(leadLoc);
                            break;
                    }
                    break;
                case 2:
                    switch(leadLoc.y - y){
                        case -2:
                            leadVal40 = rc.senseLead(leadLoc);
                            break;
                        case -1:
                            leadVal41 = rc.senseLead(leadLoc);
                            break;
                        case 0:
                            leadVal42 = rc.senseLead(leadLoc);
                            break;
                        case 1:
                            leadVal43 = rc.senseLead(leadLoc);
                            break;
                        case 2:
                            leadVal44 = rc.senseLead(leadLoc);
                            break;
                    }
                    break;
            }
        }
    }

    public int numLeadMineableUnrolled(int dx, int dy){
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
