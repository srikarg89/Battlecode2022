package cracked4BuildOrder;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Laboratory extends Robot {

    int prevLead = 0;
    int minerCount = 0;
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

        minerCount = comms.getRobotCount(RobotType.MINER);
        int initialMinerCount = 3; // scale this according to map size
        double scalingFactor = Math.sqrt(rc.getMapHeight() * rc.getMapWidth());
        initialMinerCount *= scalingFactor/15;
        if(rc.canTransmute() && minerCount>=initialMinerCount){
            rc.transmute();
            rc.setIndicatorString("Transmuting");
        }

        prevLead = lead;
        checkPossibleDeath();
    }


}