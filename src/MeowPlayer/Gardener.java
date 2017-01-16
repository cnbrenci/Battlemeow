package MeowPlayer;
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
        if(groveCenter != null)
        {
            if(rc.getLocation().isWithinDistance(groveCenter, 0.01f))
            {
                // build trees and water
                buildHex(0);
                waterTrees();
            }
            else
            {
                // walk towards the grove!
                if(rc.canMove(groveCenter)){
                    rc.move(groveCenter);
                }
                else {
                    tryMove(rc.getLocation().directionTo(groveCenter));
                }
            }
        }
        else {
            if(!findGoodGroveSpot()) {
                tryMove(Utils.randomDirection());
            }
        }

        if(rc.getTeamBullets()>80 && rc.canBuildRobot(RobotType.SCOUT, Direction.getEast())) {
            rc.buildRobot(RobotType.SCOUT, Direction.getEast());
        }
    }

    private void waterTrees() throws GameActionException {
        //1.1 = radius of gardener (1) + 0.1
        TreeInfo treeList[] = rc.senseNearbyTrees((float)1.1,rc.getTeam());
        System.out.println(treeList.length);
        if ( treeList.length>0 ) {
            TreeInfo lowestTree = treeList[0];
            for(TreeInfo tree : treeList) {
                if ( tree.getHealth()<lowestTree.getHealth() ) {
                    lowestTree=tree;
                }
            }
            if(rc.canWater(lowestTree.location)) {
                rc.water(lowestTree.location);
                System.out.println("Watering Tree ID: "+lowestTree.getID());
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
        return false;
    }
}
