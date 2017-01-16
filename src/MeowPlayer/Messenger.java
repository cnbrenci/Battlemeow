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

    private static void InitOncePerGame(RobotController rc) throws GameActionException {
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
    public static void recordMapEdge(RobotController rc, MapLocation loc, SimpleDirection dir) throws GameActionException {
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


    public static int incrementArchonsCreatedCount(RobotController rc) throws GameActionException {
        int currentValue = rc.readBroadcast(Channels.ARCHONS_CREATED);
        rc.broadcast(Channels.ARCHONS_CREATED, ++currentValue);
        if(currentValue == 1) // this is our first Archon i.e. first unit in the game
            InitOncePerGame(rc);
        return currentValue;
    }

    public static int incrementGardenersCreatedCount(RobotController rc) throws GameActionException {
        return incrementCreatedCount(rc, Channels.GARDENERS_CREATED);

    }

    public static int incrementLumberjacksCreatedCount(RobotController rc) throws GameActionException {
        return incrementCreatedCount(rc, Channels.LUMBERJACKS_CREATED);
    }

    public static int incrementSoldiersCreatedCount(RobotController rc) throws GameActionException {
        return incrementCreatedCount(rc, Channels.SOLDIERS_CREATED);
    }

    public static int incrementTanksCreatedCount(RobotController rc) throws GameActionException {
        return incrementCreatedCount(rc, Channels.TANKS_CREATED);
    }

    public static int incrementScoutsCreatedCount(RobotController rc) throws GameActionException {
        return incrementCreatedCount(rc, Channels.SCOUTS_CREATED);
    }

    private static int incrementCreatedCount(RobotController rc, int channel) throws GameActionException {
        int currentValue = rc.readBroadcast(channel);
        rc.broadcast(channel, ++currentValue);
        return currentValue;
    }
}
