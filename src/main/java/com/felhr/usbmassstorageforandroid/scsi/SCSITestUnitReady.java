package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSITestUnitReady extends SCSICommand
{
    public byte TESTUNIT_OPERATION_CODE = 0x00;
    public byte control;

    public SCSITestUnitReady()
    {
        this.dataTransportPhase = false;
        this.direction = 0;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(TESTUNIT_OPERATION_CODE);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
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
