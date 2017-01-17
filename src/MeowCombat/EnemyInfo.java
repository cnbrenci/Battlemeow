package MeowCombat;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

/**
 * Created by Cassi on 1/16/2017.
 */
public class EnemyInfo extends RobotInfo {

    public EnemyInfo(RobotInfo robotInfo, int lastRoundSeen) {
        super(robotInfo.ID, robotInfo.team, robotInfo.type, robotInfo.location, robotInfo.health, robotInfo.attackCount, robotInfo.moveCount);
    }
}
