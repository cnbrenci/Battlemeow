package MeowPlayer;
import MeowMovement.Journey;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Gardener extends Robot{
    private Direction[] hexDir = new Direction[6];
    private float groveRadius = GameConstants.BULLET_TREE_RADIUS + rc.getType().bodyRadius + 2f;
    private final float lastResortGroveRadius = GameConstants.BULLET_TREE_RADIUS + rc.getType().bodyRadius;
    private MapLocation groveCenter = null;
    private MapLocation mamaArchonLoc;
    private Journey journeyTowardsGroveCenter = null;
    private Journey journeyAwayFromMamaArchon = null;
    Direction awayFromMamaArchon;
    private final float DISTANCE_PER_JOURNEY = 10f;
    private int journeysToFindGrove = 0;

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

        mamaArchonLoc = Utils.findNearbyFriendlyArchon_OrNull(rc).location;
        awayFromMamaArchon = mamaArchonLoc.directionTo(rc.getLocation());
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
                    if(journeyTowardsGroveCenter == null)
                        journeyTowardsGroveCenter = new Journey(rc, groveCenter);
                    journeyTowardsGroveCenter.moveTowardsDestinationAndDontStopBelievin();
                }
            }
        }
        else {
            // todo: check for neutral trees nearby so we can decide whether we need some lumberjacks

            if(!findGoodGroveSpot()) {
                if(journeyAwayFromMamaArchon == null || journeyAwayFromMamaArchon.haveReachedDestination()) {
                    journeyAwayFromMamaArchon = new Journey(rc, rc.getLocation().add(awayFromMamaArchon, DISTANCE_PER_JOURNEY));
                    if(++journeysToFindGrove>5) groveRadius = lastResortGroveRadius;
                }

                journeyAwayFromMamaArchon.moveTowardsDestinationAndDontStopBelievin();
            }
        }

        if(rc.canBuildRobot(RobotType.SCOUT, Direction.getEast()) && shouldBuildScout()) {
            rc.buildRobot(RobotType.SCOUT, Direction.getEast());
        }
    }

    private boolean shouldBuildScout() throws GameActionException {
        int numScoutsCreated = Messenger.getScoutsCreatedCount(rc);
        if(numScoutsCreated > 3 && rc.getTeamBullets() < 180) {
            return false;
        }
        if(numScoutsCreated > 16 && rc.getRobotCount() > 12) {
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
