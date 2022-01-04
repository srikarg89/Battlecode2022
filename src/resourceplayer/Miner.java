package resourceplayer;

import battlecode.common.*;

public class Miner extends Robot {

    MapLocation archonLoc = null;

    public Miner(RobotController rc){
        super(rc);
        System.out.println("Running constructor!");
    }

    public void run() throws GameActionException {
        super.run();
        // Find the location of the archon that spawned you
        if(archonLoc == null) {
            for (Direction dir : Navigation.directions) {
                MapLocation testLoc = myLoc.add(dir);
                if (rc.canSenseRobotAtLocation(testLoc)) {
                    RobotInfo info = rc.senseRobotAtLocation(testLoc);
                    if (info.getType() == RobotType.ARCHON && info.getTeam() == myTeam) {
                        archonLoc = testLoc;
                        System.out.println("Found my archon loc: " + archonLoc.toString());
                    }
                }
            }
        }
        assert(archonLoc != null);

        // TODO: If you're blocking the way of a fellow miner, move forward so that he can help you mine
        // Movement strat: if you're right next to a miner and there's mineable blocks if you separate yourself, then separate yourself.
        MapLocation mineLocation = checkMineable(myLoc);
        if(mineLocation == null) {
            nav.moveAwayFrom(myLoc.directionTo(archonLoc));
        }
        else if(rc.isMovementReady()){
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
        mineLocation = checkMineable(myLoc);
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

    public void tryMine(MapLocation mineLocation) throws GameActionException {
        while (rc.canMineGold(mineLocation)) {
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation)) {
            rc.mineLead(mineLocation);
        }
    }

}
