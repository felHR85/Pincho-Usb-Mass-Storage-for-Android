package com.felhr.usbmassstorageforandroid.scsi;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 12/12/14.
 */
public abstract class SCSICommand
{
    public SCSICommand()
    {

    }

    /*
        Serialize a SCSI command object into a stream of bytes
     */
    public abstract byte[] getSCSICommandBuffer();
}
