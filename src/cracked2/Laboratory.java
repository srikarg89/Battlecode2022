package cracked2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Laboratory extends Robot {

    int prevLead = 0;

    public Laboratory(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        int lead = rc.getTeamLeadAmount(myTeam);
        int leadDiff = lead - prevLead;
        if(rc.canTransmute() && leadDiff > 3 * rc.getTransmutationRate()){
            rc.transmute();
            rc.setIndicatorString("Transmuting");
        }

        prevLead = lead;
    }


}