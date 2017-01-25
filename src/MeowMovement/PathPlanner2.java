package MeowMovement;

import battlecode.common.*;
import Utilities.*;

import javax.management.monitor.GaugeMonitor;

public class PathPlanner2 {
    private RobotController rc;

    boolean backtracking=false;
    boolean stuck=false;
    boolean done=false;

    MapLocation currentLocation;
    MapLocation lastLocation;
    MapLocation nextLocation;
    MapLocation destination;

    Direction directionToDestination;

    int stuckCounter=0;
    int degreesOfMotion=16;

    float angleDelta;
    float stepSize;

    PotentialMove[] potentialLocations;

    public PathPlanner2(RobotController rc) {
        this.rc = rc;
        this.stepSize=rc.getType().strideRadius;
        this.angleDelta=360f/(float)degreesOfMotion;
        this.currentLocation=rc.getLocation();
        this.lastLocation=rc.getLocation();
    }

    private void backtrack() throws GameActionException {
        //initially checks 90 deg left and right
        //if finds nothing, checks more
        //int anglesToCheck=(degreesOfMotion)/4+stuckCounter;
        int anglesToCheck=3;
        for(int i=-anglesToCheck;i<=anglesToCheck;i++) {
            Direction testDirection=lastLocation.directionTo(currentLocation).rotateLeftDegrees(-1f*angleDelta*(float)i);
            if (rc.canMove(testDirection,stepSize)) {
                nextLocation=currentLocation.add(testDirection,stepSize);
                break;
            }
        }
        if(nextLocation==null) {
            //didn't find any potential moves (completely encircled)
            stuckCounter++;
            if ( stuckCounter==degreesOfMotion)
                stuck=true;
            rc.disintegrate();
        }
    }

    private boolean isFree() {
        boolean isFree=false;
        if(Math.abs(currentLocation.directionTo(nextLocation).degreesBetween(directionToDestination))<angleDelta)
        {
            isFree=true;
        }
        return isFree;
    }

    public boolean moveNear(MapLocation destination, float distance) throws GameActionException {
        MapLocation tempDestination=null;

        tempDestination=destination.add(destination.directionTo(rc.getLocation()),distance);
        //if(rc.getLocation().distanceTo(destination)<rc.getType().strideRadius+rc.getType().bodyRadius) {
        //    tempDestination = destination;//destination.add(destination.directionTo(currentLocation), distance);
        //}
        //else
        //{
        //    tempDestination = destination;
       // }
        boolean done = move(tempDestination);
        //rc.setIndicatorDot(tempDestination,0,0,0);
        return done;
    }

    public boolean move(MapLocation destination) throws GameActionException {
        BytecodeCounter moveBytecode = new BytecodeCounter();
        this.destination=destination;
        if(!stuck) {
            currentLocation = rc.getLocation();
            nextLocation = null;
            directionToDestination = currentLocation.directionTo(destination);

            if (!backtracking) {
                if(currentLocation.distanceTo(destination)>rc.getType().strideRadius) {
                    int closestDistanceIndex = getPossibleLocations(rc.getType().strideRadius);
                    if (closestDistanceIndex > -1) {
                        nextLocation = potentialLocations[closestDistanceIndex].getLocation();
                        //rc.setIndicatorDot(nextLocation,255,255,255);
                        if (potentialLocations[closestDistanceIndex].distanceToDestination() > currentLocation.distanceTo(destination)) {
                            backtracking = true;
                        }
                    }
                }
                else
                {
                    nextLocation=currentLocation.add(directionToDestination,currentLocation.distanceTo(destination));
                }
            } else {
                backtrack();
                if (nextLocation!=null && isFree()) {
                    backtracking = false;
                    stuckCounter = 0;
                }
            }

            if (nextLocation!=null && !rc.hasMoved() && rc.canMove(nextLocation)) {
                rc.move(nextLocation);
                lastLocation = currentLocation;
            }
        }

        if(rc.getLocation().distanceTo(destination)<0.001)
            done=true;

        System.out.println("Stuck? "+stuck);
        System.out.println("Backtracking? "+backtracking);

        return done;
    }

    private int getPossibleLocations(float distanceFromCurrentLocation) throws GameActionException{
        //gets possible locations
        //returs index of location closest to destination
        float closestDistance=999f;
        int closestDistanceIndex=-1;
        potentialLocations=new PotentialMove[degreesOfMotion];
        for(int i=0;i<degreesOfMotion;i++) {
            MapLocation locationToCheck = currentLocation.add(Direction.getEast().rotateLeftDegrees((float)i*360f/(float)degreesOfMotion),distanceFromCurrentLocation);
            potentialLocations[i]=new PotentialMove(rc,locationToCheck,destination);
            float distanceToDestination=potentialLocations[i].distanceToDestination();
            if(potentialLocations[i].isOpen() && distanceToDestination<closestDistance ) {
                closestDistance=distanceToDestination;
                closestDistanceIndex=i;
            }
            //rc.setIndicatorDot(potentialLocations[i].getLocation(),0,0,0);
        }
        return closestDistanceIndex;
    }
}
