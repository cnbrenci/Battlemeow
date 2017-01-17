package MeowPlayer;
import Utilities.Utils;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.*;
import java.util.ArrayList;
/**
 * Created by Chris on 1/13/2017.
 */
public class ScoutHunter extends Robot{
    int timeRange=20;
    int distRange=30;

    boolean hasTarget=false;
    boolean hasDestination=false;
    boolean[] checkedArchonStartLocations;

    RobotInfo target;
    RobotInfo[] sensedEnemies;

    MapLocation destination;
    MapLocation[] enemyArchonStartingLocations;

    private ArrayList<EnemyMemory> gardenerList = new ArrayList<EnemyMemory>();
    private ArrayList<EnemyMemory> archonList = new ArrayList<EnemyMemory>();

    public ScoutHunter(RobotController rc) throws GameActionException {
        super(rc);
        if(Messenger.incrementScoutsCreatedCount(rc) == 1)
            System.out.println("I'm the first Scout on my team!");
        
        enemyArchonStartingLocations=rc.getInitialArchonLocations(rc.getTeam().opponent());

        checkedArchonStartLocations=new boolean[enemyArchonStartingLocations.length];
        for(int i=0; i<checkedArchonStartLocations.length; i++)
            checkedArchonStartLocations[i]=false;

    }

    @Override
    public void runOneTurn() throws GameActionException {
        System.out.println("Round "+rc.getRoundNum());
        //reset variables
        target=null;
        destination=null;

        //sense nearby enemies
        sensedEnemies = rc.senseNearbyRobots(-1,enemy);

        //update sensed map
        int enemyIndex=processEnemyInfo();

        for(EnemyMemory asdf : gardenerList)
        {
            System.out.println(asdf.unitIDNumber);
            System.out.println(asdf.unitIDNumber);
        }

        //if there are gardeners in the sight range, target weakest gardener
        if(enemyIndex>=0) {
            target = sensedEnemies[enemyIndex];
            destination=target.getLocation();
            System.out.println("Destination = gardener in sight");
        }

        //otherwise get destination from memory
        if ( destination==null) {
            destination = getGardenerFromMemory();
            if(destination != null)
                System.out.println("Destination = gardener from memory");
        }

        //note if within sight range of enemy starting locations
        for (int i=0;i<checkedArchonStartLocations.length; i++)
            if (rc.getLocation().distanceTo(enemyArchonStartingLocations[i]) < 10)
                checkedArchonStartLocations[i] = true;

        //check unchecked archon starting locations
        if ( destination==null) {
            destination = getStartingLocationToCheck();
            if(destination != null)
                System.out.println("Destination = enemy archon spawn location");
        }

        //otherwise head to location of last seen Archon
        if (destination==null) {
            destination = getArchonFromMemory();
            if(destination != null)
                System.out.println("Destination = archon from memory");
        }

        //patrol
        if (destination==null) {
            destination = rc.getLocation().add(Utils.randomDirection(), 10);
            if(destination != null)
                System.out.println("Destination = random patrol");
        }

        //move towards destination
        if ( destination != null )
            headToward(destination);

        //try to fire
        if ( target!= null)
            tryToFire();

    }

    public void tryToFire() throws GameActionException {
        if (rc.canFireSingleShot() && rc.getLocation().distanceTo(target.getLocation())<rc.getType().bodyRadius+target.getType().bodyRadius+rc.getType().bulletSpeed)
        {
            rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
        }
    }

    public MapLocation getStartingLocationToCheck()
    {
        MapLocation closestLocation=null;
        float distToClosestLocation=1000;
        for(int i=0; i<enemyArchonStartingLocations.length; i++)
        {

            if (!checkedArchonStartLocations[i] );
            {
                float distanceToStartLocation=rc.getLocation().distanceTo(enemyArchonStartingLocations[i]);
                if (distanceToStartLocation<distToClosestLocation) {
                    //System.out.println(enemyArchonStartingLocations[i] + "  Checked? " + checkedArchonStartLocations[i] + "  Distance: " + rc.getLocation().distanceTo(enemyArchonStartingLocations[i]));
                    closestLocation = enemyArchonStartingLocations[i];
                    distToClosestLocation = rc.getLocation().distanceTo(enemyArchonStartingLocations[i]);
                }
            }
        }
        return closestLocation;
    }

    public MapLocation getGardenerFromMemory()
    {
        for (EnemyMemory rememberedRobot : gardenerList) {
            if (rc.getRoundNum()-rememberedRobot.turnSeen<timeRange)
            {
                if (rc.getLocation().distanceTo(rememberedRobot.lastLocation)<distRange)
                {
                    return rememberedRobot.lastLocation;
                }
            }
        }
        return null;
    }

    public MapLocation getArchonFromMemory()
    {
        MapLocation archonLastSeen=null;
        int roundLastSeen=0;
        for (EnemyMemory rememberedRobot : archonList) {
            if (rememberedRobot.turnSeen>roundLastSeen)
            {
                archonLastSeen=rememberedRobot.lastLocation;
                roundLastSeen=rememberedRobot.turnSeen;
            }
        }
        return archonLastSeen;
    }

    public void headToward(MapLocation destination) throws GameActionException {
        //move as close as can to target
        if (!rc.hasMoved())
        {
            if(rc.getLocation().distanceTo(destination)<rc.getType().strideRadius)
            {
                //for (float stridesize = rc.getType().strideRadius; stridesize > 0; stridesize = stridesize - (float) 0.1) {
                //    if (rc.canMove(rc.getLocation().directionTo(target.getLocation()), stridesize)) {
                //        rc.move(rc.getLocation().directionTo(destination), stridesize);
                //    }
                //}
            }
            else
            {
                tryMove(rc.getLocation().directionTo(destination), 10, 9);
            }
        }
    }

    public int processEnemyInfo()
    {
        int weakestGardenerIndex=-1;
        int currentIndex=0;
        float weakestGardenerHealth=1000;
        boolean alreadySeen;

        if(sensedEnemies.length>0) {
            for (RobotInfo sensedRobot : sensedEnemies) {
                if (sensedRobot.type.equals(RobotType.GARDENER)) {
                    if(sensedRobot.health<weakestGardenerHealth) {
                        weakestGardenerIndex = currentIndex;
                        weakestGardenerHealth=(float)sensedRobot.health;
                    }
                    alreadySeen = false;
                    for (EnemyMemory rememberedRobot : gardenerList) {
                        if (sensedRobot.getID()-rememberedRobot.unitIDNumber==0)
                        {
                            rememberedRobot.updateMemory(sensedRobot, rc.getRoundNum());
                            alreadySeen = true;
                            break;
                        }
                    }
                    if (!alreadySeen)
                        gardenerList.add(new EnemyMemory(sensedRobot, rc.getRoundNum()));

                }
                if (sensedRobot.type.equals(RobotType.ARCHON)) {
                    alreadySeen=false;
                    for (EnemyMemory rememberedRobot : archonList) {
                        if (sensedRobot.getID() == rememberedRobot.unitIDNumber)
                        {
                            rememberedRobot.updateMemory(sensedRobot,rc.getRoundNum());
                            alreadySeen=true;
                            break;
                        }
                    }
                    if (!alreadySeen)
                        archonList.add(new EnemyMemory(sensedRobot,rc.getRoundNum()));
                }
                currentIndex++;
            }
        }
        return weakestGardenerIndex;
    }
}
