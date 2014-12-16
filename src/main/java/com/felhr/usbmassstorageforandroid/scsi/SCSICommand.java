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

    public boolean hasDataPhase()
    {
        return dataTransportPhase;
    }

    public int getDirection()
    {
        return direction;
    }
}
