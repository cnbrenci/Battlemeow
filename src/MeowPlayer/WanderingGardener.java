package MeowPlayer;
import MeowMovement.PathPlanner2;
import Utilities.Utils;
import battlecode.common.*;

public class WanderingGardener extends Robot{
    Messenger messageHandler=new Messenger();
    PathPlanner2 pathHandler;

    //MapLocation soldierNode=null;
    MapLocation soldierNode1=null,soldierNode2=null;

    MapLocation trackOpenNodeClosestToStart=null;

    MapLocation midpoint, currentLocation, initialTree;

    int treeCounter=0;
    float treeWaterThreshold=GameConstants.BULLET_TREE_MAX_HEALTH*0.8f;

    float buffer=0.2f;
    float spacing=RobotType.GARDENER.bodyRadius*2+GameConstants.BULLET_TREE_RADIUS*2+buffer;
    float buildOffset=RobotType.GARDENER.bodyRadius+GameConstants.BULLET_TREE_RADIUS+GameConstants.GENERAL_SPAWN_OFFSET;
    float delX=spacing/2f;
    float delY=delX*(float)Math.sqrt(3.0);
    float initialTreeX;
    float initialTreeY;
    float treeWaterDist=RobotType.GARDENER.bodyRadius+GameConstants.BULLET_TREE_RADIUS+0.2f;

    //boolean isFirstTree=true;

    public WanderingGardener(RobotController rc) throws GameActionException {
        super(rc);
        pathHandler=new PathPlanner2(rc);
        midpoint = Utils.getMapMidpoint(rc);
    }
    public void runOneTurn() throws GameActionException {
        currentLocation=rc.getLocation();
        buildBitches();
        doGardenerStuff();
    }

    public boolean shouldPlantTree() {
        boolean shouldPlant=false;
        if(treeCounter<1000) {
            shouldPlant=true;
        }
        return shouldPlant;
    }

    public void doGardenerStuff() throws GameActionException{
        MapLocation closestNode=null;
        MapLocation[] allNodes=null;

        int hasFirstTreeBeenPlanted=messageHandler.getHasFirstTreeBeenPlanted(rc);
        if(hasFirstTreeBeenPlanted==1 && initialTree==null) {
            initialTreeX = messageHandler.getTreeGridStartX(rc);
            initialTreeY = messageHandler.getTreeGridStartY(rc);
            initialTree = new MapLocation(initialTreeX, initialTreeY);
        }

        closestNode=getClosestNode();
        allNodes=getNearbyNodes(closestNode);

        TreeInfo[] allAlliedTrees=rc.senseNearbyTrees(rc.getType().sensorRadius,rc.getTeam());
        TreeInfo weakestTree=getLowestHealthTree(allAlliedTrees);




        /*
        if(initialTree!=null){
            rc.setIndicatorDot(initialTree,255,255,255);}
        */

        if(shouldPlantTree() && (weakestTree==null || weakestTree.health>treeWaterThreshold)){
            if(hasFirstTreeBeenPlanted==0) {
                plantFirstTree();
                treeCounter++;
            }
            else
            {
                findNodeClosestToStartingNode(allNodes);
                if(trackOpenNodeClosestToStart != null)
                {
                    //rc.setIndicatorDot(trackOpenNodeClosestToStart,0,0,0);
                    if(closeEnoughToPlant(trackOpenNodeClosestToStart)) {
                        boolean plantingWasSuccessful=tryToPlant(trackOpenNodeClosestToStart);
                        if(plantingWasSuccessful) {
                            trackOpenNodeClosestToStart=null;
                            treeCounter++;
                        }
                    } else {
                        rc.setIndicatorLine(rc.getLocation(),trackOpenNodeClosestToStart,0,255,0);
                        if(rc.canBuildRobot(RobotType.SOLDIER,rc.getLocation().directionTo(soldierNode1)) && rc.getTeamBullets()>250 ) {
                            rc.buildRobot(RobotType.SOLDIER,rc.getLocation().directionTo(soldierNode1));
                        }
                        else
                        {
                            if(rc.canBuildRobot(RobotType.SOLDIER,rc.getLocation().directionTo(soldierNode2)) && rc.getTeamBullets()>250) {
                                rc.buildRobot(RobotType.SOLDIER,rc.getLocation().directionTo(soldierNode2));
                            }
                        }
                        pathHandler.moveNear(trackOpenNodeClosestToStart,buildOffset);
                        //rc.setIndicatorDot(trackOpenNodeClosestToStart,0,255,0);
                    }
                    rc.setIndicatorDot(trackOpenNodeClosestToStart,0,255,0);
                }
            }
        } else {
            //move towards weakest tree
            if(weakestTree!=null) {
                rc.setIndicatorLine(rc.getLocation(),weakestTree.location,0,0,255);
                pathHandler.moveNear(weakestTree.location, treeWaterDist);
                rc.setIndicatorDot(weakestTree.location,0,0,255);
            }
        }
        waterWeakestTreeInRange(allAlliedTrees);

        //draw dots

        //all nodes
        for(MapLocation tempLoc : allNodes)
            rc.setIndicatorDot(tempLoc, 255, 0, 0);

        //closest node
        //rc.setIndicatorDot(closestNode,255,255,255);

        //tracked closest to start node
        // if(trackOpenNodeClosestToStart!=null) {
        //    rc.setIndicatorDot(trackOpenNodeClosestToStart,0,0,0); }
        //rc.setIndicatorDot(initialTree,0,0,255);
    }

