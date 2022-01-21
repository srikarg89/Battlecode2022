package hopefullyiwin;

import battlecode.common.*;

import java.util.Map;

// And then just go to the spot with the best heuristic

class HeuristicObject {
    double lead;
    double numTeammates;
    double numEnemies;
    public HeuristicObject(double lead, double numTeammates, double numEnemies){
        this.lead = lead;
        this.numTeammates = numTeammates;
        this.numEnemies = numEnemies;
    }
}

public class Miner extends Robot {

    MapLocation archonLoc = null;
    int archonIndex = -1;
    MapLocation mineLocation = null;
    Direction spawnDir = null;
    MapLocation currentTarget = null;
    final int ignore_farmer_threshold = 11;
    MapLocation farmerLoc; // If this is null ur not a farmer get lost, if its not you are a farmer
    final int FARM_RADIUS = 13;

    int leadVal00 = 0; int leadVal01 = 0; int leadVal02 = 0; int leadVal03 = 0; int leadVal04 = 0; int leadVal10 = 0; int leadVal11 = 0; int leadVal12 = 0; int leadVal13 = 0; int leadVal14 = 0; int leadVal20 = 0; int leadVal21 = 0; int leadVal22 = 0; int leadVal23 = 0; int leadVal24 = 0; int leadVal30 = 0; int leadVal31 = 0; int leadVal32 = 0; int leadVal33 = 0; int leadVal34 = 0; int leadVal40 = 0; int leadVal41 = 0; int leadVal42 = 0; int leadVal43 = 0; int leadVal44 = 0;
    int teammate_2_1 = 0; int teammate_20 = 0; int teammate_21 = 0; int teammate_1_2 = 0; int teammate_1_1 = 0; int teammate_10 = 0; int teammate_11 = 0; int teammate_12 = 0; int teammate0_2 = 0; int teammate0_1 = 0; int teammate01 = 0; int teammate02 = 0; int teammate1_2 = 0; int teammate1_1 = 0; int teammate10 = 0; int teammate11 = 0; int teammate12 = 0; int teammate2_1 = 0; int teammate20 = 0; int teammate21 = 0;

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        // Find the location of the archon that spawned you
        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert(archonLoc != null);

        RobotInfo[] nearbyDangerousEnemies = new RobotInfo[nearbyEnemies.length];
        int numDangerousEnemies = 0;
        int enemyDamage = 0;
        for (int i = nearbyEnemies.length; i-- > 0; ) {
            if (nearbyEnemies[i].getType() == RobotType.SOLDIER || nearbyEnemies[i].getType() == RobotType.WATCHTOWER) {
                nearbyDangerousEnemies[numDangerousEnemies] = nearbyEnemies[i];
                numDangerousEnemies++;
                enemyDamage += nearbyEnemies[i].getType().damage;
            }
        }

        // Calculate mineLocation
        comms.scanEnemyArchons(); // Costs 500 bytecode

        if (currentTarget == null) {
            currentTarget = comms.getArchonScoutingLocation(archonIndex);
            if (currentTarget == null) {
                currentTarget = nav.getRandomMapLocation();
            } else {
                indicatorString += "AT; ";
            }
        }

        if(canFarm){
            indicatorString += "YE; ";
        }
        else{
            indicatorString += "NA; ";
        }

        // Update comms farthest pushed miner
        for(int i = comms.FARTHEST_AWAY_MINER_START_IDX; i < comms.FARTHEST_AWAY_MINER_START_IDX + 4; i++){
            int currLocNum = rc.readSharedArray(i);
            boolean update = false;
            if(currLocNum == 0){
                update = true;
            }
        }

