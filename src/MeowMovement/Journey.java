package MeowMovement;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;

/**
 * Created by Cassi on 1/16/2017.
 *
 * Ported Taylor's code into this class to be used by any robot.
 * If the robot needs pathfinding, give it a Journey field in the class and instantiate
 * with your destination, then call moveTowardsDestinationAndDontStopBelievin each turn
 * till you reach your destination.
 */
public class Journey {
    private boolean destinationReached = false;
    private MapLocation previous;
    private MapLocation destination;

    private RobotController rc;
    private int degreesOfMotion = 16;

    // idea: instead of only keeping the previous deadend location, why not keep all the deadend locations?
    // You know you've already explored the only option in that location... so it's essentially blocked too.
    private ArrayList<MapLocation> blockedlist = new ArrayList<MapLocation>();

    // todo: move to PotentialMove class and make an array of that instead
    private MapLocation[] adjacentSpaces;
    private double distances[];


    public Journey(RobotController rc, MapLocation dest) {
        this(rc, dest, 16);
    }

    public Journey(RobotController rc, MapLocation dest, int degOfMotion) {
        this.rc = rc;
        previous = rc.getLocation();
        destination = dest;
        degreesOfMotion = degOfMotion;

        adjacentSpaces = new MapLocation[degOfMotion];
    }

    public boolean haveReachedDestination() { return destinationReached;}
    public boolean haveStoppedBelievin() {return false; }

    /*
        returns true if we reached the destination, false otherwise
     */
    public boolean moveTowardsDestinationAndDontStopBelievin() throws GameActionException {
        if(destinationReached)
            return true;
        // MapLocation targetpos = new MapLocation((float) 157, (float) 486); // movement_test2
        //MapLocation targetpos = new MapLocation((float) 424, (float) 400); // TreeMap
        //MapLocation targetpos = new MapLocation((float) 253, (float) 311); // TreeMap2
        // create arrays of the MapLocation of all adjacent spaces and the distance from
        // those spaces to the end goal
        distances = new double[adjacentSpaces.length];
        double mindist = 999;
        int minloc = 0;
        int deadendcounter = 0;
        // loop through all the adjacent spaces and calculate their MapLocations
        for (int i = 0; i < adjacentSpaces.length; i++) {
            //MapLocation tmploc = rc.getLocation().add((i) * (float) Math.PI / (degrees_of_motion / 2));
            MapLocation tmploc = rc.getLocation().add((i) * (float) Math.PI / (degreesOfMotion / 2), rc.getType().strideRadius);
            adjacentSpaces[i] = tmploc;
            // if we can move there (ie: not blocked) and the location hasn't been added to the blockedlist
            if (rc.canMove(tmploc) && !blockedlist.contains(adjacentSpaces[i])) {
                distances[i] = Math.sqrt(Math.pow(tmploc.x - destination.x, 2) + Math.pow(tmploc.y - destination.y, 2));
                if (distances[i] < 3.5) {
                    System.out.println("destination reached");
                    destinationReached = true;
                    return destinationReached;
                }
            } else { // if we can't move to the space, give it a very large distance
                distances[i] = 999;
                deadendcounter += 1;
            }
            // keep track of the minimum distance of the adjacent spaces
            if (distances[i] < mindist) {
                mindist = distances[i];
                minloc = i;
            }
            //System.out.println("dist " + i + ": " + distances[i]);
        }


        // if we only have one direction to go (ie: only one dir that doesn't have 999 distance)
        if (deadendcounter == (adjacentSpaces.length - 1)) {
            // add the current loc to the blockedlist, to avoid being stuck in the dead end
            blockedlist.add(rc.getLocation());
            previous = rc.getLocation();
            rc.move(adjacentSpaces[minloc]);
        }
        else if (distances[minloc] == 999) { // somehow everything is blocked or on blockedlist
            System.out.println("Err: Nowhere to move");
        }
        else { // find the closest point and move to it
            // if we're moving backwards add current loc to blocked list
            if (previous.compareTo(adjacentSpaces[minloc]) == 0) {
                blockedlist.add(rc.getLocation());
            }
            previous = rc.getLocation();
            rc.move(adjacentSpaces[minloc]);
        }
        return destinationReached;
    }
}

