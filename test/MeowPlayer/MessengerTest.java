package MeowPlayer;

import battlecode.common.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Cassi on 1/22/2017.
 */
public class MessengerTest {
    private RobotController rc;
    private TreeInfo tree1;
    private TreeInfo tree2;
    private TreeInfo tree3;
    private TreeInfo tree4;
    private TreeInfo tree5;


    @Before
    public void setUp() throws Exception {
        rc = new TestRobotController();
        tree1 = new TreeInfo(1, Team.NEUTRAL, new MapLocation(1, 1), 1f, 100f, 10, null );
        tree2 = new TreeInfo(2, Team.NEUTRAL, new MapLocation(2, 2), 2f, 200f, 20, null );
        tree3 = new TreeInfo(3, Team.NEUTRAL, new MapLocation(3, 3), 3f, 300f, 30, null );
        tree4 = new TreeInfo(2, Team.NEUTRAL, new MapLocation(2, 2), 3f, 150f, 0, null );
        tree5 = new TreeInfo(5, Team.NEUTRAL, new MapLocation(5, 5), 3f, 150f, 0, RobotType.LUMBERJACK );

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void recordNeutralTree() throws Exception {
        Messenger.recordNeutralTree(rc, tree1);
        Messenger.recordNeutralTree(rc, tree2);
        Messenger.recordNeutralTree(rc, tree3);
        Messenger.recordNeutralTree(rc, tree4);
        Messenger.recordNeutralTree(rc, tree5);

        assertEquals(tree1.ID, Messenger.getTreeAtIndex(rc, 0).ID);
        assertEquals(tree2.ID, Messenger.getTreeAtIndex(rc, 1).ID);
        assertTrue(tree4.health == Messenger.getTreeAtIndex(rc, 1).health);
        assertEquals(tree5.containedRobot, Messenger.getTreeAtIndex(rc, 3).containedRobot);

        boolean success = Messenger.deleteTree(rc, tree1.ID);
        assertTrue(success);
    }

    @Test
    public void getClosestTree() throws Exception {
        MapLocation myLoc = new MapLocation(6f, 6f);

        Messenger.recordNeutralTree(rc, tree1);
        assertEquals(tree1.ID, Messenger.getClosestTree(rc, myLoc).ID);
        assertTrue(Messenger.getClosestTree(rc, myLoc, true) == null);

        Messenger.recordNeutralTree(rc, tree2);
        assertEquals(tree2.ID, Messenger.getClosestTree(rc, myLoc).ID);
        assertTrue(Messenger.getClosestTree(rc, myLoc, true) == null);

        Messenger.recordNeutralTree(rc, tree3);
        assertEquals(tree3.ID, Messenger.getClosestTree(rc, myLoc).ID);
        assertTrue(Messenger.getClosestTree(rc, myLoc, true) == null);

        Messenger.recordNeutralTree(rc, tree4);
        assertEquals(tree3.ID, Messenger.getClosestTree(rc, myLoc).ID);
        assertTrue(Messenger.getClosestTree(rc, myLoc, true) == null);

        Messenger.recordNeutralTree(rc, tree5);
        assertEquals(tree5.ID, Messenger.getClosestTree(rc, myLoc).ID);
        assertEquals(tree5.ID, Messenger.getClosestTree(rc, myLoc, true).ID);

        Messenger.deleteTree(rc, tree5.ID);
        assertTrue(Messenger.getClosestTree(rc, myLoc, true) == null);

    }

    @Test
    public void getClosestTree1() throws Exception {

    }

}