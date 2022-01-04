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

        // Try to mine on squares around us.
        MapLocation mineLocation = checkMineable();
        if(mineLocation != null){
            System.out.println("Trying to mine at: " + mineLocation.toString());
            tryMine(mineLocation);
        }
        else if(archonLoc != null) { // Otherwise try to move in the direction opposite the Archon.
            nav.moveAwayFrom(myLoc.directionTo(archonLoc));
        }
        else{
            System.out.println("ERROR DIDNT FIND ARCHON THAT SPAWNED ME");
        }
    }

    // Find the location with the most reserves (gold and lead) to mine from. If there is no location to mine from, return null
    public MapLocation checkMineable() throws GameActionException {
        MapLocation bestLoc = null;
        int bestLead = 0;
        int bestGold = 0;
        for (Direction dir : Navigation.allDirections) { // Maximum mine radius is 2
            MapLocation newLoc = myLoc.add(dir);
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
