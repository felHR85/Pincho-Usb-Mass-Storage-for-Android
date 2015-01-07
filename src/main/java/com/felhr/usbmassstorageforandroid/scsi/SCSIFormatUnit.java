package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIFormatUnit extends SCSICommand
{
    public static final byte FORMATUNIT_OPERATION_CODE = 0x04;

    private boolean fmtpinfo;
    private boolean rtoReq;
    private boolean longList;
    private boolean fmtData;
    private boolean cmplst;
    private int defectListFormat;
    private byte vendorSpecific;
    private byte control;

    public SCSIFormatUnit(boolean fmtpinfo, boolean rtoReq, boolean longList,
                          boolean fmtData, boolean cmplst, int defectListFormat)
    {
        this.dataTransportPhase = true; // Format Unit can send data during data-phase
        this.direction = 0;
        this.fmtpinfo = fmtpinfo;
        this.rtoReq = rtoReq;
        this.longList = longList;
        this.fmtData = fmtData;
        this.cmplst = cmplst;
        this.defectListFormat = defectListFormat;
        this.vendorSpecific = 0x00;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(FORMATUNIT_OPERATION_CODE);
        byte firstByte = 0x00;

        if(fmtpinfo)
            firstByte |= (1 << 7);
        if(rtoReq)
            firstByte |= (1 << 6);
        if(longList)
            firstByte |= (1 << 5);
        if(fmtData)
            firstByte |= (1 << 4);
        if(cmplst)
            firstByte |= (1 << 3);
        switch(defectListFormat)
        {
            case 0:
                firstByte &= ~(1 << 2);
                firstByte &= ~(1 << 1);
                firstByte &= ~(1);
                break;
            case 1:
                firstByte &= ~(1 << 2);
                firstByte &= ~(1 << 1);
                firstByte |= 1;
                break;
            case 2:
                firstByte &= ~(1 << 2);
                firstByte |= (1 << 1);
                firstByte &= ~(1);
            case 3:
                firstByte &= ~(1 << 2);
                firstByte |= (1 << 1);
                firstByte |= 1;
                break;
            case 4:
                firstByte |= (1 << 2);
                firstByte &= ~(1 << 1);
                firstByte &= ~(1);
                break;
            case 5:
                firstByte |= (1 << 2);
                firstByte &= ~(1 << 1);
                firstByte |= 1;
                break;
            case 6:
                firstByte |= (1 << 2);
                firstByte |= (1 << 1);
                firstByte &= ~(1);
                break;
            case 7:
                firstByte |= (1 << 2);
                firstByte |= (1 << 1);
                firstByte |= 1;
                break;

        }
        buffer.put(firstByte);
        buffer.put(vendorSpecific);
        buffer.put((byte) 0x00);
        buffer.put(control);
        return buffer.array();
    }

    @Override
    public CommandBlockWrapper getCbw()
    {
        byte[] rawInstruction = this.getSCSICommandBuffer();
        int dCBWDataTransferLength = 0;

        byte bmCBWFlags = 0x00;

        byte bCBWLUN = 0x00; // Check this!!!
        byte bCBWCBLength = (byte) (rawInstruction.length);

        CommandBlockWrapper cbw = new CommandBlockWrapper(dCBWDataTransferLength, bmCBWFlags, bCBWLUN, bCBWCBLength);
        return cbw;
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
