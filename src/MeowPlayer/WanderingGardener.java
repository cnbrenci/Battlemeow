package MeowPlayer;
import MeowMovement.PathPlanner2;
import Utilities.Utils;
import battlecode.common.*;

public class WanderingGardener extends Robot{
    Messenger messageHandler=new Messenger();
    PathPlanner2 pathHandler;

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

    boolean isFirstTree=true;

    public WanderingGardener(RobotController rc) throws GameActionException {
        super(rc);
        pathHandler=new PathPlanner2(rc);
        midpoint = Utils.getMapMidpoint(rc);
    }
    public void runOneTurn() throws GameActionException {
        currentLocation=rc.getLocation();
        doGardenerStuff();
    }

    public boolean shouldPlantTree() {
        boolean shouldPlant=false;
        if(treeCounter<20) {
            shouldPlant=true;
        }
        return shouldPlant;
    }

    public void doGardenerStuff() throws GameActionException{
        MapLocation closestNode=null;
        MapLocation[] allNodes=null;

        closestNode=getClosestNode();
        allNodes=getNearbyNodes(closestNode);

        TreeInfo[] allAlliedTrees=rc.senseNearbyTrees(rc.getType().sensorRadius,rc.getTeam());
        TreeInfo weakestTree=getLowestHealthTree(allAlliedTrees);
        /*
        for(MapLocation tempLoc : allNodes) {
            rc.setIndicatorDot(tempLoc, 255, 0, 0);
        }
        if(initialTree!=null){
            rc.setIndicatorDot(initialTree,255,255,255);}
        */
        if(shouldPlantTree() && (weakestTree==null || weakestTree.health>treeWaterThreshold)){
            if(isFirstTree) {
                plantFirstTree();
                treeCounter++;
            }
            else
            {
                System.out.println("A");
                findNodeClosestToStartingNode(allNodes);
                if(trackOpenNodeClosestToStart != null)
                {
                    System.out.println("B");
                    if(closeEnoughToPlant(trackOpenNodeClosestToStart)) {
                        System.out.println("C");
                        boolean plantingWasSuccessful=tryToPlant(trackOpenNodeClosestToStart);
                        if(plantingWasSuccessful) {
                            System.out.println("D");
                            trackOpenNodeClosestToStart=null;
                            treeCounter++;
                        }
                    } else {
                        System.out.println("E");
                        pathHandler.moveNear(trackOpenNodeClosestToStart,buildOffset);
                        //rc.setIndicatorDot(trackOpenNodeClosestToStart,0,255,0);
                    }
                }
            }
        } else {
            //move towards weakest tree
            if(weakestTree!=null) {
                System.out.println("F");
                pathHandler.moveNear(weakestTree.location, treeWaterDist);
            }
        }
        System.out.println("G");
        waterWeakestTreeInRange(allAlliedTrees);

        //draw dots

        //rc.setIndicatorDot(closestNode,0,255,0);
        //if(trackOpenNodeClosestToStart!=null) {
        //    rc.setIndicatorDot(trackOpenNodeClosestToStart,0,0,0); }
        //rc.setIndicatorDot(initialTree,0,0,255);
    }

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
            //rc.setIndicatorDot(weakestNearbyTree.getLocation(),0,0,255);
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
            if(rc.canSenseLocation(nodesToCheck[i]) && rc.senseTreeAtLocation(nodesToCheck[i])==null) {
                float distFromLocationToFirstTree=nodesToCheck[i].distanceTo(initialTree);
                if (distFromLocationToFirstTree<closestDistance) {
                    nextNode=nodesToCheck[i];
                    closestDistance=distFromLocationToFirstTree;
                }
            }
        }

        if(trackOpenNodeClosestToStart==null) {
            trackOpenNodeClosestToStart=nextNode;
        } else {
            if(nextNode!=null) {
                if(nextNode.distanceTo(initialTree)<trackOpenNodeClosestToStart.distanceTo(initialTree)) {
                    trackOpenNodeClosestToStart=nextNode;
                }
            }
        }
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
        float tempx=currentLocation.x-(currentLocation.x-initialTreeX)%delX;
        float tempy=currentLocation.y-(currentLocation.y-initialTreeY)%delY;
        MapLocation bottomLeft=new MapLocation(tempx,tempy);
        System.out.println("InitialTreeXY: "+initialTreeX+","+initialTreeY);
        rc.setIndicatorDot(bottomLeft,0,0,255);
        float xcheck=Math.abs((tempx-initialTreeX)%(2f*delX));
        float ycheck=Math.abs((tempy-initialTreeY)%(2f*delY));
        System.out.println("Xcheck "+xcheck);
        System.out.println("Ycheck "+ycheck);
        System.out.println("delX "+delX);
        System.out.println("delY "+delY);
        MapLocation node1=null,node2=null;
        if((xcheck<0.001f && ycheck<0.001f) || (Math.abs(xcheck-delX)<0.001f && Math.abs(ycheck-delY)<0.001f))
        {
            node1=bottomLeft;
            node2=new MapLocation(tempx+delX,tempy+delY);
        }
        else
        {
            node1=new MapLocation(tempx+delX,tempy);
            node2=new MapLocation(tempx,tempy+delY);
        }
        rc.setIndicatorDot(node1,255,0,0);
        rc.setIndicatorDot(node2,0,255,0);

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
        if(rc.senseNearbyTrees(rc.getType().sensorRadius,rc.getTeam()).length==0) {
            Direction treeDirection = rc.getLocation().directionTo(midpoint);
            if(rc.canPlantTree(treeDirection)) {
                rc.plantTree(treeDirection);
                initialTree = rc.getLocation().add(treeDirection,rc.getType().bodyRadius+GameConstants.BULLET_TREE_RADIUS+GameConstants.GENERAL_SPAWN_OFFSET);
                messageHandler.setTreeGridStartX(rc,initialTree.x);
                messageHandler.setTreeGridStartY(rc,initialTree.y);

                initialTreeX=messageHandler.getTreeGridStartX(rc);
                initialTreeY=messageHandler.getTreeGridStartY(rc);
                System.out.println("Initial Tree: "+initialTreeX+","+initialTreeY);
            }
        }
        isFirstTree=false;
    }

}