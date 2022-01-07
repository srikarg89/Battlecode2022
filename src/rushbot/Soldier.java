package rushbot;

import battlecode.common.*;


public class Soldier extends Robot {
    MapLocation archonLoc = null;

    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.SAGE, RobotType.SOLDIER, RobotType.WATCHTOWER, RobotType.ARCHON, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY};

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
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
                        System.out.println("Found my archon loc: " + archonLoc.toString());
                    }
                }
            }
        }
        assert (archonLoc != null);


        // Attacking
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(myType.actionRadiusSquared, opponent);

        MapLocation toAttack = null;
        int oppTypeIndex = 10;
        RobotType oppType = null;
        int distance = Integer.MAX_VALUE;


        for (int i = 0; i < enemies.length; i++) {
            RobotInfo info = enemies[i];
            // helper method used that finds position of enemy robot type in predefined array
            // to help us figure out which bot to destroy first
            int currOppTypeIndex = Util.getArrayIndex(priorityOrder, info.type);
            int currDistance = myLoc.distanceSquaredTo(info.location);

            if (oppType == null) {
                toAttack = info.location;
                oppTypeIndex = currOppTypeIndex;
                oppType = info.type;
                distance = currDistance;
            }

            // switch if we prioritize current opp. robot type more
            else if (currOppTypeIndex < oppTypeIndex) {
                toAttack = info.location;
                oppType = info.type;
                oppTypeIndex = currOppTypeIndex;
                distance = currDistance;
            }

            // if same type as robot already found, only switch if we're closer
            else if (currOppTypeIndex == oppTypeIndex) {
                if (currDistance < distance) {
                    toAttack = info.location;
                    distance = currDistance;
                }
            }
        }

        // Attack the highest priority enemy
        if (toAttack != null && rc.canAttack(toAttack)) {
            rc.attack(toAttack);
        }
        // Otherwise move towards enemy archon
        else {
            MapLocation targetLoc = comms.searchForEnemyArchons();
            nav.goTo(targetLoc);
        }
    }
}
