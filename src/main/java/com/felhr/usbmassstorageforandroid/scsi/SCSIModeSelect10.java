package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 22/12/14.
 */
public class SCSIModeSelect10 extends SCSICommand
{
    public static final byte MODESELECT10_OPERATION_CODE = 0x55;

    private boolean pageFormat;
    private boolean savePages;
    private int parameterListLength;
    private byte control;

    public SCSIModeSelect10(boolean pageFormat, boolean savePages ,int parameterListLength)
    {
        this.dataTransportPhase = false;
        this.direction = 0;
        this.pageFormat = pageFormat;
        this.savePages = savePages;
        this.parameterListLength = parameterListLength;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put(MODESELECT10_OPERATION_CODE);
        byte firstByte = 0x00;

        if(pageFormat)
            firstByte |= (1 << 4);

        if(savePages)
            firstByte |= 1;

        buffer.put(firstByte);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put(convertToByte(parameterListLength, 2));
        buffer.put(control);
        return buffer.array();
    }

    @Override
    public CommandBlockWrapper getCbw()
    {
        byte[] rawInstruction = this.getSCSICommandBuffer();
        int dCBWDataTransferLength = parameterListLength; // MODE_SELECT10 Data phase OUT Endpoint???

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