    /*
    public void tryGardenerMove(MapLocation loc,float howClose) throws GameActionException {
        MapLocation tempDestination=loc.add(loc.directionTo(rc.getLocation()),howClose);
        Direction dir=rc.getLocation().directionTo(tempDestination);
        float distanceTo=rc.getLocation().distanceTo(tempDestination);

        if(distanceTo<rc.getType().strideRadius)

        float degreeOffset=20;
        int checksPerSide=3;
        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while (currentCheck <= checksPerSide) {
            // Try the offset of the left side
            if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
            }
            // Try the offset on the right side
            if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
    }
    */

    public void waterWeakestTreeInRange(TreeInfo[] allTrees) throws GameActionException {
        TreeInfo weakestNearbyTree=null;
        float weakestNearbyTreeHealth=99999f;
        for(int i=0;i<allTrees.length;i++) {
            int currentTreeID=allTrees[i].getID();
            float currentTreeHealth=allTrees[i].getHealth();
            if(rc.canInteractWithTree(currentTreeID) && rc.canWater(currentTreeID) && currentTreeHealth < weakestNearbyTreeHealth) {
                weakestNearbyTree=allTrees[i];
                weakestNearbyTreeHealth=currentTreeHealth;
            }
        }
        if(weakestNearbyTree!=null) {
            rc.water(weakestNearbyTree.getID());
            rc.setIndicatorDot(weakestNearbyTree.getLocation(),0,0,255);
        }
    }

    public TreeInfo getLowestHealthTree(TreeInfo[] allTrees) {
        TreeInfo lowestTree=null;
        float lowestTreeHealth=9999f;
        for(int i=0;i<allTrees.length;i++) {
            if(allTrees[i].health < lowestTreeHealth) {
                lowestTree=allTrees[i];
                lowestTreeHealth=lowestTree.health;
            }
        }
        return lowestTree;
    }

    public boolean tryToPlant(MapLocation nodeToPlantAt) throws GameActionException{
        boolean successfulPlant=false;
        //rc.setIndicatorDot(nodeToPlantAt,255,255,255);
        Direction directionToNode=rc.getLocation().directionTo(nodeToPlantAt);
        if(rc.canPlantTree(directionToNode)) {
            rc.plantTree(directionToNode);
        }
        if(rc.senseTreeAtLocation(nodeToPlantAt)!=null) {
            successfulPlant=true;
        }
        return successfulPlant;
    }

