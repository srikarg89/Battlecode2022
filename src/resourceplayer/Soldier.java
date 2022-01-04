package resourceplayer;

import battlecode.common.*;

public class Soldier extends Robot {

    public Soldier(RobotController rc){
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Util.tryMove(Navigation.directions);
    }

}
