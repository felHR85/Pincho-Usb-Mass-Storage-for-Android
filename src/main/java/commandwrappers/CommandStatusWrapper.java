package commandwrappers;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/12/14.
 */
public class CommandStatusWrapper extends CommandWrapper
{
    private int dCSWSignature;
    private int dCSWTag;
    private int dCSWDataResidue;
    private byte bCSWStatus;

    public CommandStatusWrapper(int dCSWSignature, int dCSWTag, int dCSWDataResidue,
                                byte bCSWStatus)
    {
        this.dCSWSignature = dCSWSignature;
        this.dCSWTag = dCSWTag;
        this.dCSWDataResidue = dCSWDataResidue;
        this.bCSWStatus = bCSWStatus;
    }

    public static CommandStatusWrapper getCWStatus(byte[] buffer)
    {
        int signature = convertToInt(Arrays.copyOfRange(buffer, 0, 4));
        int tag = convertToInt(Arrays.copyOfRange(buffer, 4, 8));
        int residue = convertToInt(Arrays.copyOfRange(buffer, 8, 12));
        byte status = buffer[12];
        return new CommandStatusWrapper(signature, tag, residue, status);
    }

    private static int convertToInt(byte[] buffer)
    {
        int result;
        result = buffer[0] & 0xff;
        result = result + ((buffer[1] & 0xff) << 8);
        result = result + ((buffer[2] & 0xff) << 16);
        return result + ((buffer[3] & 0xff) << 24);
    }

    @Override
    public byte[] getCWBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(CBS_SIZE);
        buffer.putInt(dCSWSignature);
        buffer.putInt(dCSWTag);
        buffer.putInt(dCSWDataResidue);
        buffer.put(bCSWStatus);
        return buffer.array();
    }

    public int getdCSWSignature()
    {
        return dCSWSignature;
    }

    public void setdCSWSignature(int dCSWSignature)
    {
        this.dCSWSignature = dCSWSignature;
    }

    public int getdCSWTag()
    {
        return dCSWTag;
    }

    public void setdCSWTag(int dCSWTag)
    {
        this.dCSWTag = dCSWTag;
    }

    public int getdCSWDataResidue()
    {
        return dCSWDataResidue;
    }

    public void setdCSWDataResidue(int dCSWDataResidue)
    {
        this.dCSWDataResidue = dCSWDataResidue;
    }

    public byte getbCSWStatus()
    {
        return bCSWStatus;
    }

    public void setbCSWStatus(byte bCSWStatus)
    {
        this.bCSWStatus = bCSWStatus;
    }
}
