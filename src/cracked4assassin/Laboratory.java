package cracked4assassin;

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
        indicatorString += "LTR: " + rc.getTransmutationRate() + ";";
        indicatorString += "CT?: " + rc.canTransmute() + ";";
        indicatorString += "AC: " + rc.getActionCooldownTurns() + ";";
        indicatorString += "CT?: " + rc.canTransmute() + ";";
        if(rc.canTransmute()){
            rc.transmute();
            rc.setIndicatorString("Transmuting");
        }

        prevLead = lead;
        checkPossibleDeath();
    }


}