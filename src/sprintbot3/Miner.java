package sprintbot3;

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

        // Bytecode here is 5800 (1700 used before this line)

        // Calculate mineLocation
        comms.scanEnemyArchons(); // Costs 500 bytecode

        indicatorString += "C " + rc.getMovementCooldownTurns() + ", " + rc.getActionCooldownTurns() + "; ";
        boolean movedTowardsGold = goToClosestGold();
        if(!movedTowardsGold){
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, opponent); // 120 bytecode
            fillMapsUnrolled2(); // Costs 350 bytecode
            double currHeuristic = getHeursitic(myLoc, nearbyEnemies); // Costs 1200 bytecode
            double bestHeuristic = currHeuristic;
            Direction bestDir = null;
            for(int i = 0; i < Util.directions.length; i++){ // If you can move to a better spot, do so
                Direction dir = Util.directions[i];
                if(!rc.canMove(dir)){
                    continue;
                }
                MapLocation newLoc = myLoc.add(dir);
                double newHeuristic = getHeursitic(newLoc, nearbyEnemies);
                if(newHeuristic > bestHeuristic){
                    bestHeuristic = newHeuristic;
                    bestDir = dir;
                }
            }
            if(bestDir != null){ // If you can move in a better direction, do so
                rc.move(bestDir);
                indicatorString += "CH: " + (int)(currHeuristic * 100) + ",BH: " + (int)(bestHeuristic * 100) + ", Dir: " + bestDir + "; ";
            }
            else if(currHeuristic == 0){ // Find nearest mine
                MapLocation targetLoc = findClosestLeadMine();
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
                indicatorString += "NAV " + Util.mapLocationToInt(targetLoc) + "; ";
            }
            else{
                indicatorString += "STAY; ";
            }
        }
        tryMineAllDirections();
        checkPossibleDeath();
        // Logger.Log("After mining: " + Clock.getBytecodesLeft());
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

    // Check if you can mine lead from a given location
    public int numLeadMineable(int dx, int dy) throws GameActionException {
        return leadMap[dx + 1][dy + 1]
                + leadMap[dx + 2][dy + 1]
                + leadMap[dx + 3][dy + 1]
                + leadMap[dx + 1][dy + 2]
                + leadMap[dx + 2][dy + 2]
                + leadMap[dx + 3][dy + 2]
                + leadMap[dx + 1][dy + 3]
                + leadMap[dx + 2][dy + 3]
                + leadMap[dx + 3][dy + 3];
    }

    // TODO: Move away from enemies
    // Higher heuristic value is better
    // 300 bytecode method
    public double getHeursitic(MapLocation center, RobotInfo[] nearbyEnemies) throws GameActionException { // 1000 bytecode
        if(!rc.canSenseLocation(center)){
            return -10000;
        }
        double numEnemies = 0.0;
        for(int i = nearbyEnemies.length; i-- > 0; ){
            RobotInfo info = nearbyEnemies[i];
            if(info.type.damage > 0 && center.distanceSquaredTo(info.location) <= info.type.actionRadiusSquared){
                numEnemies++;
            }
        }
        int dx = center.x - myLoc.x;
        int dy = center.y - myLoc.y;
        double leadMineable = numLeadMineableUnrolled(dx, dy); // 50 bytecode baby.
        double numTeammates = 0;
        if(leadMineable > 0){
            // Only move away from adjacent teammates if ur busy mining lead
            numTeammates = rc.senseNearbyRobots(center, 1, myTeam).length; // 100 bytecode
        }
        double cooldown = 10.0 + rc.senseRubble(center);
        // TODO maybe instead of couting enemies in attack radius, count how far away you are from said enemy? And then you could do like 1 / distance so that larger distance = lower heuristic
        return (leadMineable - numTeammates * 5 - numEnemies * 40) / cooldown; // Larger heuristic is better
    }

    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestLeadMine() throws GameActionException {
        MapLocation bestLoc = null;
        int bestHeuristic = 100000;
        // Go to nearest lead mine
        MapLocation[] leadLocs = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared);
        // Logger.Log("Num leadlocs found: " + leadLocs.length);
        for(int i = 0; i < leadLocs.length; i++){
            int dist = myLoc.distanceSquaredTo(leadLocs[i]);
            int cooldown = 10 + rc.senseRubble(leadLocs[i]);
            int heuristic = dist + cooldown;
            if(heuristic >= bestHeuristic){
                continue;
            }
            if(rc.senseLead(leadLocs[i]) == 1){ // Save for farming
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

    public void fillMaps() throws GameActionException {

        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                MapLocation temp = new MapLocation(myLoc.x + (i - 2), myLoc.y + (j - 2));
                if(!rc.canSenseLocation(temp)){
//                    goldMap[i][j] = 0;
                    leadMap[i][j] = 0;
                }
//                goldMap[i][j] = rc.senseGold(temp);
                int leadVal = rc.senseLead(temp);
                if(leadVal > 1){ // Save for farming
                    leadMap[i][j] = leadVal;
                }
                else{
                    leadMap[i][j] = 0;
                }
            }
        }
    }

    public void fillMapsUnrolled() throws GameActionException {
        // Syso is 7 bytecode
        // 36 bytecode per group * 25 groups = 900 bytecode total
        MapLocation temp = new MapLocation(myLoc.x + -2, myLoc.y + -2); // 13 bytecode
        if(!rc.canSenseLocation(temp)){ // 9 bytecode (5 for method, 4 for if statement)
            leadVal00 = 0;
        }
        else{
            leadVal00 = rc.senseLead(temp); // 10 bytecode
        }
        if(leadVal00 == 1){ // 4 bytecode
            leadVal00 = 0;
        }

        temp = new MapLocation(myLoc.x + -2, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal01 = 0;
        }
        else{ leadVal01 = rc.senseLead(temp); }
        if(leadVal01 == 1){ leadVal01 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -2, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal02 = 0;
        }
        else{ leadVal02 = rc.senseLead(temp); }
        if(leadVal02 == 1){ leadVal02 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -2, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal03 = 0;
        }
        else{ leadVal03 = rc.senseLead(temp); }
        if(leadVal03 == 1){ leadVal03 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -2, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal04 = 0;
        }
        else{ leadVal04 = rc.senseLead(temp); }
        if(leadVal04 == 1){ leadVal04 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal10 = 0;
        }
        else{ leadVal10 = rc.senseLead(temp); }
        if(leadVal10 == 1){ leadVal10 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal11 = 0;
        }
        else{ leadVal11 = rc.senseLead(temp); }
        if(leadVal11 == 1){ leadVal11 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal12 = 0;
        }
        else{ leadVal12 = rc.senseLead(temp); }
        if(leadVal12 == 1){ leadVal12 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal13 = 0;
        }
        else{ leadVal13 = rc.senseLead(temp); }
        if(leadVal13 == 1){ leadVal13 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal14 = 0;
        }
        else{ leadVal14 = rc.senseLead(temp); }
        if(leadVal14 == 1){ leadVal14 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal20 = 0;
        }
        else{ leadVal20 = rc.senseLead(temp); }
        if(leadVal20 == 1){ leadVal20 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal21 = 0;
        }
        else{ leadVal21 = rc.senseLead(temp); }
        if(leadVal21 == 1){ leadVal21 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal22 = 0;
        }
        else{ leadVal22 = rc.senseLead(temp); }
        if(leadVal22 == 1){ leadVal22 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal23 = 0;
        }
        else{ leadVal23 = rc.senseLead(temp); }
        if(leadVal23 == 1){ leadVal23 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal24 = 0;
        }
        else{ leadVal24 = rc.senseLead(temp); }
        if(leadVal24 == 1){ leadVal24 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal30 = 0;
        }
        else{ leadVal30 = rc.senseLead(temp); }
        if(leadVal30 == 1){ leadVal30 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal31 = 0;
        }
        else{ leadVal31 = rc.senseLead(temp); }
        if(leadVal31 == 1){ leadVal31 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal32 = 0;
        }
        else{ leadVal32 = rc.senseLead(temp); }
        if(leadVal32 == 1){ leadVal32 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal33 = 0;
        }
        else{ leadVal33 = rc.senseLead(temp); }
        if(leadVal33 == 1){ leadVal33 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal34 = 0;
        }
        else{ leadVal34 = rc.senseLead(temp); }
        if(leadVal34 == 1){ leadVal34 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal40 = 0;
        }
        else{ leadVal40 = rc.senseLead(temp); }
        if(leadVal40 == 1){ leadVal40 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal41 = 0;
        }
        else{ leadVal41 = rc.senseLead(temp); }
        if(leadVal41 == 1){ leadVal41 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal42 = 0;
        }
        else{ leadVal42 = rc.senseLead(temp); }
        if(leadVal42 == 1){ leadVal42 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal43 = 0;
        }
        else{ leadVal43 = rc.senseLead(temp); }
        if(leadVal43 == 1){ leadVal43 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal44 = 0;
        }
        else{ leadVal44 = rc.senseLead(temp); }
        if(leadVal44 == 1){ leadVal44 = 0; } // Save for farming
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
