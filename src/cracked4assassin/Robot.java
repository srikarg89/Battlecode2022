package cracked4assassin;

import battlecode.common.*;

public class Robot {

    // Constants
    int mapWidth;
    int mapHeight;
    int roundNum = 1;
    double SQRT_2 = Math.sqrt(2);
    double SQRT_3 = Math.sqrt(3);
    double SQRT_4 = Math.sqrt(4);

    // Robot properties
    RobotController rc;
    Navigation nav;
    Comms comms;
    MapLocation myLoc;
    MapLocation prevLoc;
    Team myTeam;
    Team opponent;
    RobotType myType;
    int age = 0;
    MapLocation[] friendlyArchons;
    int numFriendlyArchons = 0;
    RobotInfo[] nearby;
    RobotInfo[] nearbyFriendlies;
    RobotInfo[] nearbyEnemies;
    String indicatorString = "";
    boolean mightDie = false;
    MapLocation closestFriendlyArchon;
    MapLocation closestEnemyArchon;

    public Robot(RobotController rc) throws GameActionException {
        myLoc = rc.getLocation();
        this.rc = rc;
        Util.rc = rc;
        Util.robot = this;
        Logger.myID = rc.getID();
        Logger.myType = rc.getType();
        Logger.robot = this;
        myTeam = rc.getTeam();
        opponent = myTeam.opponent();
        myType = rc.getType();
        age = 0;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
//        rubbleMap = new int[mapWidth][mapHeight];
        nav = new Navigation(rc, this);
        comms = new Comms(rc, this);
        if(myType != RobotType.ARCHON){
            comms.findFriendlyArchons();
        }
    }

    public void run() throws GameActionException {
        Logger.Log("My Location: " + rc.getLocation());
        myLoc = rc.getLocation();
        roundNum = rc.getRoundNum();
        nearby = rc.senseNearbyRobots();
        nearbyFriendlies = rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam);
        nearbyEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, opponent);
//        System.out.println("Robot run method: " + Clock.getBytecodesLeft());
        nav.update();
        if(rc.getRoundNum() == 2 && myType == RobotType.ARCHON){
            // Find teammate archons
            comms.findFriendlyArchons();
        }
        // TODO: Only do this for scouts, cuz most likely by the time anyone else gets there the archon will have moved
        if(rc.getRoundNum() >= 2){
//            comms.determineSymmetry();
        }
        comms.runRubbleBasedSymmetry();
        comms.scanEnemyArchons();

        int closestFriendlyArchonIdx = comms.getClosestFriendlyArchonIndex();
        closestFriendlyArchon = Util.intToMapLocation(rc.readSharedArray(closestFriendlyArchonIdx));
        closestEnemyArchon = comms.getClosestEnemyArchonOnComms();
        if(closestEnemyArchon == null){
            int symmetry = rc.readSharedArray(comms.SYMMETRY_IDX);
            if(symmetry == 1 || symmetry == 2 || symmetry == 4){
                MapLocation[] potentialEnemyLocs = comms.getPotentialEnemyArchonLocations();
                int bestDist = Integer.MAX_VALUE;
                for(int i = 0; i < potentialEnemyLocs.length; i++){
                    int dist = myLoc.distanceSquaredTo(potentialEnemyLocs[i]);
                    if(closestEnemyArchon == null || dist < bestDist){
                        closestEnemyArchon = potentialEnemyLocs[i];
                        bestDist = dist;
                    }
                }
            }
        }
//        System.out.println("My location: " + rc.getLocation());
//        System.out.println("Closest enemy archon: " + closestEnemyArchon);
//        System.out.println("Closest friendly archon: " + closestFriendlyArchon);
        comms.updateClosestEnemyTroop(closestFriendlyArchon, closestEnemyArchon, nearbyEnemies);
        age++;
    }

    public void checkPossibleDeath() throws GameActionException {
        nearbyEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, opponent);
        int enemyDmg = Util.getPotentialDamage(nearbyEnemies);
        boolean potentiallyDying = enemyDmg >= rc.getHealth() || rc.getHealth() <= RobotType.SOLDIER.damage;
        if(potentiallyDying && !mightDie){ // If ur in danger of dying
            System.out.println("OMG I MIGHT DIE");
            comms.addRobotCount(myType, -1); // Let everyone know that you might die (and thus don't count yourself as an alive robot)
            mightDie = true;
        }
        else if(!potentiallyDying && mightDie){
            System.out.println("Nvm we gucci");
            comms.addRobotCount(myType, 1); // Let everyone know that you are actually alive and well
            mightDie = false;
        }
    }

}
