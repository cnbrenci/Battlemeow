package PathingTest;
import MeowMovement.Journey;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Gardener extends Robot{
    private Direction[] hexDir = new Direction[6];
    private boolean[] treePlanted;
    private final float groveRadius = GameConstants.BULLET_TREE_RADIUS + rc.getType().bodyRadius;
    private MapLocation groveCenter = null;
    //private MapLocation destination = new MapLocation(424,400);
    private MapLocation destination = new MapLocation(355,275);
    private Journey journey = new Journey(rc,destination);

    public Gardener(RobotController rc) throws GameActionException {
        super(rc);
        if(Messenger.incrementGardenersCreatedCount(rc) == 1)
            System.out.println("I'm the first Gardener on my team!");

        hexDir[0]=Direction.getEast();
        hexDir[1]= hexDir[0].rotateLeftDegrees(60);
        hexDir[2]= hexDir[0].rotateLeftDegrees(120);
        hexDir[3]= hexDir[0].rotateLeftDegrees(180);
        hexDir[4]= hexDir[0].rotateLeftDegrees(240);
        hexDir[5]= hexDir[0].rotateLeftDegrees(300);
    }

    @Override
    public void runOneTurn() throws GameActionException {
        journey.moveTowardsDestinationAndDontStopBelievin();
    }

    private boolean shouldBuildScout() throws GameActionException {
        if(Messenger.getScoutsCreatedCount(rc) > 10 && rc.getRobotCount() > 12) {
            return false;
        }

        return true;
    }

    private void waterTrees() throws GameActionException {
        //1.1 = radius of gardener (1) + 0.1
        TreeInfo treeList[] = rc.senseNearbyTrees(1.1f, rc.getTeam());
        if ( treeList.length > 0 ) {
            TreeInfo lowestTree = Utils.getLowestTree(treeList);
            if(rc.canWater(lowestTree.location)) {
                rc.water(lowestTree.location);
                //System.out.println("Watering Tree ID: "+lowestTree.getID());
            }
        }
    }

    private void buildHex(int openingIndex) throws GameActionException {
        for(int i=0;i<=5;i++) {
            if ( rc.canPlantTree(hexDir[i]) && i!=openingIndex && rc.getTeamBullets()>GameConstants.BULLET_TREE_COST ) {
                rc.plantTree(hexDir[i]);
            }
        }
    }

    private boolean checkHexClear() {
        if ( rc.canPlantTree(hexDir[0]) && rc.canPlantTree(hexDir[1]) && rc.canPlantTree(hexDir[2]) && rc.canPlantTree(hexDir[3]) && rc.canPlantTree(hexDir[4]) && rc.canPlantTree(hexDir[5]) )
            return true;
        else
            return false;
    }

    private boolean findGoodGroveSpot() throws GameActionException {
        float furthestDist = rc.getType().sensorRadius - groveRadius - 0.1f;
        MapLocation proposedGroveCenter;
        for(float dist = furthestDist; dist >= 1.0f; dist-- )
        {
            for(int i = 0; i<6; i++)
            {
                proposedGroveCenter = rc.getLocation().add(hexDir[i], dist);
                if(!rc.isCircleOccupiedExceptByThisRobot(proposedGroveCenter, groveRadius))
                {
                    System.out.println("found groveCenter " + proposedGroveCenter.toString());
                    groveCenter = proposedGroveCenter;
                    rc.setIndicatorDot(proposedGroveCenter, 0, 0, 0);
                    return true;
                }
            }
        }
        // todo: Could use the difficulty to find a grove spot combined with # of trees as an indicator that we need some lumberjacks to cut down trees
        return false;
    }
}