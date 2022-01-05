package resourceplayer;

import battlecode.common.*;


public class Soldier extends Robot {
    MapLocation archonLoc = null;

    public Soldier(RobotController rc) {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // Try to attack someone
        MapLocation me = rc.getLocation();

        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);

        MapLocation toAttack = null;
        RobotType oppType = null;
        int distance = Integer.MAX_VALUE;


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


        for (RobotInfo info : enemies) {
            if (oppType == null) {
                toAttack = info.location;
                oppType = info.type;
                distance = me.distanceSquaredTo(info.location);
            }

            // switch if we prioritize current robot type more
            //TODO: need to add all the other types of robots as well
            else if (info.type == RobotType.ARCHON && oppType == RobotType.SOLDIER
                    || (info.type == RobotType.SOLDIER && oppType == RobotType.BUILDER)) {
                toAttack = info.location;
                oppType = info.type;
                distance = me.distanceSquaredTo(info.location);
            }

            // if same type as robot already found, only switch if we're closer
            else if (info.type == oppType) {
                int currDistance = me.distanceSquaredTo(info.location);
                if (currDistance < distance) {
                    toAttack = info.location;
                    oppType = info.type;
                    distance = currDistance;
                }
            }
        }

        if (toAttack != null && rc.canAttack(toAttack)) {
            rc.attack(toAttack);
        }


        // movement
        // TODO: make movement smarter
        else {
            nav.moveAwayFrom(myLoc.directionTo(archonLoc));
        }
    }
    }