    public boolean closeEnoughToPlant(MapLocation nodeToPlantAt) throws GameActionException {
        boolean closeEnough=false;
        float distance=rc.getLocation().distanceTo(nodeToPlantAt);
        if(Math.abs(distance-buildOffset)<0.01) {
            closeEnough=true;
        }
        return closeEnough;
    }

    public void findNodeClosestToStartingNode(MapLocation[] nodesToCheck) throws GameActionException {
        MapLocation nextNode=null;
        float closestDistance=999f;
        for(int i=0;i<nodesToCheck.length;i++) {

            if(rc.canSenseLocation(nodesToCheck[i]) && rc.senseTreeAtLocation(nodesToCheck[i])==null && rc.senseRobotAtLocation(nodesToCheck[i])==null) {


                float distFromLocationToFirstTree=nodesToCheck[i].distanceTo(initialTree);
                if (distFromLocationToFirstTree<closestDistance) {
                    nextNode=nodesToCheck[i];
                    closestDistance=distFromLocationToFirstTree;
                }
            }
        }

        //NOTE: dont track, recheck each round
        trackOpenNodeClosestToStart=nextNode;
        /*
        if(trackOpenNodeClosestToStart==null) {
            trackOpenNodeClosestToStart=nextNode;
        } else {
            if(nextNode!=null) {
                if(nextNode.distanceTo(initialTree)<trackOpenNodeClosestToStart.distanceTo(initialTree)) {
                    trackOpenNodeClosestToStart=nextNode;
                }
            }
        }
        */

    }

    public MapLocation[] getNearbyNodes(MapLocation centerNode) {
        MapLocation[] nearbyNodes = new MapLocation[7];
        nearbyNodes[0]=centerNode;
        for(int i=1;i<nearbyNodes.length;i++) {
            nearbyNodes[i]=centerNode.add(Direction.getEast().rotateLeftDegrees(60f*(float)(i-1)),spacing);
        }
        return nearbyNodes;
    }

