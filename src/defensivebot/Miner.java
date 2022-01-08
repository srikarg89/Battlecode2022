package defensivebot;

import battlecode.common.*;

public class Miner extends Robot {

    MapLocation archonLoc = null;
    MapLocation mineLocation = null;
    Direction spawnDir = null;
    Direction[] shuffled = Util.shuffleArr(Util.directions);


    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        System.out.println("Running constructor!");
    }

    public void run() throws GameActionException {
        super.run();
//        System.out.println("Round number: " + rc.getRoundNum());
        // Find the location of the archon that spawned you
        if(archonLoc == null) {
            for (Direction dir : Navigation.directions) {
                MapLocation testLoc = myLoc.add(dir);
                if (rc.canSenseRobotAtLocation(testLoc)) {
                    RobotInfo info = rc.senseRobotAtLocation(testLoc);
                    if (info.getType() == RobotType.ARCHON && info.getTeam() == myTeam) {
                        archonLoc = testLoc;
                        spawnDir = info.getLocation().directionTo(myLoc);
                        System.out.println("Found my archon loc: " + archonLoc.toString());
                    }
                }
            }
        }
        assert(archonLoc != null);

        // TODO: If you're blocking the way of a fellow miner, move forward so that he can help you mine
        // Movement strat: if you're right next to a miner and there's mineable blocks if you separate yourself, then separate yourself.

        // Calculate mineLocation
        comms.scanEnemyArchons();
        MapLocation mineLocation = findClosestMine();

        // If there's nowhere that I can sense to mine, find a new mineLocation
        if(mineLocation == null){
//            nav.moveAwayFrom(archonLoc);
//            Direction away = myLoc.directionTo(archonLoc).opposite();

            // Do some sort of intelligent scouting stuff
//            MapLocation target = Util.multiplyDirection(myLoc, spawnDir, Math.max(rc.getMapWidth(), rc.getMapHeight()));
            // Move away from spawning archon
//            nav.goTo(target);
//            Util.tryMove(shuffled); // move randomly
//            nav.moveAwayFrom(archonLoc);


//            MapLocation targetLoc = comms.searchForEnemyArchons();
//            if(targetLoc != null) nav.goTo(targetLoc);
//            else

            int currDistance = myLoc.distanceSquaredTo(archonLoc);
            int scaledRadius = comms.getRobotCount(RobotType.SOLDIER)/numFriendlyArchons;

             MapLocation targetLoc = comms.searchForEnemyArchons();

            if(currDistance > scaledRadius/0.1 && Math.random() < 0.5)
                nav.moveTowards(archonLoc);

            else
                nav.moveAwayFrom(archonLoc);
//                Util.tryMove(shuffled);

        }

        else if(myLoc.distanceSquaredTo(mineLocation) > 2){
            nav.goTo(mineLocation);
        }


        else if(rc.isMovementReady()){
            // Spreading out code
            int nearby = rc.senseNearbyRobots(2, myTeam).length;
            Direction bestMoveDir = Direction.CENTER;
            for(Direction dir : Navigation.directions){
                MapLocation newCenter = myLoc.add(dir);
                if(!rc.canMove(dir)){
                    continue;
                }
                if(checkMineable(newCenter) == null){
                    continue;
                }
                int robotsNearNewCenter = rc.senseNearbyRobots(newCenter, 2, myTeam).length;
                if(robotsNearNewCenter < nearby){
                    nearby = robotsNearNewCenter;
                    bestMoveDir = dir;
                }
            }
            if(bestMoveDir != Direction.CENTER){
                Util.tryMove(bestMoveDir);
            }
        }

        // Try to mine on squares around us.
        if(mineLocation != null){
            System.out.println("Trying to mine at: " + mineLocation.toString());
            tryMine(mineLocation);
        }

    }

    // Find the location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation checkMineable(MapLocation center) throws GameActionException {
        MapLocation bestLoc = null;
        int bestLead = 0;
        int bestGold = 0;
        for (Direction dir : Navigation.allDirections) { // Maximum mine radius is 2
            MapLocation newLoc = center.add(dir);
            if(!rc.canSenseLocation(newLoc)){
                continue;
            }
            int lead = rc.senseLead(newLoc);
            int gold = rc.senseGold(newLoc);
            if(bestGold > gold){ // We value gold more than lead
                continue;
            }
            if(bestGold == gold && bestLead >= lead){ // If the gold reserves are equal, check which one has more lead
                continue;
            }
            bestLead = lead;
            bestGold = gold;
            bestLoc = newLoc;
        }
        return bestLoc;
    }

    // Find the closest location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation findClosestMine() throws GameActionException {
        MapLocation bestLoc = null;
        int bestDist = 100000;
        boolean foundGold = false;
        // Go to nearest gold mine
        MapLocation[] sensable = rc.getAllLocationsWithinRadiusSquared(myLoc, myType.visionRadiusSquared);
        for(MapLocation newLoc : sensable){
            if(!rc.canSenseLocation(newLoc)){
                continue;
            }
            int gold = rc.senseGold(newLoc);
            int lead = rc.senseLead(newLoc);
            int dist = myLoc.distanceSquaredTo(newLoc);
            if(gold == 0 && lead <= 1){ // Ignore it if it doesn't have gold or lead
                continue;
            }
            if(gold == 0 && foundGold){ // Ignore a non-gold repository if we alr found gold
                continue;
            }
            if((gold > 0 && !foundGold) || (dist < bestDist)){
                bestLoc = newLoc;
                bestDist = dist;
                if(gold > 0){
                    foundGold = true;
                }
            }
        }
        return bestLoc;
    }

    public void tryMine(MapLocation mineLocation) throws GameActionException {
        while (rc.canMineGold(mineLocation)) {
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation)>1) {  // don't mine if therre's one lead so we can regenerate
            rc.mineLead(mineLocation);
        }
    }

}
