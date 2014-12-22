package com.felhr.usbmassstorageforandroid.scsi;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 12/12/14.
 */
public abstract class SCSICommand
{
    protected boolean dataTransportPhase;
    protected int direction; // 0:NONE 1:IN(to host) 2:OUT(from host)

    public SCSICommand()
    {

    }

    /*
        Serialize a SCSI command object into a stream of bytes
     */
    public abstract byte[] getSCSICommandBuffer();

    public boolean hasDataPhase() // This will be called by the SCSI command handler
    {
        return dataTransportPhase;
    }

    public int getDirection() // This will be called by the SCSI command handler
    {
        return direction;
    }

    protected byte[] convertToByte(int number)
    {
        byte[] buffer;
        if(number >= 0x00 && number <= 0xff)
        {
            buffer = new byte[1];
            buffer[0] = (byte) number;
        }else if(number > 0xff && number <= 0xffff)
        {
            buffer = new byte[2];
            buffer[0] = (byte) (number & 0xff);
            buffer[1] = (byte) (number >> 8 & 0xff);
        }else if(number > 0xffff && number <= 0xffffff)
        {
            buffer = new byte[3];
            buffer[0] = (byte) (number & 0xff);
            buffer[1] = (byte) (number >> 8 & 0xff);
            buffer[2] = (byte) (number >> 16 & 0xff);
        }else if(number > 0xffffff && number <= 0xffffffff)
        {
            buffer = new byte[4];
            buffer[0] = (byte) (number & 0xff);
            buffer[1] = (byte) (number >> 8 & 0xff);
            buffer[2] = (byte) (number >> 16 & 0xff);
            buffer[3] = (byte) (number >> 24 & 0xff);

        }else
        {
            buffer = null;
        }
        return buffer;
    }
}