    public MapLocation getClosestNode() {
        MapLocation closestNode=null;
        float tempx=0,tempy=0;
        float deltaXToOtherNode=0,deltaYToOtherNode=0;
        if(currentLocation.x>=initialTreeX && currentLocation.y>=initialTreeY) {
            //robot northeast of initial tree, temp node=southwest of robot
            tempx=currentLocation.x-Math.abs(currentLocation.x-initialTreeX)%delX;
            tempy=currentLocation.y-Math.abs(currentLocation.y-initialTreeY)%delY;
            deltaXToOtherNode=delX;
            deltaYToOtherNode=delY;
        }
        if(currentLocation.x>=initialTreeX && currentLocation.y<=initialTreeY) {
            //robot southeast of initial tree, temp node=northwest of robot
            tempx=currentLocation.x-Math.abs(currentLocation.x-initialTreeX)%delX;
            tempy=currentLocation.y+Math.abs(currentLocation.y-initialTreeY)%delY;
            deltaXToOtherNode=delX;
            deltaYToOtherNode=-1f*delY;
        }
        if(currentLocation.x<=initialTreeX && currentLocation.y>=initialTreeY) {
            //robot northwest of initial tree, temp node=southeast of robot
            tempx=currentLocation.x+Math.abs(currentLocation.x-initialTreeX)%delX;
            tempy=currentLocation.y-Math.abs(currentLocation.y-initialTreeY)%delY;
            deltaXToOtherNode=-1f*delX;
            deltaYToOtherNode=delY;
        }
        if(currentLocation.x<=initialTreeX && currentLocation.y<=initialTreeY) {
            //robot southwest of initial tree, temp node=northeast of robot
            tempx=currentLocation.x+Math.abs(currentLocation.x-initialTreeX)%delX;
            tempy=currentLocation.y+Math.abs(currentLocation.y-initialTreeY)%delY;
            deltaXToOtherNode=-1f*delX;
            deltaYToOtherNode=-1f*delY;
        }
        float tolerance=0.05f;
        MapLocation tempNode=new MapLocation(tempx,tempy);
        float xcheck1=(Math.abs(tempx-initialTreeX)+tolerance/2f)%(2f*delX);
        float ycheck1=(Math.abs(tempy-initialTreeY)+tolerance/2f)%(2f*delY);
        //float xcheck2=(Math.abs(tempx-initialTreeX)-delX)%delX;
        //float ycheck2=(Math.abs(tempy-initialTreeY)-delY)%delY;

        System.out.println("Checks");
        System.out.println(xcheck1);
        System.out.println(ycheck1);


        MapLocation node1=null,node2=null;


        MapLocation tempNode1=new MapLocation(tempx,tempy);
        MapLocation tempNode2=new MapLocation(tempx+deltaXToOtherNode,tempy+deltaYToOtherNode);
        float xcheck2=(Math.abs(tempx+deltaXToOtherNode-initialTreeX)+tolerance/2f)%(2f*delX);
        float ycheck2=(Math.abs(tempy+deltaYToOtherNode-initialTreeY)+tolerance/2f)%(2f*delY);
        System.out.println(xcheck2);
        System.out.println(ycheck2);

        if((xcheck1 < tolerance && ycheck1 < tolerance) || (xcheck2<tolerance && ycheck2 < tolerance)) {
            //tempnode is a valid node, get other node
            node1=tempNode;
            node2=new MapLocation(tempx+deltaXToOtherNode,tempy+deltaYToOtherNode);

            //soldierNode=node1;
            soldierNode1=new MapLocation(tempx,tempy+deltaYToOtherNode);
            soldierNode2=new MapLocation(tempx+deltaXToOtherNode,tempy);
        }
        else
        {
            node1=new MapLocation(tempx,tempy+deltaYToOtherNode);
            node2=new MapLocation(tempx+deltaXToOtherNode,tempy);
            //soldierNode=node1;
            soldierNode1=tempNode;
            soldierNode2=new MapLocation(tempx+deltaXToOtherNode,tempy+deltaYToOtherNode);
        }
        rc.setIndicatorDot(node1.add(Direction.getEast(),0.2f),255,0,255);
        rc.setIndicatorDot(node2.add(Direction.getEast(),0.2f), 255,255,0);


        //float tempx=currentLocation.x-(currentLocation.x-initialTreeX)%delX;
        //float tempy=currentLocation.y-(currentLocation.y-initialTreeY)%delY;
        //MapLocation bottomLeft=new MapLocation(tempx,tempy);
        //System.out.println("InitialTreeXY: "+initialTreeX+","+initialTreeY);
        //rc.setIndicatorDot(bottomLeft,0,0,255);
        //float xcheck=Math.abs((tempx-initialTreeX)%(2f*delX));
        //float ycheck=Math.abs((tempy-initialTreeY)%(2f*delY));
        //System.out.println("Xcheck "+xcheck);
        //System.out.println("Ycheck "+ycheck);
        //System.out.println("delX "+delX);
        //System.out.println("delY "+delY);
        //MapLocation node1=null,node2=null;
        //float tolerance=0.05f;
        //if((xcheck<tolerance && ycheck<tolerance) || (Math.abs(xcheck-delX)<tolerance && Math.abs(ycheck-delY)<tolerance))
        //{
        //    node1=bottomLeft;
        //    node2=new MapLocation(tempx+delX,tempy+delY);
        //}
        //else
        //{
        //    node1=new MapLocation(tempx+delX,tempy);
        //    node2=new MapLocation(tempx,tempy+delY);
        //}
        //rc.setIndicatorDot(node1,255,0,0);
        //rc.setIndicatorDot(node2,0,255,0);

        float dist1=currentLocation.distanceTo(node1);
        float dist2=currentLocation.distanceTo(node2);
        if(dist1<dist2) {
            closestNode=node1;
        }
        else
        {
            closestNode=node2;
        }
        return closestNode;
    }

