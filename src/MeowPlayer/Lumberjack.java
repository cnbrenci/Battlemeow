package MeowPlayer;
import MeowMovement.PathPlanner2;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Lumberjack extends Robot{
    private PathPlanner2 pathHandler;
    private TreeInfo primaryTarget = null;
    private TreeInfo secondaryTarget = null;

    public Lumberjack(RobotController rc) throws GameActionException {
        super(rc);
        if(Messenger.incrementLumberjacksCreatedCount(rc) == 1)
            System.out.println("I'm the first Lumberjack on my team!");
        pathHandler = new PathPlanner2(rc);
    }

    @Override
    public void runOneTurn() throws GameActionException {
        // defense
        huntEnemyRobots();
        if(rc.hasMoved() && rc.hasAttacked()) return;

        // setup phase
        TreeInfo nearbyTrees[] = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        if(primaryTarget == null) {
            // set target
            TreeInfo closestRobotTree = Messenger.getClosestTree(rc, rc.getLocation(), true);
            if(closestRobotTree != null) {
                primaryTarget = closestRobotTree;
                //rc.setIndicatorDot(primaryTarget.location, 255, 255, 255);
            }
            else {
                secondaryTarget = Utils.getLowestTree(nearbyTrees);
                if(secondaryTarget == null)  {
                    secondaryTarget = Messenger.getClosestTree(rc, rc.getLocation());
                    //rc.setIndicatorDot(secondaryTarget.location, 255, 255, 255);
                }
            }
        }

        // action phase
        tryChopTargetTree(primaryTarget);
        tryChopTargetTree(secondaryTarget);

        if(!rc.hasMoved() && !rc.hasAttacked()) {
            Direction randorection = Utils.randomDirection();
            if(rc.canMove(randorection))
                rc.move(randorection);
        }
    }

    private void tryChopTargetTree(TreeInfo tree) throws GameActionException {
        if(tree != null) {
            if(rc.canChop(tree.ID)) {
                rc.chop(tree.ID);
                Messenger.deleteTree(rc, tree.ID);
            }
            else {
                if(!rc.hasMoved())
                    pathHandler.moveNear(tree.location, tree.radius+.2f);
            }
        }
    }

    private void findNeutralTrees() throws GameActionException {

        TreeInfo[] nearbyNeutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        for(TreeInfo tree : nearbyNeutralTrees) {
            Messenger.recordNeutralTree(rc, tree);
        }

        TreeInfo lowestTree = Utils.getLowestTree(nearbyNeutralTrees);
        while(lowestTree != null) {

        }
    }

    private void huntEnemyRobots() throws GameActionException {
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

        if(robots.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
            rc.strike();
        } else {
            // No close robots, so search for robots within sight radius
            robots = rc.senseNearbyRobots(-1,enemy);

            // If there is a robot, move towards it
            if(robots.length > 0) {
                MapLocation myLocation = rc.getLocation();
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = myLocation.directionTo(enemyLocation);

                tryMove(toEnemy);
            } /*else {
                // Move Randomly
                tryMove(Utils.randomDirection());
            }*/
        }
    }
}
