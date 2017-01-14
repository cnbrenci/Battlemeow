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
    private RobotController rc;
    private static int CHANNEL_WEST_WALL = 10;
    private static int CHANNEL_NORTH_WALL = 10;
    private static int CHANNEL_EAST_WALL = 10;
    private static int CHANNEL_SOUTH_WALL = 10;

    public Messenger(RobotController rc)
    {
        this.rc = rc;
    }

    // todo: somewhere that is only run once per match, we need to set the array to all -1
    /*
     * index 10-19	Map corners
     * 10: x value for WEST wall of map
     * 12: y value of NORTH wall of map
     * 14: x value of EAST wall of map
     * 16: y value of SOUTH wall of map
     */
    public void recordMapCorner(MapLocation loc, SimpleDirection dir) throws GameActionException {
        switch(dir)
        {
            case North:
                rc.broadcast(CHANNEL_NORTH_WALL, Math.round(loc.x));
                break;
            case South:
                rc.broadcast(CHANNEL_SOUTH_WALL, Math.round(loc.x));
                break;
            case East:
                rc.broadcast(CHANNEL_EAST_WALL, Math.round(loc.y));
                break;
            case West:
                rc.broadcast(CHANNEL_WEST_WALL, Math.round(loc.y));
                break;
            case NorthEast:
                rc.broadcast(CHANNEL_NORTH_WALL, Math.round(loc.x));
                rc.broadcast(CHANNEL_EAST_WALL, Math.round(loc.y));
                break;
            case NorthWest:
                rc.broadcast(CHANNEL_NORTH_WALL, Math.round(loc.x));
                rc.broadcast(CHANNEL_WEST_WALL, Math.round(loc.y));
                break;
            case SouthEast:
                rc.broadcast(CHANNEL_EAST_WALL, Math.round(loc.y));
                rc.broadcast(CHANNEL_SOUTH_WALL, Math.round(loc.x));
                break;
            case SouthWest:
                rc.broadcast(CHANNEL_WEST_WALL, Math.round(loc.y));
                rc.broadcast(CHANNEL_SOUTH_WALL, Math.round(loc.x));
                break;
        }

    }
}
