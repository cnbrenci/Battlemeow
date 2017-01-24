package MeowPlayer;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        try {
            // Here, we've separated the controls into a different method for each RobotType.
            // You can add the missing ones or rewrite this into your own control structure.
            switch (rc.getType()) {
                case ARCHON:
                    Archon archon = new Archon(rc);
                    archon.run();
                    break;
                case GARDENER:
                    WanderingGardener gardener = new WanderingGardener(rc);
                    //PathTester gardener = new PathTester(rc);
                    gardener.run();
                    break;
                case SOLDIER:
                    Soldier soldier = new Soldier(rc);
                    soldier.run();
                    break;
                case LUMBERJACK:
                    Lumberjack lumberjack = new Lumberjack(rc);
                    lumberjack.run();
                    break;
                case SCOUT:
                    //Scout scout = new Scout(rc);
                    ScoutHunter scout = new ScoutHunter(rc);
                    scout.run();
                    break;
            }
        }
        catch(Exception e){
            System.out.println("Unexpected Exception");
            e.printStackTrace();
        }
    }
}
