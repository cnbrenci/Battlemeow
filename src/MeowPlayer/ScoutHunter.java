package MeowPlayer;
import MeowMovement.Journey;
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

    boolean killedTarget=false;
    boolean[] checkedArchonStartLocations;

    RobotInfo target;
    RobotInfo[] sensedEnemies,sensedAllies;

    MapLocation destination;
    MapLocation deadUnitLocation;
    MapLocation[] enemyArchonStartingLocations;

    private Journey journey;

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
        //if had target, check if it died last round
        if(target!=null)
        {
            try {
                if(rc.getLocation().distanceTo(target.getLocation())<8) {
                    rc.senseRobot(target.getID());
                    killedTarget=false;
                }

            }
            catch(Exception E) {
                killedTarget=true;
                //if target was broadcast target, reset broadcast
                if(Messenger.getTargetGardenerID(rc)==target.getID()) {
                    Messenger.setTargetGardenerID(rc, 0);
                }
                deadUnitLocation=target.location;
                //if senseRobot throws an exception, robot is likely dead
                //assuming it didn't move out of your sight range
                if(target.getType().equals(RobotType.GARDENER))
                   delete(gardenerList,target.getID());
                if(target.getType().equals(RobotType.ARCHON))
                    delete(archonList,target.getID());
            }
        }

        //broadcast checks
        if(Messenger.getTargetGardenerHealth(rc)==0)
            Messenger.setTargetGardenerID(rc,0);
        if(rc.getRoundNum()-Messenger.getTargetGardenerTurnSeen(rc)>3)
            Messenger.setTargetGardenerID(rc,0);
        if(Messenger.getTargetGardenerID(rc)>0)
        {
            if(rc.getLocation().distanceTo(new MapLocation(Messenger.getTargetGardenerX(rc),Messenger.getTargetGardenerY(rc)))<rc.getType().sensorRadius) {
                if(!rc.canSenseRobot(Messenger.getTargetGardenerID(rc))) {
                    Messenger.setTargetGardenerID(rc,0);
                }
            }
        }


        //reset variables
        target=null;
        destination=null;

        //sense units
        sensedEnemies = rc.senseNearbyRobots(-1,enemy);
        sensedAllies = rc.senseNearbyRobots(-1,rc.getTeam());

        //update sensed map
        int enemyIndex=processEnemyInfo();

        //if called for help, go help
        //to do

        //if there are gardeners in the sight range, target weakest gardener
        //else, target the weakest non-gardener non-archon
        //else, target the weakest archon
        if(enemyIndex>=0) {
            target = sensedEnemies[enemyIndex];
            if(target.getType().equals(RobotType.GARDENER)) {
                if (Messenger.getTargetGardenerID(rc) == 0 || target.getHealth() < Messenger.getTargetGardenerHealth(rc)) {
                    Messenger.setTargetGardenerID(rc, (int) target.getID());
                    Messenger.setTargetGardenerHealth(rc, (int) target.getHealth());
                    Messenger.setTargetGardenerX(rc, (int) target.getLocation().x);
                    Messenger.setTargetGardenerY(rc, (int) target.getLocation().y);
                    Messenger.setTargetGardenerTurnSeen(rc, rc.getRoundNum());
                }
            }
            destination=target.getLocation();
            System.out.println("Destination = gardener in sight");
        }

        //otherwise, go to broadcast location
        if ( destination==null) {
            if (Messenger.getTargetGardenerID(rc)>0)
            {
                destination=new MapLocation(Messenger.getTargetGardenerX(rc),Messenger.getTargetGardenerY(rc));
            }
            if (destination != null)
                System.out.println("Destination = gardener from broadcast");

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

        //spread out by heading away from allied units in range
        if (destination==null) {
            //get center of allied mass
            MapLocation centerOfMass=alliedCenterOfMass();
            if (rc.getLocation().distanceTo(centerOfMass)>0.5)
                destination=rc.getLocation().add(centerOfMass.directionTo(rc.getLocation()),rc.getType().strideRadius);
            else
                destination=null;
            if(destination != null)
                System.out.println("Destination = spread out");
        }

        //patrol
        if (destination==null) {
            destination = rc.getLocation().add(Utils.randomDirection(), 10);
            if(destination != null)
                System.out.println("Destination = random patrol");
        }

        //move towards destination
        if ( destination != null ) {
            headToward(destination);
            //journey = new Journey(rc,destination);
            //journey.moveTowardsDestinationAndDontStopBelievin();
        }

        //try to fire
        if ( target!= null)
            tryToFire();

        System.out.println();
        System.out.println("Target ID From Broadcast: "+Messenger.getTargetGardenerID(rc));
        System.out.println("Target Health From Broadcast: "+Messenger.getTargetGardenerHealth(rc));
        System.out.println("Target X From Broadcast: "+Messenger.getTargetGardenerX(rc));
        System.out.println("Target Y From Broadcast: "+Messenger.getTargetGardenerY(rc));
        System.out.println("Target Turn Seen From Broadcast: "+Messenger.getTargetGardenerTurnSeen(rc));

    }

    public MapLocation alliedCenterOfMass()
    {
        float avgx=0f;
        float avgy=0f;
        for(RobotInfo ally : sensedAllies)
        {
            avgx+=ally.getLocation().x;
            avgy+=ally.getLocation().y;
        }
        avgx+=rc.getLocation().x;
        avgy+=rc.getLocation().y;
        avgx/=(sensedAllies.length+1);
        avgy/=(sensedAllies.length+1);
        return new MapLocation(avgx,avgy);
    }

    public void tryToFire() throws GameActionException {
        if (rc.canFireSingleShot() && rc.getLocation().distanceTo(target.getLocation())<rc.getType().bodyRadius+target.getType().bodyRadius+rc.getType().bulletSpeed)
        {
            rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
        }
    }

    public void delete(ArrayList<EnemyMemory> list, int unitID)
    {
        for (EnemyMemory rememberedRobot : list) {
            if (rememberedRobot.unitIDNumber==unitID)
            {
                list.remove(rememberedRobot);
                break;
            }
        }
    }

    public MapLocation getStartingLocationToCheck()
    {
        MapLocation closestLocation=null;
        float distToClosestLocation=1000;
        for(int i=0; i<enemyArchonStartingLocations.length; i++)
        {

            if (!checkedArchonStartLocations[i] )
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
        int gardenerCount=0;
        int weakestGardenerIndex=-1;
        float weakestGardenerHealth=1000;

        int archonCount=0;
        int weakestArchonIndex=-1;
        float weakestArchonHealth=1000;

        int otherCount=0;
        int otherIndex=-1;
        float otherHealth=1000;

        int currentIndex=0;
        boolean alreadySeen;

        if(sensedEnemies.length>0) {
            for (RobotInfo sensedRobot : sensedEnemies) {
                if (sensedRobot.type.equals(RobotType.GARDENER)) {
                    gardenerCount++;
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
                    archonCount++;
                    if(sensedRobot.health<weakestArchonHealth) {
                        weakestArchonIndex=currentIndex;
                        weakestArchonHealth=(float)sensedRobot.health;
                    }
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
                if (!sensedRobot.type.equals(RobotType.GARDENER)&&!sensedRobot.type.equals(RobotType.ARCHON))
                {
                    otherCount++;
                    if(sensedRobot.health<otherHealth) {
                        otherIndex=currentIndex;
                        otherHealth=(float)sensedRobot.health;
                    }
                }
                currentIndex++;
            }
        }
        if(gardenerCount>0)
            return weakestGardenerIndex;
        else
            if(otherCount>0)
                return otherIndex;
            else
                if(archonCount>0 && rc.getRoundNum()>200)
                    return weakestArchonIndex;
                else
                    return -1;

    }
}
