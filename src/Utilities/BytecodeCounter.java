package Utilities;
import battlecode.common.*;

public class BytecodeCounter {
    private int startingBytecodeNum;

    public BytecodeCounter() {
        startingBytecodeNum=Clock.getBytecodeNum();
    }
    public int getBytecodeUsed() {
        return Clock.getBytecodeNum()-startingBytecodeNum;
    }
}
