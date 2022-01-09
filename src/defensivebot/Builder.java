package defensivebot;

import battlecode.common.*;

public class Builder extends Robot {


    MapLocation archonLoc = null;
    MapLocation lastBuiltTower = null;
    int archonIndex = -1;
    boolean needToRepair = false;

    MapLocation[] watchTowerBuildSpots = new MapLocation[8];
    int numSpots = 0;
    int spotIndex = 0;  // index value used to mark which build spot we're navigating to
    boolean canBuild = false;
    int direction_multiplier = rc.getRoundNum()/100*4;

    int mid_x = rc.getMapWidth()/2;
    int mid_y = rc.getMapHeight()/2;
    MapLocation middle = new MapLocation(mid_x, mid_y);



    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }


    public void run() throws GameActionException {
        super.run();
//        System.out.println(numSpots +", " + spotIndex);
//        rc.setIndicatorString(""+needToRepair);
//        desiredDistanceFromArchonSquared = rc.getRobotCount() / 5;  //arbitrary, need to fix


        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);

        boolean repaired = repair();
        // TODO: Move out of the archon's way when spawned
        if(repaired){

        }
        else if(numSpots == spotIndex){         // generate new build spots to build at
            generateBuildSpots(direction_multiplier);
            direction_multiplier += 3;
            canBuild = false;
            spotIndex = 0;      //start from the beginning of the generated spots
        }
        else if(!canBuild){             // still travelling to a build spot
            nav.minDistToSatisfy = 3;
            nav.goTo(watchTowerBuildSpots[spotIndex]);
            canBuild = myLoc.distanceSquaredTo(watchTowerBuildSpots[spotIndex]) < 9;
        }

        else if(canBuild && rc.getTeamLeadAmount(rc.getTeam()) > RobotType.WATCHTOWER.buildCostLead * 3){      // time to build a tower :D
            Direction dir = myLoc.directionTo(archonLoc);           // build in direction opposite of the home archon (closer to opposing soldiers)
            Direction[] buildDirections = nav.closeDirections(dir.opposite());
            Direction build_dir = Util.tryBuild(RobotType.WATCHTOWER, buildDirections);
            if(build_dir != Direction.CENTER){      // yay we built a tower
                needToRepair = true;                // need to get the tower we just built to max health
                lastBuiltTower = myLoc.add(build_dir);
                canBuild = false;                   // set can build to false so we move to the next build location
                spotIndex++;                        // increment index in array to next spot
            }
        }

    }

    // Build a 3x3 spaced out watchtower fortress
    public void generateBuildSpots(int directionMultiplier) throws GameActionException{
        //generates potential build locations in all cardinal directions going outwards from home archon
        // builder can go to every one and try to build a watchtower there
//        System.out.println("hello");
        final Direction[] directions = Util.closeDirections(myLoc.directionTo(middle)); // build towers towards the middle of the map
        // TODO: need to change to the direction of enemy archons

        this.numSpots = 0;
        for(int i=3; i< 7; i++){        // go in the first 4 best directions
            Direction direction = directions[i];
            MapLocation potentialBuildSpot = Util.multiplyDirection(this.archonLoc, direction, directionMultiplier);

            // check if this is a valid mapLocation
            if(potentialBuildSpot.x < rc.getMapWidth() && potentialBuildSpot.x > 0 && potentialBuildSpot.y < rc.getMapHeight() && potentialBuildSpot.y > 0){
                this.watchTowerBuildSpots[numSpots] = potentialBuildSpot;
                numSpots++;
            }
        }
//        System.out.println(numSpots);
    }


    public boolean repair() throws GameActionException {
        // Try repairing nearby watchtowers
        boolean repaired = false;
        RobotInfo[] potentialTowers = rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam);
        for (int i = 0; i < potentialTowers.length; i++) {
            if(potentialTowers[i].type != RobotType.WATCHTOWER){
                continue;
            }
            int max_health = RobotType.WATCHTOWER.getMaxHealth(potentialTowers[i].getLevel());
            if(potentialTowers[i].getHealth() >= max_health){
                continue; // No need to repair this boi
            }
            while (potentialTowers[i].getHealth() < max_health && rc.isActionReady()) {
                repaired = true;
                rc.repair(potentialTowers[i].location);
                rc.setIndicatorString("Repairing da boi");;
            }
        }
        return repaired;
    }



}
