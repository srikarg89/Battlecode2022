package sprintbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
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
            turnCount += 1;  // We have now been alive for one more turn!
            if(rc.getRoundNum() > 500){
                rc.resign();
            }

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                robot.run();
                if(!checkBytecode(robot, rc)){
                    rc.resign();
                }
            } catch (GameActionException e) {
                Logger.Log("CAUGHT GAMEACTIONEXCEPTION");
                e.printStackTrace();
                rc.resign();
            } catch (Exception e) {
                Logger.Log("CAUGHT EXCEPTION");
                e.printStackTrace();
                rc.resign();
            } finally {
                Clock.yield();
            }
        }
    }

    public static boolean checkBytecode(Robot robot, RobotController rc) throws GameActionException {
        if(robot.roundNum != rc.getRoundNum()){
            System.out.println("Robot round num: " + robot.roundNum);
            System.out.println("Actual round num: " + rc.getRoundNum());
            System.out.println("BYTECODE ERROR REEEEEEEEEEEEEEEEEEEEEEEE");
            return false;
        }
        return true;
    }
}
