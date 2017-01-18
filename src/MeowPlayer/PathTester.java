package MeowPlayer;

        import battlecode.common.*;
        import java.util.*;

        import java.util.Collections;

/**
 * Created by Cassi on 1/10/2017.
 */
public class PathTester extends Robot {

    private ArrayList<MapLocation> blockedlist = new ArrayList<MapLocation>();
    private MapLocation previous = rc.getLocation();
    private int destination_reached = 0;
    public PathTester(RobotController rc) {
        super(rc);
    }

    @Override
    public void runOneTurn() throws GameActionException {
        if (destination_reached == 0) {
            // MapLocation targetpos = new MapLocation((float) 157, (float) 486); // movement_test2
            //MapLocation targetpos = new MapLocation((float) 424, (float) 400); // TreeMap
            MapLocation targetpos = new MapLocation((float) 253, (float) 311); // TreeMap2
            // create arrays of the MapLocation of all adjacent spaces and the distance from
            // those spaces to the end goal
            int degrees_of_motion = 16;
            MapLocation[] adjspaces = new MapLocation[degrees_of_motion];
            double[] dists = new double[adjspaces.length];
            double mindist = 999;
            int minloc = 0;
            int deadendcounter = 0;

            // loop through all the adjacent spaces and calculate their MapLocations
            for (int i = 0; i < adjspaces.length; i++) {
                //MapLocation tmploc = rc.getLocation().add((i) * (float) Math.PI / (degrees_of_motion / 2));
                MapLocation tmploc = rc.getLocation().add((i) * (float) Math.PI / (degrees_of_motion / 2),1);
                adjspaces[i] = tmploc;
                // if we can move there (ie: not blocked) and the location hasn't been added to the blockedlist
                if (rc.canMove(tmploc) && !blockedlist.contains(generalizedLocation(adjspaces[i]))) {
                    dists[i] = Math.sqrt(Math.pow(tmploc.x - targetpos.x, 2) + Math.pow(tmploc.y - targetpos.y, 2));
                    if (dists[i]<3.5) {
                        System.out.println("destination reached");
                        destination_reached = 1;
                        break;
                    }
                } else { // if we can't move to the space, give it a very large distance
                    dists[i] = 999;
                    deadendcounter += 1;
                }
                // keep track of the minimum distance of the adjacent spaces
                if (dists[i] < mindist) {
                    mindist = dists[i];
                    minloc = i;
                }
                System.out.println("dist " + i + ": " + dists[i]);
            }

            if (destination_reached==0) {
                // if we only have one direction to go (ie: only one dir that doesn't have 999 distance)
                if (deadendcounter == (adjspaces.length - 1)) {
                    // add the current loc to the blockedlist, to avoid being stuck in the dead end
                    blockedlist.add(generalizedLocation(rc.getLocation()));
                    previous = rc.getLocation();
                    rc.move(adjspaces[minloc]);
                } else if (dists[minloc] == 999) { // somehow everything is blocked or on blockedlist
                    System.out.println("Err: Nowhere to move");
                } else { // find the closest point and move to it
                    // if we're moving backwards add current loc to blocked list
                    if (previous.compareTo(adjspaces[minloc]) == 0) {
                        blockedlist.add(generalizedLocation(rc.getLocation()));
                    }
                    previous = rc.getLocation();
                    rc.move(adjspaces[minloc]);

                }
            }
        }

        System.out.println(rc.getLocation());
        generalizedLocation(rc.getLocation());
        if(rc.getRoundNum() > 1000) {
            rc.disintegrate();
        }


    }

    public MapLocation generalizedLocation(MapLocation exactLocation)
    {
        float roundTo=0.2f;
        float generalx=roundTo*Math.round(exactLocation.x/roundTo);
        float generaly=roundTo*Math.round(exactLocation.y/roundTo);
        MapLocation generalLocation = new MapLocation(generalx,generaly);

        System.out.println("Exact: "+exactLocation);
        System.out.println("General: "+generalLocation);

        return generalLocation;

    }

}
