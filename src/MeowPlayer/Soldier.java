package MeowPlayer;
import MeowMovement.PathPlanner2;
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

    PathPlanner2 pathHandler;

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

                // check if each one is a gardener
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


        boolean isFirstSoldier = false;
        MapLocation[] EnemyArchonLocations = rc.getInitialArchonLocations(enemy);

        // Check if this is the first soldier
        if (Messenger.getSoldiersCreatedCount(rc) == 1)
        {
            isFirstSoldier = true;
        }

        // if so, rush!
        if (isFirstSoldier)
        {
            // If there's only one enemy Archon, try to move to it
            if (EnemyArchonLocations.length == 1)
            {
                pathHandler.move(EnemyArchonLocations[0]);
            }
            // if there are multiple archons, find the closest one and try to move to it
            else
            {
                float distanceToArchon = 1000000000;
                for (MapLocation EnenmyArchonLocation : EnemyArchonLocations)
                {
                    if (rc.getLocation().distanceTo(EnenmyArchonLocation) < distanceToArchon)
                    {
                        distanceToArchon = rc.getLocation().distanceTo(EnenmyArchonLocation);
                    }
                    pathHandler.move(EnenmyArchonLocation);
                }
            }
        }

        // Move randomly
        tryMove(Utils.randomDirection());
    }
}