        indicatorString += "C " + rc.getMovementCooldownTurns() + ", " + rc.getActionCooldownTurns() + "; ";
        boolean movedTowardsGold = goToClosestGold();
        // System.out.println("Bytecode B: " + Clock.getBytecodesLeft()); // 5800 bytecode
        if (!movedTowardsGold) {
            // System.out.println("Bytecode C: " + Clock.getBytecodesLeft()); // 5700
//            if (enemyDamage > rc.getHealth() / 4) { // Retreat!
            if (numDangerousEnemies > 5) { // Retreat!
                MapLocation enemyCOM = Util.calculateEnemySoldierCOM(nearbyDangerousEnemies);
                Direction enemyDir = myLoc.directionTo(enemyCOM);
                MapLocation retreatLoc = myLoc.add(enemyDir).add(enemyDir).add(enemyDir).add(enemyDir);
                nav.goTo(retreatLoc);
            } else { // Run heuristic code
                if(farmerLoc != null){
//                if(false){
                    runFarmer();
                }
                else{
                    indicatorString += "EXP; ";
                    fillMapsUnrolled2(); // Costs 350 bytecode
                    fillAdjTeammate();
                    // System.out.println("Bytecode D: " + Clock.getBytecodesLeft()); // 4600
                    double currHeuristic = getHeursitic(myLoc, nearbyEnemies); // Costs 500 bytecode
                    // System.out.println("CH: " + currHeuristic);
                    double bestHeuristic = currHeuristic;
                    if(currHeuristic < 0){
                        MapLocation enemyCOM = Util.calculateEnemySoldierCOM(nearbyDangerousEnemies);
                        Direction retreatDir = myLoc.directionTo(enemyCOM).opposite();
                        nav.goTo(myLoc.add(retreatDir).add(retreatDir).add(retreatDir).add(retreatDir).add(retreatDir).add(retreatDir));
                    }
                    else if(currHeuristic != 0) {
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
                        } else {
                            indicatorString += "CH: " + (int) (currHeuristic * 100) + "; ";
                            indicatorString += "STAY; ";
                        }
                    } else { // Find nearest mine
                        // System.out.println("Bytecode G: " + Clock.getBytecodesLeft());
                        MapLocation targetLoc = findClosestLeadMine(ignore_farmer_threshold);
//                        MapLocation targetLoc = findClosestLeadMine(2);
                        // System.out.println("Bytecode H: " + Clock.getBytecodesLeft());
                        if (targetLoc == null) {
                            if(canFarm){
//                            if(false){
                                checkIfIShouldBeAFarmerAndCry();
                            }
                            // Go to a far away lead mine area
                            if (myLoc.distanceSquaredTo(currentTarget) <= 4) {
                                currentTarget = nav.getRandomMapLocation();
                            }
                            targetLoc = currentTarget;
                        }
                        nav.goTo(targetLoc);
                        // System.out.println("Bytecode I: " + Clock.getBytecodesLeft());
                        indicatorString += "NAV " + Util.mapLocationToInt(targetLoc) + "; ";
                    }
                }
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

    public void runFarmer() throws GameActionException {
        // Do farmer stuff
        indicatorString += "FMR " + farmerLoc.toString() + "; ";
        MapLocation[] leadMines = rc.senseNearbyLocationsWithLead(farmerLoc, FARM_RADIUS, 2);
        if(leadMines.length == 0){
            nav.circle(farmerLoc, 3, 13, true);
        }
        else{
            MapLocation closestMine = null;
            int closestDist = Integer.MAX_VALUE;
            for(int i = 0; i < leadMines.length; i++){
                int dist = myLoc.distanceSquaredTo(leadMines[i]);
                if(dist < closestDist){
                    closestDist = dist;
                    closestMine = leadMines[i];
                }
            }
            nav.goTo(closestMine);
        }
    }

    // TODO: Move away from enemies
    // 300 bytecode method
    public double getHeursitic(MapLocation center, RobotInfo[] nearbyEnemies) throws GameActionException { // 1000 bytecode
        if(!rc.canSenseLocation(center)){
            return -10000;
        }
//        RobotInfo[] enemyList = rc.senseNearbyRobots(center, myType.visionRadiusSquared, opponent);
//        System.out.println("Bytecode left A: " + Clock.getBytecodesLeft());

        double numEnemies = 0.0;
        for(int i = nearbyEnemies.length; i-- > 0; ){
            if(nearbyEnemies[i].type == RobotType.SOLDIER || nearbyEnemies[i].type == RobotType.WATCHTOWER){
//                numEnemies++;
//                double movesToReach = Util.minMovesToReach(center, nearbyEnemies[i].location);
//                numEnemies += 1.0 / movesToReach;
                numEnemies += 1.0 / Math.sqrt(center.distanceSquaredTo(nearbyEnemies[i].location));
            }
        }

//        System.out.println("Bytecode left B (" + nearbyEnemies.length + "): " + Clock.getBytecodesLeft());

        double leadMineable = numLeadMineableUnrolled(center.x - myLoc.x, center.y - myLoc.y); // 50 bytecode baby.
        double numTeammates = 0;
        int dx = myLoc.x - center.x;
        int dy = myLoc.y - center.y;

        if(leadMineable > 0){ // Only move away from adjacent teammates if ur busy mining lead
            numTeammates = getNumAdjTeammates(dx, dy);
        }

        if(leadMineable > 5){
            leadMineable = 5;
        }

        double cooldown = 10.0 + rc.senseRubble(center);
//        System.out.println("Heuristic: " + center.toString() + ", " + leadMineable + ", " + numTeammates + ", " + numEnemies);
//        System.out.println("Bytecode left C: " + Clock.getBytecodesLeft());
//        double val = leadMineable - numTeammates - numEnemies * 6;
        if(leadMineable == 0){
            return -numEnemies * 3 * cooldown;
        }
        return (leadMineable - numTeammates - numEnemies * 6) / cooldown; // Larger heuristic is better
    }

    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestLeadMine(int minLead) throws GameActionException {
        MapLocation bestLoc = null;
        int bestHeuristic = 100000;
        int dist; int cooldown; int heuristic;
        // Go to nearest lead mine
        MapLocation[] leadLocs = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared, minLead);
        // TODO Only go for the nearby leadloc if there's no miners that are closer (figure out how many turns itll take everyone to mine it and how useful you'd be)
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

    public void checkIfIShouldBeAFarmerAndCry() throws GameActionException {
        MapLocation[] currentFarmerLocs = new MapLocation[64];
        int numFarmerLocs = 0;
        for(int i = comms.FARMER_LOCATION_START_IDX; i <= comms.FARMER_LOCATION_END_IDX; i++){
            int val = rc.readSharedArray(i);
            if(val == 0){
                break;
            }
            currentFarmerLocs[i - comms.FARMER_LOCATION_START_IDX] = Util.intToMapLocation(val);
            numFarmerLocs++;
        }

        MapLocation[] farmersNearMe = new MapLocation[numFarmerLocs];
        int numFarmersNearMe = 0;
        for(int i = 0; i < numFarmerLocs; i++){
            if(myLoc.distanceSquaredTo(currentFarmerLocs[i]) <= myType.visionRadiusSquared * 4){
                farmersNearMe[numFarmersNearMe] = currentFarmerLocs[i];
                numFarmersNearMe++;
            }
        }

        MapLocation[] leadFarms = rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared, 2);
        MapLocation closestUnexploredLeadLoc = null;
        int closestDist = Integer.MAX_VALUE;
        for(int i = 0; i < leadFarms.length; i++){
            boolean isPartOfFarm = false;
            MapLocation leadLoc = leadFarms[i];
            for(int j = 0; j < numFarmersNearMe; j++){
                if(leadLoc.distanceSquaredTo(farmersNearMe[j]) < FARM_RADIUS){
                    isPartOfFarm = true;
                    break;
                }
            }
            if(!isPartOfFarm){
                int dist = myLoc.distanceSquaredTo(leadLoc);
                if(dist < closestDist){
                    closestDist = dist;
                    closestUnexploredLeadLoc = leadLoc;
                }
            }
        }

        if(closestUnexploredLeadLoc != null){
            // Set that to be ur farmer loc
            // Become a farmer
            // Cry cuz its drought season
            comms.addFarmerLoc(closestUnexploredLeadLoc);
            farmerLoc = closestUnexploredLeadLoc;
        }

    }

