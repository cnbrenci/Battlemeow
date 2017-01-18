package MeowPlayer;
import MeowMovement.Journey;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Archon extends Robot{

    private int numGardenersHired = 0;
    private int numGardenersAttemptedToHire = 0;
    private Direction awayFromCenter;
    private Journey neverWalkAway;
    private boolean isBobTheBuilder;

    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        awayFromCenter = Utils.getMapMidpoint(rc).directionTo(rc.getLocation());
        if(Messenger.incrementArchonsCreatedCount(rc) == 1) {
            System.out.println("I'm the first Archon on my team!");
            isBobTheBuilder = true;
        }

        neverWalkAway = new Journey(rc, rc.getLocation().add(awayFromCenter, 20));
    }

    @Override
    public void runOneTurn() throws GameActionException {
        // Try to walk away from the map midpoint
        neverWalkAway.moveTowardsDestinationAndDontStopBelievin();
        if(neverWalkAway.haveReachedDestination())
            neverWalkAway = new Journey(rc, rc.getLocation().add(awayFromCenter, 10));

        // Attempt to build a gardener in 1 of 3 angles towards the middle
        if(shouldHireGardener()) {
            Direction gardenerHireDirection = getGardenerBuildDirection();
            if (rc.canHireGardener(gardenerHireDirection)) {
                rc.hireGardener(gardenerHireDirection);
                numGardenersHired++;
            }
            numGardenersAttemptedToHire++;
        }

        //tryMove(Utils.randomDirection());
        // Broadcast archon's location for other robots on the team to know
        //MapLocation myLocation = rc.getLocation();
        //rc.broadcast(0,(int)myLocation.x);
        //rc.broadcast(1,(int)myLocation.y);
    }

    private boolean shouldHireGardener() throws GameActionException {
        if(rc.getTreeCount() >= 30) {
            return false;
        }

        // if there are 5 gardeners nearby, they're probably stuck and it's not worth it to make more
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        int gardenerCnt = 0;
        for(RobotInfo robot : robots) {
            if(robot.getType() == RobotType.GARDENER)
                gardenerCnt++;
        }
        if(gardenerCnt >= 5) return false;

        if(isBobTheBuilder) {
            if(numGardenersHired == 0) return true;
        }

        if(Messenger.getScoutsCreatedCount(rc) <= 2 && rc.getTeamBullets() < 240) return false;

        int numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
        if(((double)numGardenersHired / (double)Messenger.getGardenersCreatedCount(rc) <= 1. / numArchons))
            return true;
        return false;
    }

    private Direction getGardenerBuildDirection()
    {
        int dirCounter = numGardenersAttemptedToHire % 3;
        Direction angleToCenter = awayFromCenter.opposite();
        int nearest45 = 45*(Math.round((int)angleToCenter.getAngleDegrees()/45));
        switch(dirCounter){
            case 0:
                return new Direction((float) Math.toRadians((double)nearest45-30));
            case 1:
                return new Direction((float) Math.toRadians((double)nearest45+30));
            default:
                return new Direction((float) Math.toRadians((double)nearest45));
        }

    }
}
