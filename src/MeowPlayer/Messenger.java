package MeowPlayer;
import Utilities.SimpleDirection;
import Utilities.Utils;
import battlecode.common.*;

import static Utilities.SimpleDirection.*;

/**
 * Created by Cassi on 1/13/2017.
 *
 * Index	Use
 * 0-9	Global Commands
 * 10-19	Map corners
 * 20-29	Unit ID counters
 * ?
 * 100-199	Unit map
 * 200-299	Tree map
 * 300-349	Allied Trees
 * 350-399	Neutral Trees
 * 400-449	Enemy Trees
 * 450-499	Allied Gardener
 * 500-549	Allied Scout
 * 550-599	Allied Soldier
 * 600-649	Allied Lumberjack
 * 650-699	Enemy Gardener
 * 700-749	Enemy Scout
 * 750-799	Enemy Soldier
 * 800-849	Enemy Lumberjack
 * 850-900	Bullet info
 *
 * For non-tree units, things to record:
 * UnitID (15 bits)
 * X location (7 bits)
 * Y location (7 bits)
 * Round seen (? Bits) -approximate to reduce bits used
 * Health (? Bits) - approximate to reduce bits used.  Maybe track percent health instead
 *
 * For unit lists, first index is # of units
 * When something dies, overwrite that index with end of list and update first index
 * Maybe track unit ID?
 */
public class Messenger {

    final private static class Channels {
        final private static int WEST_WALL = 10;    // x coordinate of the west wall on the map
        final private static int NORTH_WALL = 11;   // y coordinate of the north wall on the map
        final private static int EAST_WALL = 12;    // x coordinate of the east wall on the map
        final private static int SOUTH_WALL = 13;   // y coordinate of the south wall on the map
        final private static int ARCHONS_CREATED = 14;      // *_CREATED are the number of each robot type created this game
        final private static int GARDENERS_CREATED = 15;
        final private static int LUMBERJACKS_CREATED = 16;
        final private static int SOLDIERS_CREATED = 17;
        final private static int SCOUTS_CREATED = 18;
        final private static int TANKS_CREATED = 19;
    }

    private RobotController rc;
    private static int createdOrder;

    public Messenger(RobotController rc) {
        this.rc = rc;
        try
        {
            this.createdOrder = newUnitCreated();
        }
        catch(Exception e)
        {
            System.out.println("Unexpected exception in newUnitCreated. " +
                    "We aren't going to have reliable createdOrder, but we don't want to crash the game so...  " +
                    "guess we'll just eat this exception and see what happens.");
            e.printStackTrace();
        }

    }

    private void InitOncePerGame() throws GameActionException {
        // whether they have been set yet
        rc.broadcast(Channels.NORTH_WALL, -1);
        rc.broadcast(Channels.EAST_WALL, -1);
        rc.broadcast(Channels.SOUTH_WALL, -1);
        rc.broadcast(Channels.WEST_WALL, -1);
    }

    /*
     * A robot who has found the edge or corner of the map should call this in order to
     * save that information into the team's shared consciousness.
     */
    public void recordMapEdge(MapLocation loc, SimpleDirection dir) throws GameActionException {
        switch(dir)
        {
            case North:
                rc.broadcast(Channels.NORTH_WALL, Math.round(loc.x));
                break;
            case South:
                rc.broadcast(Channels.SOUTH_WALL, Math.round(loc.x));
                break;
            case East:
                rc.broadcast(Channels.EAST_WALL, Math.round(loc.y));
                break;
            case West:
                rc.broadcast(Channels.WEST_WALL, Math.round(loc.y));
                break;
            case NorthEast:
                rc.broadcast(Channels.NORTH_WALL, Math.round(loc.x));
                rc.broadcast(Channels.EAST_WALL, Math.round(loc.y));
                break;
            case NorthWest:
                rc.broadcast(Channels.NORTH_WALL, Math.round(loc.x));
                rc.broadcast(Channels.WEST_WALL, Math.round(loc.y));
                break;
            case SouthEast:
                rc.broadcast(Channels.EAST_WALL, Math.round(loc.y));
                rc.broadcast(Channels.SOUTH_WALL, Math.round(loc.x));
                break;
            case SouthWest:
                rc.broadcast(Channels.WEST_WALL, Math.round(loc.y));
                rc.broadcast(Channels.SOUTH_WALL, Math.round(loc.x));
                break;
        }
    }

    /*
    Returns the build number for this robot -
    this number is essentially the count of how many robots of that type have been built
     */
    public int getRobotBuildNumber()
    {
        return createdOrder;
    }

    /*
     * Unit Type Build Counters
     *
     */
    private int newUnitCreated() throws GameActionException {
        int currentValue = 0;
        switch(rc.getType())
        {
            case ARCHON:
                currentValue = rc.readBroadcast(Channels.ARCHONS_CREATED);
                rc.broadcast(Channels.ARCHONS_CREATED, ++currentValue);
                if(currentValue == 1) // this is our first Archon i.e. first unit in the game
                    InitOncePerGame();
                break;
            case GARDENER:
                currentValue = rc.readBroadcast(Channels.GARDENERS_CREATED);
                rc.broadcast(Channels.GARDENERS_CREATED, ++currentValue);
                break;
            case LUMBERJACK:
                currentValue = rc.readBroadcast(Channels.LUMBERJACKS_CREATED);
                rc.broadcast(Channels.LUMBERJACKS_CREATED, ++currentValue);
                break;
            case SOLDIER:
                currentValue = rc.readBroadcast(Channels.SOLDIERS_CREATED);
                rc.broadcast(Channels.SOLDIERS_CREATED, ++currentValue);
                break;
            case TANK:
                currentValue = rc.readBroadcast(Channels.TANKS_CREATED);
                rc.broadcast(Channels.TANKS_CREATED, ++currentValue);
                break;
            case SCOUT:
                currentValue = rc.readBroadcast(Channels.SCOUTS_CREATED);
                rc.broadcast(Channels.SCOUTS_CREATED, ++currentValue);
                break;
            default:
                throw new UnsupportedOperationException("Robot Type: " + rc.getType());
        }

        return currentValue;
    }


}
