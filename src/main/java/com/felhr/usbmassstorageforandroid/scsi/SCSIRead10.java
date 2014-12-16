package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIRead10 extends SCSICommand
{
    public static final byte READ_OPERATION_CODE = 0x28;

    public SCSIRead10()
    {

    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        return buffer.array();
    }
}
