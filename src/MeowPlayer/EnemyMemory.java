package MeowPlayer;
import battlecode.common.*;

public class EnemyMemory
{
    MapLocation lastLocation;
    float lastHealth;
    int turnSeen;
    int unitIDNumber;
    RobotType unitType;

    public EnemyMemory(RobotInfo sensedInfo,int turn)
    {
        updateMemory(sensedInfo,turn);
    }
    public void updateMemory(RobotInfo sensedInfo, int turn)
    {
        lastLocation=sensedInfo.location;
        lastHealth=(float)sensedInfo.health;
        unitIDNumber=sensedInfo.ID;
        turnSeen=turn;
        unitType=sensedInfo.getType();
    }
}