    public void plantFirstTree() throws GameActionException{
        Direction initMoveDir=rc.getLocation().directionTo(midpoint);
        if(rc.canMove(initMoveDir) && !rc.hasMoved()) {
            rc.move(initMoveDir);
        }
        for(int i=0;i<16;i++) {
            Direction directionToTry=rc.getLocation().directionTo(midpoint).rotateLeftDegrees(360f/16f*(float)i);
            boolean successfulFirstPlant = tryToPlant(rc.getLocation().add(directionToTry,buildOffset));
            System.out.println("Tried to plant: "+successfulFirstPlant);
            if(successfulFirstPlant) {
                initialTree = rc.getLocation().add(directionToTry,rc.getType().bodyRadius+GameConstants.BULLET_TREE_RADIUS+GameConstants.GENERAL_SPAWN_OFFSET);
                System.out.println("Init Tree: "+initialTree);
                messageHandler.setTreeGridStartX(rc,initialTree.x);
                messageHandler.setTreeGridStartY(rc,initialTree.y);
                initialTreeX=messageHandler.getTreeGridStartX(rc);
                initialTreeY=messageHandler.getTreeGridStartY(rc);
                System.out.println("Initial Tree: "+initialTreeX+","+initialTreeY);
                messageHandler.setHasFirstTreeBeenPlanted(rc,1);
                break;

            }
        }

        /*
        if(rc.senseNearbyTrees(rc.getType().sensorRadius,rc.getTeam()).length==0) {
            Direction treeDirection = rc.getLocation().directionTo(midpoint);
            if(rc.canPlantTree(treeDirection)) {
                rc.plantTree(treeDirection);
                initialTree = rc.getLocation().add(treeDirection,rc.getType().bodyRadius+GameConstants.BULLET_TREE_RADIUS+GameConstants.GENERAL_SPAWN_OFFSET);
                messageHandler.setTreeGridStartX(rc,initialTree.x);
                messageHandler.setTreeGridStartY(rc,initialTree.y);

                initialTreeX=messageHandler.getTreeGridStartX(rc);
                initialTreeY=messageHandler.getTreeGridStartY(rc);

            }
        }
        isFirstTree=false;
        */
    }

    private void buildBitches() throws GameActionException {
        if(Messenger.getScoutsCreatedCount(rc) < 1) {
            buildRobot(RobotType.SCOUT);
        }

        if(Messenger.getLumberjacksCreatedCount(rc) < 1) {
            buildRobot(RobotType.LUMBERJACK);
        }

        if(Messenger.getSoldiersCreatedCount(rc) < 1) {
            buildRobot(RobotType.SOLDIER);
        }
    }

    private Direction getRobotBuildDirection(RobotType robotType)
    {
        Direction angleToCenter = rc.getLocation().directionTo(Utils.getMapMidpoint(rc));
        if(rc.canBuildRobot(robotType, angleToCenter)) return angleToCenter;
        if(rc.canBuildRobot(robotType, angleToCenter.rotateLeftDegrees(15f))) return angleToCenter.rotateLeftDegrees(15f);
        if(rc.canBuildRobot(robotType, angleToCenter.rotateRightDegrees(15f))) return angleToCenter.rotateRightDegrees(15f);

        for (float deg = 1; deg < 360; deg+=5) {
            if(rc.canBuildRobot(robotType, angleToCenter.rotateLeftDegrees(deg))) return angleToCenter.rotateLeftDegrees(deg);
        }
        return angleToCenter;
    }

    private void buildRobot(RobotType robotType) throws GameActionException {
        Direction dir = getRobotBuildDirection(robotType);
        if(rc.canBuildRobot(robotType, dir))
            rc.buildRobot(robotType, dir);
    }

}