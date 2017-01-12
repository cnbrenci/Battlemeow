package MeowPlayer;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Gardener extends Robot{

    public Gardener(RobotController rc) {
        super(rc);
        System.out.println("I'm a gardener!");
    }

    @Override
    public void runOneTurn() throws GameActionException {
            // Listen for home archon's location
            int xPos = rc.readBroadcast(0);
            int yPos = rc.readBroadcast(1);
            MapLocation archonLoc = new MapLocation(xPos,yPos);

            // Generate a random direction
            Direction dir = Utils.randomDirection();

            if(rc.canPlantTree(dir))
                rc.plantTree(dir);

            TreeInfo trees[] = rc.senseNearbyTrees();
            for(TreeInfo tree : trees)
            {
                if(rc.canWater(tree.location) && tree.team == rc.getTeam()) {
                    rc.water(tree.location);
                }
            }

            // Randomly attempt to build a soldier or lumberjack in this direction
            /*if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
                rc.buildRobot(RobotType.LUMBERJACK, dir);
            }*/

            // Move randomly
            tryMove(Utils.randomDirection());
    }
}
