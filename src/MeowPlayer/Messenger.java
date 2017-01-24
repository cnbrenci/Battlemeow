package MeowPlayer;
import Utilities.SimpleDirection;
import battlecode.common.*;


/**
 * Created by Cassi on 1/13/2017.
 *
 * Neutral trees - channels 100 - 2000
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

        final private static int targetGardenerID=20;
        final private static int targetGardenerHealth=21;
        final private static int targetGardenerX=22;
        final private static int targetGardenerY=23;
        final private static int targetGardenerTurnSeen=24;

        final private static int NEUTRAL_TREE_LIST_LENGTH=100;
        final private static int NEUTRAL_TREE_LIST_START=NEUTRAL_TREE_LIST_LENGTH+1;
        final private static int NEUTRAL_TREE_MAX_CHANNEL=2000;
    }

    final private static class Offsets {
        final private static int TREE_INDEX=7;
        final private static int TREE_ID=0;
        final private static int TREE_X=1;
        final private static int TREE_Y=2;
        final private static int TREE_NUMBULLETS=3;
        final private static int TREE_ROBOTTYPE=4;
        final private static int TREE_RADIUS=5;
        final private static int TREE_HEALTH=6;
    }


    private static int robotTypeToInt(RobotType robotType) {
        if(robotType == null) return 0;
        switch(robotType) {
            case ARCHON:
                return Channels.ARCHONS_CREATED;
            case GARDENER:
                return Channels.GARDENERS_CREATED;
            case LUMBERJACK:
                return Channels.LUMBERJACKS_CREATED;
            case SOLDIER:
                return Channels.SOLDIERS_CREATED;
            case TANK:
                return Channels.TANKS_CREATED;
            case SCOUT:
                return Channels.SCOUTS_CREATED;
            default:
                return 0;
        }
    }

    private static RobotType intToRobotType(int i) {
        switch(i) {
            case Channels.ARCHONS_CREATED:
                return RobotType.ARCHON;
            case Channels.GARDENERS_CREATED:
                return RobotType.GARDENER;
            case Channels.LUMBERJACKS_CREATED:
                return RobotType.LUMBERJACK;
            case Channels.SOLDIERS_CREATED:
                return RobotType.SOLDIER;
            case Channels.TANKS_CREATED:
                return RobotType.TANK;
            case Channels.SCOUTS_CREATED:
                return RobotType.SCOUT;
            default:
                return null;
        }
    }

    /*
    Adds the given tree to our list of neutral trees
    returns true if successfully added
    returns false if the list of trees has reached max length (meaning we can't add it)
     */
    public static boolean recordNeutralTree(RobotController rc, TreeInfo tree) throws GameActionException {
        int treeListLength = rc.readBroadcast(Channels.NEUTRAL_TREE_LIST_LENGTH);

        // check to see if we already have this tree
        if(treeListLength > 0) {
            for(int i = 0; i < treeListLength; i++) {
                int treeId = rc.readBroadcast(Channels.NEUTRAL_TREE_LIST_START + (Offsets.TREE_INDEX*i) + Offsets.TREE_ID);
                if(treeId == tree.ID) {
                    // we already have this tree! let's just update the values that could have changed.
                    rc.broadcast(Channels.NEUTRAL_TREE_LIST_START + (Offsets.TREE_INDEX*i) + Offsets.TREE_NUMBULLETS, tree.getContainedBullets());
                    rc.broadcast(Channels.NEUTRAL_TREE_LIST_START + (Offsets.TREE_INDEX*i) + Offsets.TREE_HEALTH, Float.floatToIntBits(tree.health));
                    return true;
                }
            }
        }
        // insert at end
        boolean success = copyTreeToIndex(rc, tree, treeListLength);
        if(success) rc.broadcast(Channels.NEUTRAL_TREE_LIST_LENGTH, treeListLength+1);
        return success;
    }

    private static boolean copyTreeToIndex(RobotController rc, TreeInfo tree, int index) throws GameActionException {
        int channelToInsertAt = Channels.NEUTRAL_TREE_LIST_START + (Offsets.TREE_INDEX*index);
        if(channelToInsertAt + Offsets.TREE_INDEX > Channels.NEUTRAL_TREE_MAX_CHANNEL) {
            return false;
        }

        rc.broadcast(channelToInsertAt + Offsets.TREE_ID, tree.ID);
        rc.broadcast(channelToInsertAt + Offsets.TREE_X, Float.floatToIntBits(tree.getLocation().x));
        rc.broadcast(channelToInsertAt + Offsets.TREE_Y, Float.floatToIntBits(tree.getLocation().y));
        rc.broadcast(channelToInsertAt + Offsets.TREE_NUMBULLETS, tree.getContainedBullets());
        rc.broadcast(channelToInsertAt + Offsets.TREE_ROBOTTYPE, robotTypeToInt(tree.containedRobot));
        rc.broadcast(channelToInsertAt + Offsets.TREE_RADIUS, Float.floatToIntBits(tree.radius));
        rc.broadcast(channelToInsertAt + Offsets.TREE_HEALTH, Float.floatToIntBits(tree.health));
        return true;
    }

    public static TreeInfo getClosestTree(RobotController rc, MapLocation loc) throws GameActionException {
        return getClosestTree(rc, loc, false);
    }

    public static TreeInfo getClosestTree(RobotController rc, MapLocation loc, boolean mustContainRobot) throws GameActionException {
        int treeListLength = rc.readBroadcast(Channels.NEUTRAL_TREE_LIST_LENGTH);

        float minDist = 1000;
        int minDistIndex = 0;
        MapLocation minDistLoc = null;
        // find the tree!!
        if(treeListLength > 0) {
            for(int i = 0; i < treeListLength; i++) {
                if(mustContainRobot) {
                    RobotType robotContained = intToRobotType(rc.readBroadcast(treeIndexChannel(i) + Offsets.TREE_ROBOTTYPE));
                    if(robotContained == null) continue;
                }

                MapLocation treeLoc = getLocFromTreeAtIndex(rc, i);
                float distance = loc.distanceTo(treeLoc);
                if(distance < minDist) {
                    minDist = distance;
                    minDistIndex = i;
                    minDistLoc = treeLoc;
                }
            }
            // get the tree!!
            if(minDistLoc != null) return getTreeAtIndex(rc, minDistIndex, minDistLoc);
        }

        return null;
    }

    public static TreeInfo getTreeAtIndex(RobotController rc, int index) throws GameActionException {
        return getTreeAtIndex(rc, index, null);
    }

    private static TreeInfo getTreeAtIndex(RobotController rc, int index, MapLocation loc) throws GameActionException {
        int id = rc.readBroadcast(treeIndexChannel(index) + Offsets.TREE_ID);
        int numBullets = rc.readBroadcast(treeIndexChannel(index) + Offsets.TREE_NUMBULLETS);
        RobotType robotType = intToRobotType(rc.readBroadcast(treeIndexChannel(index) + Offsets.TREE_ROBOTTYPE));
        float radius = Float.intBitsToFloat(rc.readBroadcast(treeIndexChannel(index) + Offsets.TREE_RADIUS));
        float health = Float.intBitsToFloat(rc.readBroadcast(treeIndexChannel(index) + Offsets.TREE_HEALTH));
        if(loc == null) loc = getLocFromTreeAtIndex(rc, index);
        return new TreeInfo(id, Team.NEUTRAL, loc, radius, health, numBullets, robotType);
    }

    private static int treeIndexChannel(int i) {
        return Channels.NEUTRAL_TREE_LIST_START + (Offsets.TREE_INDEX*i);
    }

    private static MapLocation getLocFromTreeAtIndex(RobotController rc, int index) throws GameActionException {
        float treeX = Float.intBitsToFloat(rc.readBroadcast(treeIndexChannel(index) + Offsets.TREE_X));
        float treeY = Float.intBitsToFloat(rc.readBroadcast(treeIndexChannel(index) + Offsets.TREE_Y));
        return new MapLocation(treeX, treeY);
    }

    public static boolean deleteTree(RobotController rc, int id) throws GameActionException {
        int treeListLength = rc.readBroadcast(Channels.NEUTRAL_TREE_LIST_LENGTH);

        if(treeListLength <= 0) return false;
        // find the tree!!
        for (int i = 0; i < treeListLength; i++) {
            if (rc.readBroadcast(treeIndexChannel(i) + Offsets.TREE_ID) == id) {
                if (i != treeListLength - 1) {
                    // this item isn't the last one in the list, so let's
                    // copy the last item in the list into the place of deleted tree
                    TreeInfo lastTree = getTreeAtIndex(rc, treeListLength - 1, null);
                    if (!copyTreeToIndex(rc, lastTree, i)) {
                        System.out.println("Failed to delete tree with id: " + id);
                        return false;
                    }
                }

                // delete the last item in the list
                rc.broadcast(Channels.NEUTRAL_TREE_LIST_LENGTH, treeListLength - 1);
                return true;
            }
        }
        return false;
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

    public static int getGardenersCreatedCount(RobotController rc) throws GameActionException {
        return rc.readBroadcast(Channels.GARDENERS_CREATED);
    }

    public static int getScoutsCreatedCount(RobotController rc) throws GameActionException {
        return rc.readBroadcast(Channels.SCOUTS_CREATED);
    }

    public static int getTargetGardenerID(RobotController rc) throws GameActionException {
        return rc.readBroadcast(Channels.targetGardenerID);
    }
    public static int getTargetGardenerHealth(RobotController rc) throws GameActionException {
        return rc.readBroadcast(Channels.targetGardenerHealth);
    }
    public static int getTargetGardenerX(RobotController rc) throws GameActionException {
        return rc.readBroadcast(Channels.targetGardenerX);
    }
    public static int getTargetGardenerY(RobotController rc) throws GameActionException {
        return rc.readBroadcast(Channels.targetGardenerY);
    }
    public static int getTargetGardenerTurnSeen(RobotController rc) throws GameActionException {
        return rc.readBroadcast(Channels.targetGardenerTurnSeen);
    }

    public static void setTargetGardenerID(RobotController rc, int ID) throws GameActionException {
        rc.broadcast(Channels.targetGardenerID,ID);
    }
    public static void setTargetGardenerHealth(RobotController rc,int health) throws GameActionException {
        rc.broadcast(Channels.targetGardenerHealth,health);
    }
    public static void setTargetGardenerX(RobotController rc,int x) throws GameActionException {
        rc.broadcast(Channels.targetGardenerX,x);
    }
    public static void setTargetGardenerY(RobotController rc,int y) throws GameActionException {
        rc.broadcast(Channels.targetGardenerY,y);
    }
    public static void setTargetGardenerTurnSeen(RobotController rc,int turn) throws GameActionException {
        rc.broadcast(Channels.targetGardenerTurnSeen,turn);
    }

    // This is how to convert float to int and back without losing data, if we want to store accurate x,y coordinates.
    public static void testStuff(){
        float f = 12.5f;
        int tointbits = Float.floatToIntBits(f);
        int torawinbits = Float.floatToRawIntBits(f);
        float back = Float.intBitsToFloat(tointbits);
        float rawback = Float.intBitsToFloat(torawinbits);
        System.out.println("float = " + f);
        System.out.println("tointbits = " + tointbits);
        System.out.println("torawintbits = " + torawinbits);
        System.out.println("back = " + back);
        System.out.println("rawback = " + rawback);
        /*
            float = 12.5
            tointbits = 1095237632
            torawintbits = 1095237632
            back = 12.5
            rawback = 12.5
         */
    }



}
