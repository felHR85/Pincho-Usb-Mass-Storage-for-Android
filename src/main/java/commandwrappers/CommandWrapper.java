package commandwrappers;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/12/14.
 */
public abstract class CommandWrapper
{
    public static final int CBW_SIGNATURE = 0x43425355;
    public static final int CBS_SIGNATURE = 0x53425355;
    public static final int CBW_SIZE = 31;
    public static final int CBS_SIZE = 13;

    public CommandWrapper()
    {

    }

    public abstract byte[] getCWBuffer();


    protected class CommandBlock
    {
        // 128 bit command instruction
        private int data1; // Most Significant
        private int data2;
        private int data3;
        private int data4; // Less Significant

        public CommandBlock(int data1, int data2, int data3, int data4)
        {
            this.data1 = data1;
            this.data2 = data2;
            this.data3 = data3;
            this.data4 = data4;
        }

        public byte[] getBuffer()
        {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(data1);
            buffer.putInt(data2);
            buffer.putInt(data3);
            buffer.putInt(data4);
            return buffer.array();
        }

    }

}
