package sprintbot;

import battlecode.common.*;
import scala.collection.Map;

// TODO: Revamp miner class. Just make a massive map of heuristics for each spot leadAvailable / (rubble + 10) + distance + (50 if adjacent friendly else 0)
// And then just go to the spot with the best heuristic

public class Miner extends Robot {

    MapLocation archonLoc = null;
    MapLocation mineLocation = null;
    Direction spawnDir = null;
    MapLocation currentTarget = null;
    int targetNum = 0;

    int[][] goldMap = new int[5][5];
    int[][] leadMap = new int[5][5];

    int leadVal00 = 0; int leadVal01 = 0; int leadVal02 = 0; int leadVal03 = 0; int leadVal04 = 0; int leadVal10 = 0; int leadVal11 = 0; int leadVal12 = 0; int leadVal13 = 0; int leadVal14 = 0; int leadVal20 = 0; int leadVal21 = 0; int leadVal22 = 0; int leadVal23 = 0; int leadVal24 = 0; int leadVal30 = 0; int leadVal31 = 0; int leadVal32 = 0; int leadVal33 = 0; int leadVal34 = 0; int leadVal40 = 0; int leadVal41 = 0; int leadVal42 = 0; int leadVal43 = 0; int leadVal44 = 0;
    int goldVal00 = 0; int goldVal01 = 0; int goldVal02 = 0; int goldVal03 = 0; int goldVal04 = 0; int goldVal10 = 0; int goldVal11 = 0; int goldVal12 = 0; int goldVal13 = 0; int goldVal14 = 0; int goldVal20 = 0; int goldVal21 = 0; int goldVal22 = 0; int goldVal23 = 0; int goldVal24 = 0; int goldVal30 = 0; int goldVal31 = 0; int goldVal32 = 0; int goldVal33 = 0; int goldVal34 = 0; int goldVal40 = 0; int goldVal41 = 0; int goldVal42 = 0; int goldVal43 = 0; int goldVal44 = 0;

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

        System.out.println("My location: " + rc.getLocation());

        // Bytecode here is 5800 (1700 used before this line)
        // Logger.Log("A: " + Clock.getBytecodesLeft());

        // TODO: If you're blocking the way of a fellow miner, move forward so that he can help you mine
        // Movement strat: if you're right next to a miner and there's mineable blocks if you separate yourself, then separate yourself.

        // Calculate mineLocation
        comms.scanEnemyArchons(); // Costs 500 bytecode

