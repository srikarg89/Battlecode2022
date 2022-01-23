package sprintbot8SageLabs;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Laboratory extends Robot {

    public Laboratory(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        if(rc.canTransmute() && rc.getTeamLeadAmount(rc.getTeam()) > 3*rc.getTransmutationRate()){
            rc.transmute();
            rc.setIndicatorString("Transmuting");
        }
    }


}