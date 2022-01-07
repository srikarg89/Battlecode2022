package resourceplayer;

import battlecode.common.*;


public class Soldier extends Robot {
    MapLocation archonLoc = null;

    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.ARCHON, RobotType.SOLDIER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY, RobotType.WATCHTOWER, RobotType.SAGE};

    public Soldier(RobotController rc) {
        super(rc);
    }


    // helper method used that finds position of enemy robot type in predefined array
    // to help us figure out which bot to destroy first
    public int getArrayIndex(RobotType[] arr,RobotType value) {
        int k=0;
        for(int i=0;i<arr.length;i++){

            if(arr[i]==value){
                k=i;
                break;
            }
        }
        return k;
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
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);

        MapLocation toAttack = null;
        int oppTypeIndex = 10;
        RobotType oppType = null;
        int distance = Integer.MAX_VALUE;


        for (RobotInfo info : enemies) {
            int currOppTypeIndex = getArrayIndex(priorityOrder, info.type);

            if (oppType == null) {
                toAttack = info.location;
                oppTypeIndex = currOppTypeIndex;
                oppType = info.type;
                distance = myLoc.distanceSquaredTo(info.location);
            }

            // switch if we prioritize current opp. robot type more
            else if (currOppTypeIndex < oppTypeIndex) {
                toAttack = info.location;
                oppType = info.type;
                oppTypeIndex = currOppTypeIndex;
                distance = myLoc.distanceSquaredTo(info.location);
            }

            // if same type as robot already found, only switch if we're closer
            else if (currOppTypeIndex == oppTypeIndex) {
                int currDistance = myLoc.distanceSquaredTo(info.location);
                if (currDistance < distance) {
                    toAttack = info.location;
                    distance = currDistance;
                }
            }
        }

        if (toAttack != null && rc.canAttack(toAttack)) {
            rc.attack(toAttack);
        }




        // Moving
        // TODO: make movement smarter
        else {
            nav.moveAwayFrom(archonLoc);
        }
    }
    }

