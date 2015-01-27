package commandwrappers;

import com.felhr.usbmassstorageforandroid.utilities.EndianessUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

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
    private byte[] commandBlock;


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

    public CommandBlockWrapper( int dCBWDataLength,
                                byte bmCBWFlags, byte bCBWLUN, byte bCBWCBLength)
    {
        this.dCBWSignature = CommandWrapper.CBW_SIGNATURE;
        this.dCBWTag = generateTag();
        this.dCBWDataLength = dCBWDataLength;
        this.bmCBWFlags = bmCBWFlags;
        this.bCBWLUN = bCBWLUN;
        this.bCBWCBLength = bCBWCBLength;
    }

    public void setCommandBlock(byte[] commandBlock)
    {
        this.commandBlock = commandBlock;
    }

    public int getdCBWDataLength()
    {
        return dCBWDataLength;
    }

    public int generateTag()
    {
        Random random = new Random();
        return random.nextInt();
    }

    @Override
    public byte[] getCWBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(CBW_SIZE);
        buffer.putInt(EndianessUtil.swapEndianess(dCBWSignature));
        buffer.putInt(dCBWTag);
        buffer.putInt(EndianessUtil.swapEndianess(dCBWDataLength));
        buffer.put(bmCBWFlags);
        buffer.put(bCBWLUN);
        buffer.put(bCBWCBLength);
        buffer.put(commandBlock);
        return buffer.array();
    }
}
