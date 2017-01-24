package MeowMovement;

import battlecode.common.*;
public class PotentialMove {
    private RobotController rc;

    private MapLocation potentialMoveLocation;
    private float distanceToDestination;
    private boolean isOpen;

    public PotentialMove(RobotController rc, MapLocation locationToCheck, MapLocation destination) {

        this.rc = rc;
        potentialMoveLocation=locationToCheck;
        distanceToDestination=potentialMoveLocation.distanceTo(destination);
        isOpen=rc.canMove(locationToCheck);
    }
    public boolean isOpen() {
        return isOpen;
    }
    public float distanceToDestination() {
        return distanceToDestination;
    }
    public MapLocation getLocation() {
        return potentialMoveLocation;
    }
    public String toString() {
        return "Location: "+potentialMoveLocation+", Distance To Destination: "+distanceToDestination;
    }
}
