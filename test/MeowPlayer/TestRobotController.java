package MeowPlayer;

import battlecode.common.*;

/**
 * Created by Cassi on 1/22/2017.
 */
public class TestRobotController implements RobotController {

    private int[] sharedTeamArray = new int[10000];

    @Override
    public float getVictoryPointCost() {
        return 0;
    }

    @Override
    public float readBroadcastFloat(int channel) {
        return Float.intBitsToFloat(sharedTeamArray[channel]);
    }

    @Override
    public int readBroadcastInt(int channel) {
        return sharedTeamArray[channel];
    }

    @Override
    public boolean readBroadcastBoolean(int channel) {
        return (sharedTeamArray[channel] != 0) ? true : false;
    }

    @Override
    public void broadcastFloat(int channel, float data) {
        sharedTeamArray[channel] = Float.floatToIntBits(data);
    }

    @Override
    public void broadcastBoolean(int channel, boolean data) {
        sharedTeamArray[channel] = data ? 1 : 0;
    }

    @Override
    public void broadcastInt(int channel, int data) {
        sharedTeamArray[channel] = data;
    }

    @Override
    public boolean canSenseBulletLocation(MapLocation loc) {
        return false;
    }

    @Override
    public int getOpponentVictoryPoints() {
        return 0;
    }

    @Override
    public void broadcast(int i, int i1) throws GameActionException {
        sharedTeamArray[i] = i1;
    }

    @Override
    public int readBroadcast(int i) throws GameActionException {
        return sharedTeamArray[i];
    }

    @Override
    public int getRoundLimit() {
        return 0;
    }

    @Override
    public int getRoundNum() {
        return 0;
    }

    @Override
    public float getTeamBullets() {
        return 0;
    }

    @Override
    public int getTeamVictoryPoints() {
        return 0;
    }

    @Override
    public int getRobotCount() {
        return 0;
    }

    @Override
    public int getTreeCount() {
        return 0;
    }