    public void tryMineAllDirections() throws GameActionException {
        for(int i = 0; i < Util.allDirections.length; i++){
            Direction dir = Util.allDirections[i];
            MapLocation loc = myLoc.add(dir);
            while (rc.canMineGold(loc)) {
                rc.mineGold(loc);
                indicatorString += "G;";
            }
        }

        for(int i = 0; i < Util.allDirections.length; i++){
            Direction dir = Util.allDirections[i];
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
        // // System.out.println("YEET: " + Clock.getBytecodesLeft());
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

    public int getNumAdjTeammates(int dx, int dy){
        switch(dx){
            case -1:
                switch(dy){
                    case -1:
                        return teammate_2_1 + teammate_1_2 + teammate_10 + teammate0_1;
                    case 0:
                        return teammate_20 + teammate_1_1 + teammate_11;
                    case 1:
                        return teammate_21 + teammate_12 + teammate_10 + teammate01;
                }
                break;
            case 0:
                switch(dy){
                    case -1:
                        return teammate_1_1 + teammate0_2 + teammate1_1;
                    case 0:
                        return teammate_10 + teammate0_1 + teammate10 + teammate01;
                    case 1:
                        return teammate_11 + teammate02 + teammate11;
                }
                break;
            case 1:
                switch(dy){
                    case -1:
                        return teammate0_1 + teammate1_2 + teammate10 + teammate2_1;
                    case 0:
                        return teammate1_1 + teammate11 + teammate20;
                    case 1:
                        return teammate01 + teammate12 + teammate10 + teammate21;
                }
                break;
        }
        assert(false);
        return -1;
    }

    public void fillAdjTeammate(){
        teammate_2_1 = 0; teammate_20 = 0; teammate_21 = 0; teammate_1_2 = 0; teammate_1_1 = 0; teammate_10 = 0; teammate_11 = 0; teammate_12 = 0; teammate0_2 = 0; teammate0_1 = 0; teammate01 = 0; teammate02 = 0; teammate1_2 = 0; teammate1_1 = 0; teammate10 = 0; teammate11 = 0; teammate12 = 0; teammate2_1 = 0; teammate20 = 0; teammate21 = 0;
        RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(5, myTeam);
        int dx; int dy; RobotInfo info;
        for(int i = nearbyTeammates.length; i-- > 0; ){
            info = nearbyTeammates[i];
            if(info.type != RobotType.MINER){
                continue;
            }
            dx = myLoc.x - info.location.x;
            dy = myLoc.y - info.location.y;
            switch(dx){
                case -2:
                    switch(dy){
                        case -1:
                            teammate_2_1 = 1;
                            break;
                        case 0:
                            teammate_20 = 1;
                            break;
                        case 1:
                            teammate_21 = 1;
                            break;
                    }
                    break;
                case -1:
                    switch(dy){
                        case -2:
                            teammate_1_2 = 1;
                            break;
                        case -1:
                            teammate_1_1 = 1;
                            break;
                        case 0:
                            teammate_10 = 1;
                            break;
                        case 1:
                            teammate_11 = 1;
                            break;
                        case 2:
                            teammate_12 = 1;
                            break;
                    }
                    break;
                case 0:
                    switch(dy){
                        case -2:
                            teammate0_2 = 1;
                            break;
                        case -1:
                            teammate0_1 = 1;
                            break;
                        case 1:
                            teammate01 = 1;
                            break;
                        case 2:
                            teammate02 = 1;
                            break;
                    }
                    break;
                case 1:
                    switch(dy){
                        case -2:
                            teammate1_2 = 1;
                            break;
                        case -1:
                            teammate1_1 = 1;
                            break;
                        case 0:
                            teammate10 = 1;
                            break;
                        case 1:
                            teammate11 = 1;
                            break;
                        case 2:
                            teammate12 = 1;
                            break;
                    }
                    break;
                case 2:
                    switch(dy){
                        case -1:
                            teammate2_1 = 1;
                            break;
                        case 0:
                            teammate20 = 1;
                            break;
                        case 1:
                            teammate21 = 1;
                            break;
                    }
                    break;
            }
        }
    }

}
