package MeowPlayer;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Soldier extends Robot{

    public Soldier(RobotController rc) {
        super(rc);
    }

    @Override
    public void runOneTurn() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        // If there are some...
        if (robots.length > 0) {
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
