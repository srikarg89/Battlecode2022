package cracked4BuildOrder;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
// ANOMALY NOTE FOR VISUALIZER: Charge is yellow, vortex is purple

public strictfp class RobotPlayer {

    public static void run(RobotController rc) throws GameActionException {

        // You can also use indicators to save debug notes in replays.
        Robot robot;

        switch (rc.getType()) {
            case ARCHON:     robot = new Archon(rc);  break;
            case MINER:      robot = new Miner(rc);   break;
            case SOLDIER:    robot = new Soldier(rc); break;
            case LABORATORY: robot = new Laboratory(rc); break;
            case WATCHTOWER: robot = new WatchTower(rc); break;
            case BUILDER:    robot = new Builder(rc); break;
            case SAGE:       robot = new Sage(rc); break;
            default:         robot = new Archon(rc); break;
        }

        while (true) {
//            if(rc.getRoundNum() > 800){
//                System.out.println("Resigning XD");
//                rc.resign();
//            }

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                robot.indicatorString = "";
                robot.run();
                rc.setIndicatorString(robot.indicatorString);
//                System.out.println("Bytecode left: " + Clock.getBytecodesLeft());
                robot.prevLoc = rc.getLocation();
                if(!checkBytecode(robot, rc)){
//                    rc.resign();
                }
            } catch (GameActionException e) {
                Logger.Log("CAUGHT GAMEACTIONEXCEPTION");
                e.printStackTrace();
//                rc.resign();
            } catch (Exception e) {
                Logger.Log("CAUGHT EXCEPTION");
                e.printStackTrace();
//                rc.resign();
            } finally {
                Clock.yield();
            }
        }
    }

    public static boolean checkBytecode(Robot robot, RobotController rc) throws GameActionException {
        if(robot.roundNum != rc.getRoundNum()){
            System.out.println("Robot round num: " + robot.roundNum);
            System.out.println("Actual round num: " + rc.getRoundNum());
            System.out.println("BYTECODE ERROR REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
            return false;
        }
        return true;
    }
}
