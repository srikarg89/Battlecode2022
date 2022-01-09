package defensivebot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Laboratory extends Robot {

    public Laboratory(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        if(rc.canTransmute() && rc.getTeamLeadAmount(rc.getTeam()) > 10*rc.getTransmutationRate()){
            rc.transmute();
            rc.setIndicatorString("Transmuting");
        }
    }

}
