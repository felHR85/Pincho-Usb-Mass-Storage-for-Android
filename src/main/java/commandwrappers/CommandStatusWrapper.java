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

    public CommandStatusWrapper getCWStatus(byte[] buffer)
    {
        int signature = convertToInt(Arrays.copyOfRange(buffer, 0, 4));
        int tag = convertToInt(Arrays.copyOfRange(buffer, 4, 4));
        int residue = convertToInt(Arrays.copyOfRange(buffer, 8, 4));
        byte status = Arrays.copyOfRange(buffer, 12, 1)[0];
        return new CommandStatusWrapper(signature, tag, residue, status);
    }

    private int convertToInt(byte[] buffer)
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
}
