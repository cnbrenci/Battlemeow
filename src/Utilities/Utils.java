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
}