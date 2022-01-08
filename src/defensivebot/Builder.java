package defensivebot;

import battlecode.common.*;

public class Builder extends Robot {


    MapLocation archonLoc = null;
    MapLocation lastBuiltArchon = null;
    int archonIndex = -1;
    int desiredDistanceFromArchonSquared = 9;



    int mid_x = rc.getMapWidth()/2;
    int mid_y = rc.getMapHeight()/2;
    MapLocation middle = new MapLocation(mid_x, mid_y);




    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }


    public void run() throws GameActionException {
        super.run();

        desiredDistanceFromArchonSquared = rc.getRobotCount() / 5;  //arbitrary, need to fix
        if(archonIndex == -1){
            archonIndex = comms.getClosestFriendlyArchonIndex();
            archonLoc = Util.intToMapLocation(rc.readSharedArray(archonIndex));
        }
        assert (archonLoc != null);

        move();



        if(myLoc.distanceSquaredTo(archonLoc) > desiredDistanceFromArchonSquared) {
            RobotInfo[] nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared / 9, rc.getTeam());
            boolean buildWatchTower = rc.getTeamLeadAmount(rc.getTeam()) > 500;

            if (buildWatchTower) {
                for (int i = 0; i < nearbyBots.length; i++) {
                    if (nearbyBots[i].getType().equals(RobotType.WATCHTOWER)) {
                        buildWatchTower = false;
                    }
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


    public void move() throws GameActionException{
        if(myLoc.distanceSquaredTo(archonLoc) < desiredDistanceFromArchonSquared){
            nav.moveTowards(middle);
        }

        else if(myLoc.distanceSquaredTo(archonLoc) > desiredDistanceFromArchonSquared){
            nav.moveTowards(archonLoc);
        }

        else if (lastBuiltArchon != null){
            nav.moveAwayFrom(lastBuiltArchon);
        }

        else{
            nav.moveTowards(nav.getRandomMapLocation());
        }
    }


    public void repair() throws GameActionException{
    }




}
