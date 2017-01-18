package PathingTest;
import Utilities.Utils;
import battlecode.common.*;

/**
 * Created by Cassi on 1/10/2017.
 */
public class Archon extends Robot{

    private int numGardenersHired = 0;

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
        if (rc.canHireGardener(dir) && numGardenersHired==0 ) {
            rc.hireGardener(dir);
            numGardenersHired++;
        }
        rc.disintegrate();

        //tryMove(Utils.randomDirection());
        // Broadcast archon's location for other robots on the team to know
        //MapLocation myLocation = rc.getLocation();
        //rc.broadcast(0,(int)myLocation.x);
        //rc.broadcast(1,(int)myLocation.y);
    }

    private boolean shouldHireGardener() throws GameActionException {
        int treeCount = rc.getTreeCount();
        if(Messenger.getGardenersCreatedCount(rc) == 1 && treeCount < 2 && Messenger.getScoutsCreatedCount(rc) < 1) {
            return false;
        }

        if(treeCount >= 40) {
            return false;
        }

        return true;
    }
}
