package MeowPlayer;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Archon extends Robot{

    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        if(Messenger.incrementArchonsCreatedCount(rc) == 1)
            System.out.println("I'm the first Archon on my team!");
    }

    @Override
    public void runOneTurn() throws GameActionException {
        // Generate a random direction
        Direction dir = Utils.randomDirection();

        // Randomly attempt to build a gardener in this direction
        if (rc.canHireGardener(dir)) {
            rc.hireGardener(dir);
            rc.disintegrate();
        }

        // Broadcast archon's location for other robots on the team to know
        MapLocation myLocation = rc.getLocation();
        rc.broadcast(0,(int)myLocation.x);
        rc.broadcast(1,(int)myLocation.y);
    }
}