        // Logger.Log("Before filling: " + Clock.getBytecodesLeft());
        if(rc.getRoundNum() < 10){
            return;
        }
//        MapLocation[] courseOrder = {new MapLocation(0, 30), new MapLocation(30, 15), new MapLocation(0, 0), new MapLocation(30, 0)};
//        MapLocation[] courseOrder = {new MapLocation(1, 28), new MapLocation(28, 15), new MapLocation(7, 12), new MapLocation(10, 12), new MapLocation(18, 21), new MapLocation(28, 1)};
        MapLocation[] courseOrder = {new MapLocation(1, 58), new MapLocation(52, 29), new MapLocation(2, 2), new MapLocation(28, 52)};
        MapLocation target = courseOrder[targetNum];
        if(myLoc.distanceSquaredTo(target) <= 4){
            targetNum++;
            if(targetNum >= courseOrder.length){
                targetNum--;
            }
            target = courseOrder[targetNum];
        }
        if(rc.getID() > 11000){
            indicatorString += "BFSOLD " + target.toString();
//            nav.goTo(target);
            nav.goToBFSOld(target);
        }
        else{
            System.out.println("Starting bytecode: " + Clock.getBytecodesLeft());
            indicatorString += "BFSNEW " + target.toString();
            nav.goToBFS(target);
            System.out.println("Ending bytecode: " + Clock.getBytecodesLeft());
        }

//        boolean movedTowardsGold = goToClosestGold();
//        if(!movedTowardsGold){
//            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam.opponent());
//            fillMapsUnrolled(); // Costs 1125 bytecode. Bytecode here is 4491
//            double currHeuristic = getHeursitic(myLoc, nearbyEnemies); // Costs 800 bytecode
//            // Logger.Log("Single heuristic: " + Clock.getBytecodesLeft());
//            // Logger.Log("Heuristic value: " + currHeuristic);
//            double bestHeuristic = currHeuristic;
//            if(bestHeuristic > 0){ // Can currently mine from this location
//                Direction bestDir = null;
//                for(int i = 0; i < Util.directions.length; i++){ // If you can move to a better spot, do so
//                    Direction dir = Util.directions[i];
//                    if(!rc.canMove(dir)){
//                        continue;
//                    }
//                    MapLocation newLoc = myLoc.add(dir);
//                    double newHeuristic = getHeursitic(newLoc, nearbyEnemies);
//                    if(newHeuristic > bestHeuristic){
//                        bestHeuristic = newHeuristic;
//                        bestDir = dir;
//                    }
//                    // Logger.Log("Next heuristic: " + Clock.getBytecodesLeft());
//                }
//                if(bestDir != null){ // If you can move in a better direction, do so
//                    rc.move(bestDir);
//                    indicatorString += "CH: " + (int)currHeuristic + ",BH: " + (int)bestHeuristic + ", Dir:" + bestDir + "; ";
//                }
//            }
//            else{ // Find nearest mine
//                MapLocation targetLoc = findClosestMine();
//                // Logger.Log("Found closest mine: " + Clock.getBytecodesLeft());
//                if(targetLoc == null){
//                    if(currentTarget == null || myLoc.distanceSquaredTo(currentTarget) <= 8){
//                        currentTarget = nav.getRandomMapLocation();
//                    }
//                    targetLoc = currentTarget;
//                }
//                nav.goTo(targetLoc);
//                indicatorString += "NAV " + Util.mapLocationToInt(targetLoc) + "; ";
//            }
//        }
//        tryMineAllDirections();
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
    public double getHeursitic(MapLocation center, RobotInfo[] nearbyEnemies) throws GameActionException { // 1000 bytecode
        if(!rc.canSenseLocation(center)){
            return -10000;
        }
        double numTeammates = rc.senseNearbyRobots(center, 2, myTeam).length; // 100 bytecode
        double numEnemies = 0.0;
        for(int i = nearbyEnemies.length; i-- > 0; ){
            RobotInfo info = nearbyEnemies[i];
            if(info.type.damage > 0 && myLoc.distanceSquaredTo(info.location) <= info.type.actionRadiusSquared){
                numEnemies++;
            }
        }
        int dx = center.x - myLoc.x;
        int dy = center.y - myLoc.y;
        // Logger.Log("XD A " + Clock.getBytecodesLeft());
//        double leadMineable = numLeadMineable(dx, dy);
        double leadMineable = numLeadMineableUnrolled(dx, dy); // 50 bytecode baby.
        double goldMineable = numGoldMineableUnrolled(dx, dy); // ur so cul <3
        // Logger.Log("XD C " + Clock.getBytecodesLeft());
        double cooldown = 10.0 + rc.senseRubble(center);
        return (leadMineable + goldMineable*10 - numTeammates * 5 - numEnemies * 15) / cooldown; // Larger heuristic is better
    }

    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestMine() throws GameActionException {
        MapLocation bestLoc = null;
        int bestHeuristic = 100000;
        // Go to nearest gold mine
        MapLocation[] goldLocs = rc.senseNearbyLocationsWithGold(myType.visionRadiusSquared);
        for(int i = 0; i < goldLocs.length; i++){
            int dist = myLoc.distanceSquaredTo(goldLocs[i]);
            int cooldown = 10 + rc.senseRubble(goldLocs[i]);
            int heuristic = dist + cooldown;
            if(heuristic < bestHeuristic){
                bestHeuristic = dist;
                bestLoc = goldLocs[i];
            }
        }
        if(bestLoc != null){
            return bestLoc;
        }

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
                indicatorString += "G" + Util.mapLocationToInt(loc);
            }
        }

        for(int i = 0; i < Util.directions.length; i++){
            Direction dir = Util.directions[i];
            MapLocation loc = myLoc.add(dir);
            while (rc.canMineLead(loc) && rc.senseLead(loc) > 1) {
                rc.mineLead(loc);
                indicatorString += "L" + Util.mapLocationToInt(loc);
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
        MapLocation temp = new MapLocation(myLoc.x + -2, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal00 = 0;
            goldVal00 = 0;
        }
        else{ leadVal00 = rc.senseLead(temp); goldVal00 = rc.senseGold(temp);}
        if(leadVal00 == 1){ leadVal00 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -2, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal01 = 0;
            goldVal01 = 0;
        }
        else{ leadVal01 = rc.senseLead(temp); goldVal01 = rc.senseGold(temp);}
        if(leadVal01 == 1){ leadVal01 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -2, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal02 = 0;
            goldVal02 = 0;

        }
        else{ leadVal02 = rc.senseLead(temp); goldVal02 = rc.senseGold(temp);}
        if(leadVal02 == 1){ leadVal02 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -2, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal03 = 0;
            goldVal03 = 0;

        }
        else{ leadVal03 = rc.senseLead(temp); goldVal03 = rc.senseGold(temp); }
        if(leadVal03 == 1){ leadVal03 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -2, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal04 = 0;
            goldVal04 = 0;

        }
        else{ leadVal04 = rc.senseLead(temp); goldVal04 = rc.senseGold(temp);}
        if(leadVal04 == 1){ leadVal04 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal10 = 0;
            goldVal10 = 0;

        }
        else{ leadVal10 = rc.senseLead(temp); goldVal10 = rc.senseGold(temp);}
        if(leadVal10 == 1){ leadVal10 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal11 = 0;
            goldVal11 = 0;

        }
        else{ leadVal11 = rc.senseLead(temp); goldVal11 = rc.senseGold(temp);}
        if(leadVal11 == 1){ leadVal11 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal12 = 0;
            goldVal12 = 0;

        }
        else{ leadVal12 = rc.senseLead(temp); goldVal12 = rc.senseGold(temp);}
        if(leadVal12 == 1){ leadVal12 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal13 = 0;
            goldVal13 = 0;

        }
        else{ leadVal13 = rc.senseLead(temp); goldVal13 = rc.senseGold(temp);}
        if(leadVal13 == 1){ leadVal13 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + -1, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal14 = 0;
            goldVal14 = 0;

        }
        else{ leadVal14 = rc.senseLead(temp); goldVal14 = rc.senseGold(temp);}
        if(leadVal14 == 1){ leadVal14 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal20 = 0;
            goldVal20 = 0;

        }
        else{ leadVal20 = rc.senseLead(temp); goldVal20 = rc.senseGold(temp);}
        if(leadVal20 == 1){ leadVal20 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal21 = 0;
            goldVal21 = 0;

        }
        else{ leadVal21 = rc.senseLead(temp); goldVal21 = rc.senseGold(temp);}
        if(leadVal21 == 1){ leadVal21 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal22 = 0;
            goldVal22 = 0;

        }
        else{ leadVal22 = rc.senseLead(temp); goldVal22 = rc.senseGold(temp);}
        if(leadVal22 == 1){ leadVal22 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal23 = 0;
            goldVal23 = 0;

        }
        else{ leadVal23 = rc.senseLead(temp); goldVal23 = rc.senseGold(temp);}
        if(leadVal23 == 1){ leadVal23 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 0, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal24 = 0;
            goldVal24 = 0;

        }
        else{ leadVal24 = rc.senseLead(temp); goldVal24 = rc.senseGold(temp);}
        if(leadVal24 == 1){ leadVal24 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal30 = 0;
            goldVal30 = 0;

        }
        else{ leadVal30 = rc.senseLead(temp); goldVal30 = rc.senseGold(temp);}
        if(leadVal30 == 1){ leadVal30 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal31 = 0;
            goldVal31 = 0;

        }
        else{ leadVal31 = rc.senseLead(temp); goldVal31 = rc.senseGold(temp);}
        if(leadVal31 == 1){ leadVal31 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal32 = 0;
            goldVal32 = 0;

        }
        else{ leadVal32 = rc.senseLead(temp); goldVal32 = rc.senseGold(temp);}
        if(leadVal32 == 1){ leadVal32 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal33 = 0;
            goldVal33 = 0;

        }
        else{ leadVal33 = rc.senseLead(temp); goldVal33 = rc.senseGold(temp);}
        if(leadVal33 == 1){ leadVal33 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 1, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal34 = 0;
            goldVal34 = 0;

        }
        else{ leadVal34 = rc.senseLead(temp); goldVal34 = rc.senseGold(temp);}
        if(leadVal34 == 1){ leadVal34 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + -2);
        if(!rc.canSenseLocation(temp)){
            leadVal40 = 0;
            goldVal40 = 0;

        }
        else{ leadVal40 = rc.senseLead(temp); goldVal40 = rc.senseGold(temp);}
        if(leadVal40 == 1){ leadVal40 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + -1);
        if(!rc.canSenseLocation(temp)){
            leadVal41 = 0;
            goldVal41 = 0;

        }
        else{ leadVal41 = rc.senseLead(temp); goldVal41 = rc.senseGold(temp);}
        if(leadVal41 == 1){ leadVal41 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + 0);
        if(!rc.canSenseLocation(temp)){
            leadVal42 = 0;
            goldVal42 = 0;

        }
        else{ leadVal42 = rc.senseLead(temp); goldVal42 = rc.senseGold(temp);}
        if(leadVal42 == 1){ leadVal42 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + 1);
        if(!rc.canSenseLocation(temp)){
            leadVal43 = 0;
            goldVal43 = 0;
        }
        else{ leadVal43 = rc.senseLead(temp); goldVal43 = rc.senseGold(temp);}
        if(leadVal43 == 1){ leadVal43 = 0; } // Save for farming

        temp = new MapLocation(myLoc.x + 2, myLoc.y + 2);
        if(!rc.canSenseLocation(temp)){
            leadVal44 = 0;
            goldVal44 = 0;

        }
        else{ leadVal44 = rc.senseLead(temp); goldVal44 = rc.senseGold(temp);}
        if(leadVal44 == 1){ leadVal44 = 0; } // Save for farming
    }


    public int numGoldMineableUnrolled(int dx, int dy){
        if(dx == -1 && dy == -1){
            return goldVal00 + goldVal10 + goldVal20 + goldVal01 + goldVal11 + goldVal21 + goldVal02 + goldVal12  + goldVal22;
        }
        if(dx == -1 && dy == 0){
            return goldVal00 + goldVal11 + goldVal21 + goldVal02 + goldVal12 + goldVal22 + goldVal03 + goldVal13  + goldVal23;
        }
        if(dx == -1 && dy == 1){
            return goldVal00 + goldVal12 + goldVal22 + goldVal03 + goldVal13 + goldVal23 + goldVal04 + goldVal14  + goldVal24;
        }
        if(dx == 0 && dy == -1){
            return goldVal11 + goldVal20 + goldVal30 + goldVal11 + goldVal21 + goldVal31 + goldVal12 + goldVal22  + goldVal32;
        }
        if(dx == 0 && dy == 0){
            return goldVal11 + goldVal21 + goldVal31 + goldVal12 + goldVal22 + goldVal32 + goldVal13 + goldVal23  + goldVal33;
        }
        if(dx == 0 && dy == 1){
            return goldVal11 + goldVal22 + goldVal32 + goldVal13 + goldVal23 + goldVal33 + goldVal14 + goldVal24  + goldVal34;
        }
        if(dx == 1 && dy == -1){
            return goldVal22 + goldVal30 + goldVal40 + goldVal21 + goldVal31 + goldVal41 + goldVal22 + goldVal32  + goldVal42;
        }
        if(dx == 1 && dy == 0){
            return goldVal22 + goldVal31 + goldVal41 + goldVal22 + goldVal32 + goldVal42 + goldVal23 + goldVal33  + goldVal43;
        }
        if(dx == 1 && dy == 1){
            return goldVal22 + goldVal32 + goldVal42 + goldVal23 + goldVal33 + goldVal43 + goldVal24 + goldVal34  + goldVal44;
        }
        return 0;
    }

    public int numLeadMineableUnrolled(int dx, int dy){
        if(dx == -1 && dy == -1){
            return leadVal00 + leadVal10 + leadVal20 + leadVal01 + leadVal11 + leadVal21 + leadVal02 + leadVal12  + leadVal22;
        }
        if(dx == -1 && dy == 0){
            return leadVal00 + leadVal11 + leadVal21 + leadVal02 + leadVal12 + leadVal22 + leadVal03 + leadVal13  + leadVal23;
        }
        if(dx == -1 && dy == 1){
            return leadVal00 + leadVal12 + leadVal22 + leadVal03 + leadVal13 + leadVal23 + leadVal04 + leadVal14  + leadVal24;
        }
        if(dx == 0 && dy == -1){
            return leadVal11 + leadVal20 + leadVal30 + leadVal11 + leadVal21 + leadVal31 + leadVal12 + leadVal22  + leadVal32;
        }
        if(dx == 0 && dy == 0){
            return leadVal11 + leadVal21 + leadVal31 + leadVal12 + leadVal22 + leadVal32 + leadVal13 + leadVal23  + leadVal33;
        }
        if(dx == 0 && dy == 1){
            return leadVal11 + leadVal22 + leadVal32 + leadVal13 + leadVal23 + leadVal33 + leadVal14 + leadVal24  + leadVal34;
        }
        if(dx == 1 && dy == -1){
            return leadVal22 + leadVal30 + leadVal40 + leadVal21 + leadVal31 + leadVal41 + leadVal22 + leadVal32  + leadVal42;
        }
        if(dx == 1 && dy == 0){
            return leadVal22 + leadVal31 + leadVal41 + leadVal22 + leadVal32 + leadVal42 + leadVal23 + leadVal33  + leadVal43;
        }
        if(dx == 1 && dy == 1){
            return leadVal22 + leadVal32 + leadVal42 + leadVal23 + leadVal33 + leadVal43 + leadVal24 + leadVal34  + leadVal44;
        }
        return 0;
    }


}
