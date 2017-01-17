package MeowPlayer;

import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Robot {
    protected RobotController rc;
    protected Team enemy;

    protected Robot(RobotController rc){
        this.rc = rc;
        enemy = rc.getTeam().opponent();
        System.out.println("Spawning " + rc.getType().name() + "!");
    }

    public void run()  {
        while(true){
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try{
                // if we have enough bullets to win, donate them.
                if(haveEnoughBulletsToWinByDonation()) {
                    rc.donate(rc.getTeamBullets());
                }

                // The code you want your robot to perform every round should be in this loop
                runOneTurn();

                tryShakeTrees();

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                //System.out.println("Ending turn with " + Clock.getBytecodesLeft() + " bytecodes remaining.");
                Clock.yield();
            }
            catch(Exception e) {
                System.out.println("Unexpected Robot Exception");
                e.printStackTrace();
            }

        }

    }

    public void runOneTurn() throws UnsupportedOperationException, GameActionException {
        throw new UnsupportedOperationException("I am a plain old robot with no purpose in life. I cannot run :'(");
    }

    private boolean haveEnoughBulletsToWinByDonation()
    {
        return (GameConstants.VICTORY_POINTS_TO_WIN - rc.getTeamVictoryPoints()) <= (rc.getTeamBullets() / GameConstants.BULLET_EXCHANGE_RATE);
    }

    private boolean tryShakeTrees() throws GameActionException {
        TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
        for(TreeInfo tree : nearbyTrees) {
            if(rc.canShake(tree.getID())) {
                rc.shake(tree.getID());
                return true;
            }
        }
        return false;
    }
    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    protected boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir, 20, 3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws battlecode.common.GameActionException
     */
    protected boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    protected boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    protected boolean isWithinMyStrideRadius(MapLocation loc)
    {
        return rc.getLocation().isWithinDistance(loc, rc.getType().strideRadius);
    }
}
