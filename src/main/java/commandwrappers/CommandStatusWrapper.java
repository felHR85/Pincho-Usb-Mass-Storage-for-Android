package commandwrappers;

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
        // TO-DO
        return null;
    }

    @Override
    public byte[] getCWBuffer()
    {
        return new byte[0];
    }
}
