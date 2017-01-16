package MeowPlayer;
import battlecode.common.*;

/**
 * Created by Chris on 1/13/2017.
 */
public class Scout extends Robot{
    private double minX, minY, maxX, maxY;
    MapLocation[] ourArchonStartingLocations, enemyArchonStartingLocations;
    Direction [] ordinalDirections={Direction.getEast(),Direction.getNorth(),Direction.getWest(),Direction.getSouth()};
    MapLocation midpoint,corner1,corner2;
    boolean hasFoundSide =false, hasFoundCorner =false, hasFoundMapSize=false;
    Direction currentDirection, firstDirection, secondDirection;

    public Scout(RobotController rc) throws GameActionException {
        super(rc);
        firstDirection = getFirstDirection();
        currentDirection = firstDirection;
        if(Messenger.incrementScoutsCreatedCount(rc) == 1)
            System.out.println("I'm the first Scout on my team!");
    }

    @Override
    public void runOneTurn() throws GameActionException {
        setMapDimensions();
        System.out.println("First Direction: "+firstDirection+", Second Direction: "+secondDirection);
        if ( rc.getRoundNum()>200)
            rc.disintegrate();
    }

    private void setMapDimensions() throws GameActionException
    {
        if ( !hasFoundMapSize )
        {
            boolean hitSomething = travelTowardEdge( currentDirection );
            if ( hitSomething )
            {
                int edgeCount=0,sideHit=0;
                boolean[] atEdge=checkForMapEdges();
                for(int edgeToCheck=0;edgeToCheck<4;edgeToCheck++)
                {
                    if(atEdge[edgeToCheck])
                    {
                        edgeCount++;
                        sideHit=edgeToCheck;
                    }
                }
                if ( edgeCount==0 )
                {
                    //in case hit a unit rather than a wall
                    boolean[] collision=checkForCollisions();
                    int collisionCount=0;
                    for (int dirToCheck=0;dirToCheck<4;dirToCheck++)
                    {
                        if ( collision[dirToCheck])
                        {
                            collisionCount++;
                            sideHit=dirToCheck;
                        }
                    }
                }
                if(secondDirection==null)
                {
                    secondDirection = getSecondDirection(firstDirection, ordinalDirections[sideHit]);
                }
                if ( edgeCount<2)
                {
                    if ( currentDirection.equals(firstDirection))
                        currentDirection=secondDirection;
                    else
                        currentDirection=firstDirection;
                }
                if ( edgeCount==2)
                {
                    hasFoundCorner = true;
                    if(corner1==null)
                    {
                        corner1 = rc.getLocation();
                        firstDirection=firstDirection.opposite();
                        //secondDirection=secondDirection.opposite();
                        currentDirection=firstDirection;
                    }
                    else {
                        corner2 = rc.getLocation();
                        hasFoundMapSize=true;
                        calculateMapDimensions();
                    }
                    //currentDirection = firstDirection.opposite();
                }
            }
        }
    }

    private void calculateMapDimensions()
    {
        float x1,x2,y1,y2;
        x1=corner1.x;
        y1=corner1.y;
        x2=midpoint.x-(corner1.x-midpoint.x);
        y2=midpoint.y-(corner1.y-midpoint.y);
        minX=x1<x2?x1:x2;
        minY=y1<y2?y1:y2;
        maxX=x1>x2?x1:x2;
        maxY=y1>y2?y1:y2;
        minX-=rc.getType().bodyRadius;
        minY-=rc.getType().bodyRadius;
        maxX+=rc.getType().bodyRadius;
        maxY+=rc.getType().bodyRadius;
        System.out.println("Bottom Left: "+minX+","+minY);
        System.out.println("Top Right: "+maxX+","+maxY);
    }

    private boolean travelTowardEdge(Direction travelDirection) throws GameActionException {
        //returns true if at edge of map
        if (!rc.hasMoved() )
        {
            if (rc.canMove(travelDirection))
            {
                //attempt full stride in travel direction
                rc.move(travelDirection);
            }
            else
            {
                //attempt lesser strides in travel direction
                for (float stridesize = rc.getType().strideRadius; stridesize > 0; stridesize = stridesize - (float) 0.1)
                {
                    if (rc.canMove(travelDirection, stridesize))
                    {
                        rc.move(travelDirection, stridesize);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Direction getSecondDirection(Direction initialDirection,Direction sideHit)
    {
        Direction dirToCheck;
        for(int dirIndex=0;dirIndex<ordinalDirections.length;dirIndex++)
        {
            dirToCheck=ordinalDirections[dirIndex];
            if (dirToCheck != sideHit && dirToCheck.degreesBetween(initialDirection)<=90)
            {
                return dirToCheck;
            }
        }
        throw new IllegalStateException("getSecondDirection - no suitable direction found");
    }

    private boolean[] checkForMapEdges() throws GameActionException {
        boolean[] atEdge={false,false,false,false};
        MapLocation checkLocation;
        for(int edgeToCheck=0;edgeToCheck<ordinalDirections.length;edgeToCheck++)
        {
            checkLocation = new MapLocation(rc.getLocation().x,rc.getLocation().y).add(ordinalDirections[edgeToCheck],rc.getType().bodyRadius+(float)0.1);
            if ( !rc.onTheMap(checkLocation) )
            {
                atEdge[edgeToCheck]=true;
            }
        }
        return atEdge;
    }

    private boolean[] checkForCollisions() throws GameActionException {
        boolean[] collision={false,false,false,false};
        for(int edgeToCheck=0;edgeToCheck<ordinalDirections.length;edgeToCheck++)
        {
            if ( !rc.canMove(ordinalDirections[edgeToCheck],(float)0.1) )
            {
                collision[edgeToCheck]=true;
            }
        }
        return collision;
    }

    private Direction getFirstDirection()
    {
        ourArchonStartingLocations=rc.getInitialArchonLocations(rc.getTeam());
        enemyArchonStartingLocations=rc.getInitialArchonLocations(rc.getTeam().opponent());

        //average starting locations for each team, get single location to compare between teams
        float enemyTeamXAvg=0,enemyTeamYAvg=0,ourTeamXAvg=0,ourTeamYAvg=0;
        for(MapLocation startingLocation : ourArchonStartingLocations)
        {
            System.out.println("Our Archon Starting Locations: "+startingLocation.x+","+startingLocation.y);
            ourTeamXAvg+=startingLocation.x;
            ourTeamYAvg+=startingLocation.y;
        }
        ourTeamXAvg/=ourArchonStartingLocations.length;
        ourTeamYAvg/=ourArchonStartingLocations.length;
        for(MapLocation startingLocation : enemyArchonStartingLocations)
        {
            System.out.println("Enemy Archon Starting Locations: "+startingLocation.x+","+startingLocation.y);
            enemyTeamXAvg+=startingLocation.x;
            enemyTeamYAvg+=startingLocation.y;
        }
        enemyTeamXAvg/=enemyArchonStartingLocations.length;
        enemyTeamYAvg/=enemyArchonStartingLocations.length;
        midpoint=new MapLocation((ourTeamXAvg+enemyTeamXAvg)/2,(ourTeamYAvg+enemyTeamYAvg)/2);
        System.out.println("Map Midpoint: "+midpoint.x+","+midpoint.y);
        //head in direction from middle to our average archon (away from enemy)
        //Direction initialDirection=new Direction(ourTeamXAvg-midpoint.x,ourTeamYAvg-midpoint.y);
        //head in direction from middle to our starting location
        Direction initialDirection=new Direction(rc.getLocation().x-midpoint.x,rc.getLocation().y-midpoint.y);
        return initialDirection;
    }
}
