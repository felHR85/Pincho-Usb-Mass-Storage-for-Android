package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 23/12/14.
 */
public class SCSIReportLuns extends SCSICommand
{
    public static final byte REPORTLUNS_OPERATION_CODE = (byte) 0xa0;

    private int selectReport; // 1 byte
    private int allocationLength; // 2 bytes
    private byte control;

    public SCSIReportLuns(int selectReport, int allocationLength)
    {
        this.dataTransportPhase = true;
        this.direction = 1;
        this.selectReport = selectReport;
        this.allocationLength = allocationLength;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put(REPORTLUNS_OPERATION_CODE);
        buffer.put((byte) 0x00);
        buffer.put(convertToByte(selectReport, 1));
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put(convertToByte(allocationLength, 2));
        buffer.put((byte) 0x00);
        buffer.put(control);
        return buffer.array();
    }

    @Override
    public CommandBlockWrapper getCbw()
    {
        //TODO
        return null;
    }

    @Override
    public void setDataPhaseBuffer(byte[] data)
    {

    }

    @Override
    public byte[] getDataPhaseBuffer()
    {
        return null;
    }
}
