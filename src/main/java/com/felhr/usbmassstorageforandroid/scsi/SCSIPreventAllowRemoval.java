package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 22/12/14.
 */
public class SCSIPreventAllowRemoval extends SCSICommand
{
    public static final byte PREVENTALLOWREMOVAL_OPERATION_CODE = 0x1e;

    public SCSIPreventAllowRemoval()
    {

    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        // TODO
       return null;
    }
}