    @Override
    public MapLocation[] getInitialArchonLocations(Team team) {
        return new MapLocation[0];
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public Team getTeam() {
        return null;
    }

    @Override
    public RobotType getType() {
        return null;
    }

    @Override
    public MapLocation getLocation() {
        return null;
    }

    @Override
    public float getHealth() {
        return 0;
    }

    @Override
    public int getAttackCount() {
        return 0;
    }

    @Override
    public int getMoveCount() {
        return 0;
    }

    @Override
    public boolean onTheMap(MapLocation mapLocation) throws GameActionException {
        return false;
    }

    @Override
    public boolean onTheMap(MapLocation mapLocation, float v) throws GameActionException {
        return false;
    }

    @Override
    public boolean canSenseLocation(MapLocation mapLocation) {
        return false;
    }

    @Override
    public boolean canSenseRadius(float v) {
        return false;
    }

    @Override
    public boolean canSensePartOfCircle(MapLocation mapLocation, float v) {
        return false;
    }

    @Override
    public boolean canSenseAllOfCircle(MapLocation mapLocation, float v) {
        return false;
    }

    @Override
    public boolean isLocationOccupied(MapLocation mapLocation) throws GameActionException {
        return false;
    }

    @Override
    public boolean isLocationOccupiedByTree(MapLocation mapLocation) throws GameActionException {
        return false;
    }

    @Override
    public boolean isLocationOccupiedByRobot(MapLocation mapLocation) throws GameActionException {
        return false;
    }

    @Override
    public boolean isCircleOccupied(MapLocation mapLocation, float v) throws GameActionException {
        return false;
    }

    @Override
    public boolean isCircleOccupiedExceptByThisRobot(MapLocation mapLocation, float v) throws GameActionException {
        return false;
    }

    @Override
    public TreeInfo senseTreeAtLocation(MapLocation mapLocation) throws GameActionException {
        return null;
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation mapLocation) throws GameActionException {
        return null;
    }

    @Override
    public boolean canSenseTree(int i) {
        return false;
    }

    @Override
    public boolean canSenseRobot(int i) {
        return false;
    }

    @Override
    public boolean canSenseBullet(int i) {
        return false;
    }

    @Override
    public TreeInfo senseTree(int i) throws GameActionException {
        return null;
    }

    @Override
    public RobotInfo senseRobot(int i) throws GameActionException {
        return null;
    }

    @Override
    public BulletInfo senseBullet(int i) throws GameActionException {
        return null;
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        return new RobotInfo[0];
    }

    @Override
    public RobotInfo[] senseNearbyRobots(float v) {
        return new RobotInfo[0];
    }

    @Override
    public RobotInfo[] senseNearbyRobots(float v, Team team) {
        return new RobotInfo[0];
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation mapLocation, float v, Team team) {
        return new RobotInfo[0];
    }

    @Override
    public TreeInfo[] senseNearbyTrees() {
        return new TreeInfo[0];
    }

    @Override
    public TreeInfo[] senseNearbyTrees(float v) {
        return new TreeInfo[0];
    }

    @Override
    public TreeInfo[] senseNearbyTrees(float v, Team team) {
        return new TreeInfo[0];
    }

    @Override
    public TreeInfo[] senseNearbyTrees(MapLocation mapLocation, float v, Team team) {
        return new TreeInfo[0];
    }

    @Override
    public BulletInfo[] senseNearbyBullets() {
        return new BulletInfo[0];
    }

    @Override
    public BulletInfo[] senseNearbyBullets(float v) {
        return new BulletInfo[0];
    }

    @Override
    public BulletInfo[] senseNearbyBullets(MapLocation mapLocation, float v) {
        return new BulletInfo[0];
    }

    @Override
    public MapLocation[] senseBroadcastingRobotLocations() {
        return new MapLocation[0];
    }

    @Override
    public boolean hasMoved() {
        return false;
    }

    @Override
    public boolean hasAttacked() {
        return false;
    }

    @Override
    public boolean isBuildReady() {
        return false;
    }

    @Override
    public int getBuildCooldownTurns() {
        return 0;
    }

    @Override
    public boolean canMove(Direction direction) {
        return false;
    }

    @Override
    public boolean canMove(Direction direction, float v) {
        return false;
    }

    @Override
    public boolean canMove(MapLocation mapLocation) {
        return false;
    }

    @Override
    public void move(Direction direction) throws GameActionException {

    }

    @Override
    public void move(Direction direction, float v) throws GameActionException {

    }

    @Override
    public void move(MapLocation mapLocation) throws GameActionException {

    }

    @Override
    public boolean canStrike() {
        return false;
    }

    @Override
    public void strike() throws GameActionException {

    }

    @Override
    public boolean canFireSingleShot() {
        return false;
    }

    @Override
    public boolean canFireTriadShot() {
        return false;
    }

    @Override
    public boolean canFirePentadShot() {
        return false;
    }

    @Override
    public void fireSingleShot(Direction direction) throws GameActionException {

    }

    @Override
    public void fireTriadShot(Direction direction) throws GameActionException {

    }

    @Override
    public void firePentadShot(Direction direction) throws GameActionException {

    }

    @Override
    public boolean canChop(MapLocation mapLocation) {
        return false;
    }

    @Override
    public boolean canChop(int i) {
        return false;
    }

    @Override
    public void chop(MapLocation mapLocation) throws GameActionException {

    }

    @Override
    public void chop(int i) throws GameActionException {

    }

    @Override
    public boolean canShake(MapLocation mapLocation) {
        return false;
    }

    @Override
    public boolean canShake(int i) {
        return false;
    }

    @Override
    public void shake(MapLocation mapLocation) throws GameActionException {

    }

    @Override
    public void shake(int i) throws GameActionException {

    }

    @Override
    public boolean canWater(MapLocation mapLocation) {
        return false;
    }

    @Override
    public boolean canWater(int i) {
        return false;
    }

    @Override
    public void water(MapLocation mapLocation) throws GameActionException {

    }

    @Override
    public void water(int i) throws GameActionException {

    }

    @Override
    public boolean canWater() {
        return false;
    }

    @Override
    public boolean canShake() {
        return false;
    }

    @Override
    public boolean canInteractWithTree(MapLocation mapLocation) {
        return false;
    }

    @Override
    public boolean canInteractWithTree(int i) {
        return false;
    }

    @Override
    public boolean hasRobotBuildRequirements(RobotType robotType) {
        return false;
    }

    @Override
    public boolean hasTreeBuildRequirements() {
        return false;
    }

    @Override
    public boolean canBuildRobot(RobotType robotType, Direction direction) {
        return false;
    }

    @Override
    public void buildRobot(RobotType robotType, Direction direction) throws GameActionException {

    }

    @Override
    public boolean canPlantTree(Direction direction) {
        return false;
    }

    @Override
    public void plantTree(Direction direction) throws GameActionException {

    }

    @Override
    public boolean canHireGardener(Direction direction) {
        return false;
    }

    @Override
    public void hireGardener(Direction direction) throws GameActionException {

    }

    @Override
    public void donate(float v) throws GameActionException {

    }

    @Override
    public void disintegrate() {

    }

    @Override
    public void resign() {

    }

    @Override
    public void setIndicatorDot(MapLocation mapLocation, int i, int i1, int i2)   {

    }

    @Override
    public void setIndicatorLine(MapLocation mapLocation, MapLocation mapLocation1, int red, int green, int blue) {
    }

    @Override
    public void setTeamMemory(int i, long l) {

    }

    @Override
    public void setTeamMemory(int i, long l, long l1) {

    }

    @Override
    public long[] getTeamMemory() {
        return new long[0];
    }

    @Override
    public long getControlBits() {
        return 0;
    }
}