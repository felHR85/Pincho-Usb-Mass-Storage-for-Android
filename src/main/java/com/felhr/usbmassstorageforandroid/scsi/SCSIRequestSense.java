package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 15/12/14.
 */
public class SCSIRequestSense extends SCSICommand
{
    public static final byte REQUESTSENSE_OPERATION_CODE = 0x03;

    private byte operationCode;
    private boolean desc;
    private byte allocationLength;
    private byte control;

    public SCSIRequestSense(boolean desc, byte allocationLength)
    {
        this.dataTransportPhase = true;
        this.direction = 1;
        this.operationCode = REQUESTSENSE_OPERATION_CODE;
        this.desc = desc;
        this.allocationLength = allocationLength;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(operationCode);
        if(desc)
            buffer.put((byte) 0x01);
        else
            buffer.put((byte) 0x00);
        buffer.put(allocationLength);
        buffer.put(control);
        return buffer.array();
    }
}
