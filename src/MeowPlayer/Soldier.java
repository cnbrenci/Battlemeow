package MeowPlayer;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Soldier extends Robot{

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
        if(Messenger.incrementSoldiersCreatedCount(rc) == 1)
            System.out.println("I'm the first Soldier on my team!");
    }

    @Override
    public void runOneTurn() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        // If there are some...
        if (robots.length > 0) {
            for (RobotInfo robot: robots)
            {
                int targetId;

                if (robot.getType() == RobotType.GARDENER)
                {
                    rc.move(robot.getLocation().directionTo(robot.location));
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robot.location));
                    }
                }
            }
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }

        // Move randomly
        tryMove(Utils.randomDirection());
    }
}
