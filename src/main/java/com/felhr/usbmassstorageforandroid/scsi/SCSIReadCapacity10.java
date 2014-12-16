package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIReadCapacity10 extends SCSICommand
{
    public static final byte READCAPACITY10_OPERATION_CODE = 0x25;

    private byte[] logicalBlockAddress;
    private boolean pmi;
    private byte control;

    public SCSIReadCapacity10(byte[] logicalBlockAddress, boolean pmi)
    {
        this.logicalBlockAddress = logicalBlockAddress;
        this.pmi = pmi;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(READCAPACITY10_OPERATION_CODE);
        buffer.put((byte) 0x00);
        buffer.put(logicalBlockAddress);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        byte pmiByte = 0x00;
        if(pmi)
            pmiByte |= 1;
        buffer.put(pmiByte);
        buffer.put(control);
        return buffer.array();
    }
}
