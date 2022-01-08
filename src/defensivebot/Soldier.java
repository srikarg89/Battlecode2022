package defensivebot;

import battlecode.common.*;

public class Soldier extends Robot {
    MapLocation archonLoc = null;
    int archonIndex = -1;

    // Proportion of soldiers that are defensive (offensive will go to enemy, defensive will stay close to spawning archon)


    // might be smart to have a few created for each archon in the beginning of each game

    // allow us to have different types of soldiers with different movement behaviour
    String type = "offensive"; // defensive, scouting
    Direction[] shuffled = Util.shuffleArr(Util.directions);


    //list defining what we should destroy first
    RobotType[] priorityOrder = {RobotType.ARCHON, RobotType.SOLDIER, RobotType.MINER,
            RobotType.BUILDER, RobotType.LABORATORY, RobotType.WATCHTOWER, RobotType.SAGE};


    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }


    public Soldier(RobotController rc, String type) throws GameActionException {
        super(rc);
        this.type = type;

    }


    // helper method used that finds position of enemy robot type in predefined array
    // to help us figure out which bot to destroy first
    public int getArrayIndex(RobotType[] arr, RobotType value) {
        int k = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(value)) {
                k = i;
                break;
            }
        }
        return k;
    }

    public void run() throws GameActionException {
        super.run();
        System.out.println("lead amount: " + rc.getTeamLeadAmount(this.myTeam));
        //lifted from miner code

        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
            System.out.println("found my archonIndex and archonLoc");
        }


//        if (archonLoc == null) {
//            for (Direction dir : Navigation.directions) {
//                MapLocation testLoc = myLoc.add(dir);
//                if (rc.canSenseRobotAtLocation(testLoc)) {
//                    RobotInfo info = rc.senseRobotAtLocation(testLoc);
//                    if (info.getType() == RobotType.ARCHON && info.getTeam() == myTeam) {
//                        archonLoc = testLoc;
//                        System.out.println("Found my archon loc: " + archonLoc.toString());
//                    }
//                }
//            }
//        }
        assert (archonLoc != null);


        // Attacking (same for both offensive and defensive soldiers)
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


        // Movement
        else {

            if (this.type == "defensive") {      // movement code for defensive soldier (
                // explore area around spawned archon, but stay close
                // defensive bubble increases as rounds progress so we don't crowd up area around archon


                // check if there is a center of mass which we should navigate to
                int compressedCenterOfMass = rc.readSharedArray(comms.CENTER_OF_ATTACKING_MASS_IDX + archonIndex);

                if(compressedCenterOfMass != comms.MAX_COMMS_VAL){
                    System.out.println("DEFEND THE KINGDOM!!!!");
                    int[] coordinates = Util.compressedToXAndY(compressedCenterOfMass);
                    nav.moveTowards(new MapLocation(coordinates[0], coordinates[1]));
                }

                else{
                    int currDistance = myLoc.distanceSquaredTo(archonLoc);
                    int scaledRadius = comms.getRobotCount(RobotType.SOLDIER) / numFriendlyArchons;

                    int DEFENSIVE_BUBBLE_CONSTANT = 3;
                    if (currDistance < (scaledRadius / DEFENSIVE_BUBBLE_CONSTANT)) {  // units surround spawning archon with specified radius
                        nav.moveAwayFrom(archonLoc);
                    } else if (currDistance < scaledRadius) {    // explore defensive bubblee
                        Util.tryMove(shuffled);
                    } else {   // move back towards spawning archon
                        nav.moveTowards(archonLoc);
                    }
                }
            }

            else {      // movement code for offensive soldier
                // TODO: move in direction of previously attacked enemy bot
                // TODO: need to incorporate comms to share enemy archon locations instead of just moving away from own archon
                nav.moveAwayFrom(archonLoc);
            }

            Clock.yield();

        }
    }
}

