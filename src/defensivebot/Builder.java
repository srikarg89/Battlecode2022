package defensivebot;

import battlecode.common.*;

public class Builder extends Robot {


    MapLocation archonLoc = null;
    MapLocation lastBuiltArchon = null;
    int archonIndex = -1;

//    int BUFFER_RADIUS = 9; // how far apart should we build watchtowers?


    int mid_x = rc.getMapWidth()/2;
    int mid_y = rc.getMapHeight()/2;
    MapLocation middle = new MapLocation(mid_x, mid_y);


    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);



        nav.moveTowards(new MapLocation(mid_x, mid_y));



        if(myLoc.distanceSquaredTo(archonLoc) > 16) {
            RobotInfo[] nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared / 9, rc.getTeam());
            boolean buildWatchTower = rc.getTeamLeadAmount(rc.getTeam()) > 500;

            if (buildWatchTower)
                for (int i = 0; i < nearbyBots.length; i++) {
                    if (nearbyBots[i].getType().equals(RobotType.WATCHTOWER)) {
                        buildWatchTower = false;
                    }
                }

            if (buildWatchTower) {
                build();
            }
        }

    }



public void build() throws GameActionException{
    Direction dir = myLoc.directionTo(new MapLocation(mid_x, mid_y));
    Direction[] buildDirections = nav.closeDirections(dir);
    Direction build_dir = Util.tryBuild(RobotType.WATCHTOWER, buildDirections);

    if(!build_dir.equals(Direction.CENTER)){
        int x_incr = Util.directionToX(build_dir);
        int y_incr = Util.directionToY(build_dir);
        lastBuiltArchon = new MapLocation(x_incr, y_incr);
    }
}



    public void repair() throws GameActionException{

    }




}
