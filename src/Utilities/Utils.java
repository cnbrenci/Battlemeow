package Utilities;
import battlecode.common.*;

public class Utils {
    /**
     * Returns a random Direction
     *
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return new Direction((float) Math.random() * 2 * (float) Math.PI);
    }

    public static Direction getNorthEast()
    {
        return Direction.getNorth().rotateRightDegrees(45);
    }

    public static Direction getNorthWest()
    {
        return Direction.getNorth().rotateLeftDegrees(45);
    }

    public static Direction getSouthEast()
    {
        return Direction.getSouth().rotateLeftDegrees(45);
    }

    public static Direction getSouthWest()
    {
        return Direction.getSouth().rotateRightDegrees(45);
    }

    public static TreeInfo getLowestTree(TreeInfo[] trees) {
        if ( trees.length>0 ) {
            TreeInfo lowestTree = trees[0];
            for(TreeInfo tree : trees) {
                if ( tree.getHealth()<lowestTree.getHealth() ) {
                    lowestTree=tree;
                }
            }
            return lowestTree;
        }
        return null;
    }

    public static MapLocation getMapMidpoint(RobotController rc)
    {
        return getAverageLocation(new MapLocation[] {
                getAverageLocation(rc.getInitialArchonLocations(rc.getTeam())),
                getAverageLocation(rc.getInitialArchonLocations(rc.getTeam().opponent()))} );
    }

    public static MapLocation getAverageLocation(MapLocation[] locations)
    {
        float xSum = 0, ySum = 0;
        for(MapLocation loc : locations)
        {
            xSum += loc.x;
            ySum += loc.y;
        }
        return new MapLocation(xSum / locations.length, ySum / locations.length);
    }

    public static RobotInfo findNearbyFriendlyArchon_OrNull(RobotController rc) {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for(RobotInfo robot : robots) {
            if(robot.type == RobotType.ARCHON)
                return robot;
        }
        return null;
    }
}