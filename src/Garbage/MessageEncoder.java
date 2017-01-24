package Garbage;

import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

/**
 * Created by Cassi on 1/21/2017.
 *
 * first
 *  x:     0b11111111 0b11000000 0b00000000 0b00000000
 *  y:     0b00000000 0b00111111 0b11110000 0b00000000
 *  type:  0b00000000 0b00000000 0b00001110 0b00000000
 *  dir:   0b00000000 0b00000000 0b00000001 0b11111111
 *
 *  second
 *  id:    0b11111111 0b11111110 0b00000000 0b00000000
 *  HP:    0b00000000 0b00000001 0b11111111 0b11000000
 *  type2: 0b00000000 0b00000000 0b00000000 0b00111000
 *  radius:0b00000000 0b00000000 0b00000000 0b00000111
 */
public class MessageEncoder {
    private static final byte TREE_BYTE = 0b000;
    private static final byte BULLET_BYTE = 0b001;
    private static final byte ARCHON_BYTE = 0b010;
    private static final byte GARDENER_BYTE = 0b011;
    private static final byte SOLDIER_BYTE = 0b100;
    private static final byte TANK_BYTE = 0b101;
    private static final byte LUMBERJACK_BYTE = 0b110;
    private static final byte SCOUT_BYTE = 0b111;

    public static IntPair Encode(TreeInfo tree) {
        // & the int against the mask, then | it with the existing thing, shifted correctly.
        int x = (int)tree.getLocation().x;
        int y = (int)tree.getLocation().y;

        byte[] firstInt = new byte[4];
        firstInt[0] = (byte) (x >> 2); // x 0:0b11111111 1:0b11000000
        firstInt[1] = (byte) (x << 6);

        firstInt[1] = (byte) (firstInt[1] | (byte) (y >> 4)); // y 1:0b00111111 2:0b11110000
        firstInt[2] = (byte) (y << 4);

        firstInt[2] = (byte) (firstInt[2] | (byte) (TREE_BYTE << 1)); // type 2:0b00001110
        // firstInt[3] isn't used for trees. those bits are for bullet direction.

       // int first = byteArrayToInt(firstInt);
        //byte[] check = intToByteArray(first);

        int hp = (int)tree.health;
        //hp = 128;
        byte[] secondInt = new byte[4]; // 32 bits
        secondInt[0] = (byte) (tree.ID >> 7); // id 0:0b11111111 1:0b11111110
        secondInt[1] = (byte) (tree.ID << 1);

        secondInt[1] = (byte) (secondInt[1] | ((byte) (hp >> 10) & 0b00000001)); // hp 1:0b00000001 2:0b11111111 3:0b11000000
        secondInt[2] = (byte) (hp >> 2);
        secondInt[3] = (byte) (hp << 6);

        secondInt[3] = (byte) (secondInt[3] | (byte) (getSecondaryType(tree) >> 2)); // secondary type 3:0b00111000
        secondInt[3] = (byte) (secondInt[3] | (byte) ((int)tree.radius & 0b00000111)); // radius 3:0b00000111

        //int second = byteArrayToInt(secondInt);
        //byte[] check2 = intToByteArray(second);

        return new IntPair(byteArrayToInt(firstInt),byteArrayToInt(secondInt));
    }

    private static byte getRobotByteType(RobotType robotType) {
        switch(robotType) {
            case ARCHON:
                return ARCHON_BYTE;
            case GARDENER:
                return GARDENER_BYTE;
            case LUMBERJACK:
                return LUMBERJACK_BYTE;
            case SOLDIER:
                return SOLDIER_BYTE;
            case TANK:
                return TANK_BYTE;
            case SCOUT:
                return SCOUT_BYTE;
        }
        System.out.println("wtf dis shit broken");
        return TREE_BYTE;
    }

    private static byte getSecondaryType(TreeInfo tree) {
        if(tree.containedBullets > 0)
            return BULLET_BYTE;
        if(tree.containedRobot != null) {
            return getRobotByteType(tree.containedRobot);
        }
        return TREE_BYTE;
    }

    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a)
    {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
}