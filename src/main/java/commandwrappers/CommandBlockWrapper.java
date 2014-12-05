package commandwrappers;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/12/14.
 */
public class CommandBlockWrapper extends CommandWrapper
{
    private int dCBWSignature;
    private int dCBWTag;
    private int dCBWDataLength;
    private byte bmCBWFlags;
    private byte bCBWLUN;
    private byte bCBWCBLength;
    private CommandBlock commandBlock;


    public CommandBlockWrapper(int dCBWSignature, int dCBWTag, int dCBWDataLength,
                               byte bmCBWFlags, byte bCBWLUN, byte bCBWCBLength)
    {
        this.dCBWSignature = dCBWSignature;
        this.dCBWTag = dCBWTag;
        this.dCBWDataLength = dCBWDataLength;
        this.bmCBWFlags = bmCBWFlags;
        this.bCBWLUN = bCBWLUN;
        this.bCBWCBLength = bCBWCBLength;
    }

    public void setCommandBlock(int data1, int data2, int data3, int data4)
    {
        this.commandBlock = new CommandBlock(data1, data2, data3, data4);
    }

    @Override
    public byte[] getCWBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(CBW_SIZE);
        buffer.putInt(dCBWSignature);
        buffer.putInt(dCBWTag);
        buffer.putInt(dCBWDataLength);
        buffer.put(bmCBWFlags);
        buffer.put(bCBWLUN);
        buffer.put(bCBWCBLength);
        buffer.put(commandBlock.getBuffer());
        return buffer.array();
    }
}
