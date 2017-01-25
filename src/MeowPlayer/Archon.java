package MeowPlayer;
import MeowMovement.*;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Archon extends Robot{

    private int numGardenersHired = 0;
    private Direction awayFromCenter;
    private PathPlanner2 pathHandler;
    private boolean isBobTheBuilder;

    private PathPlanner2 test;

    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        awayFromCenter = Utils.getMapMidpoint(rc).directionTo(rc.getLocation());
        if(Messenger.incrementArchonsCreatedCount(rc) == 1) {
            System.out.println("I'm the first Archon on my team!");
            isBobTheBuilder = true;
        }
        //pathHandler = new PathPlanner2(rc);
    }

    @Override
    public void runOneTurn() throws GameActionException {
        // Try to walk away from the map midpoint
        //pathHandler.move(rc.getLocation().add(awayFromCenter, rc.getType().strideRadius));
        if(rc.canMove(awayFromCenter, rc.getType().strideRadius))
            rc.move(awayFromCenter, rc.getType().strideRadius);

        if(shouldHireGardener()) {
            Direction buildDir = getGardenerBuildDirection();
            if(rc.canHireGardener(buildDir))
                rc.hireGardener(buildDir);
        }
    }

    private boolean canSeeAtLeastOneGardener() {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for(RobotInfo robot : robots) {
            if(robot.type == RobotType.GARDENER) return true;
        }
        return false;
    }

    private boolean shouldHireGardener() throws GameActionException {
        if((rc.getTreeCount() / (Messenger.getGardenersCreatedCount(rc)+1)) > 6 && canSeeAtLeastOneGardener()) {
            return false;
        }

        if(isBobTheBuilder) {
            if(numGardenersHired == 0) return true;
        }

        int numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
        if(((double)numGardenersHired / (double)Messenger.getGardenersCreatedCount(rc) <= 1. / numArchons))
            return true;
        return false;
    }

    private Direction getGardenerBuildDirection()
    {
        Direction angleToCenter = awayFromCenter.opposite();
        if(rc.canHireGardener(angleToCenter)) return angleToCenter;
        if(rc.canHireGardener(angleToCenter.rotateLeftDegrees(15f))) return angleToCenter.rotateLeftDegrees(15f);
        if(rc.canHireGardener(angleToCenter.rotateRightDegrees(15f))) return angleToCenter.rotateRightDegrees(15f);

        for (float deg = 1; deg < 360; deg+=5) {
            if(rc.canHireGardener(angleToCenter.rotateLeftDegrees(deg))) return angleToCenter.rotateLeftDegrees(deg);
        }
        return angleToCenter;
    }
}
