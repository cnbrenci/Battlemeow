package MeowPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.*;

/**
 * Created by Chris on 1/13/2017.
 */
public class ScoutHunter extends Robot{
    private double minX, minY, maxX, maxY;
    int targetID;
    float nextStride;
    MapLocation[] enemyArchonStartingLocations;
    Direction [] ordinalDirections={Direction.getEast(),Direction.getNorth(),Direction.getWest(),Direction.getSouth()};
    MapLocation targetLocation;
    boolean hasTarget=false;
    Direction targetDirection;

    public ScoutHunter(RobotController rc)
    {
        super(rc);
        enemyArchonStartingLocations=rc.getInitialArchonLocations(rc.getTeam().opponent());
        targetLocation=pickFirstTarget();
        targetDirection=new Direction (rc.getLocation(),targetLocation);
    }

    @Override
    public void runOneTurn() throws GameActionException {
        RobotInfo[] enemyInfo = rc.senseNearbyRobots(-1, enemy);


        //sense for gardeners
        hasTarget=false;
        for (RobotInfo enemyCheck : enemyInfo )
        {
            if (enemyCheck.type.equals(RobotType.GARDENER))
            {
                //set closest gardener as target
                targetLocation=enemyCheck.location;
                targetDirection=new Direction (rc.getLocation(),targetLocation);
                targetID=enemyCheck.ID;
                hasTarget=true;
                break;
            }
        }

        //move as close as can to target
        if (rc.canMove(targetDirection) )
        {
            if (!rc.hasMoved())
                rc.move(targetDirection,nextStride);
        }
        else
        {
            for (float stridesize = nextStride; stridesize > 0; stridesize = stridesize - (float) 0.1) {
                if (rc.canMove(targetDirection, stridesize) && !rc.hasMoved()) {
                    rc.move(targetDirection, stridesize);
                }
            }
        }
        //fire
        if (rc.canFireSingleShot()&&hasTarget)
        {
            rc.fireSingleShot(targetDirection);
            nextStride=(float)1.5;
        }
        else
        {
            nextStride=(float)2.5;
        }

        //if find gardener, taget closest
        //sense for archons
    }

    public MapLocation pickFirstTarget()
    {
        for (MapLocation test : enemyArchonStartingLocations)
            System.out.println(test+", "+test.distanceTo(rc.getLocation()));

        int numEnemyArchons=enemyArchonStartingLocations.length;
        float distanceToEnemy,closestEnemy;
        closestEnemy=enemyArchonStartingLocations[0].distanceTo(rc.getLocation());
        targetLocation=enemyArchonStartingLocations[0];
        if ( numEnemyArchons>1 )
        {
            for (int i = 1; i < numEnemyArchons; i++)
            {
                distanceToEnemy=enemyArchonStartingLocations[i].distanceTo(rc.getLocation());
                if(distanceToEnemy<closestEnemy)
                    targetLocation=enemyArchonStartingLocations[i];
            }
        }
        return targetLocation;
    }
    public void hunt() throws GameActionException{
        

    }


}
