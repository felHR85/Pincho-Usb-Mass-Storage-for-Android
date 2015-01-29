package com.felhr.usbmassstorageforandroid.scsi;

import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIReadCapacity10 extends SCSICommand
{
    public static final byte READCAPACITY10_OPERATION_CODE = 0x25;
    private static final int READCAPACITY10_COMMAND_LENGTH = 10;

    private int logicalBlockAddress;
    private boolean pmi;
    private byte control;

    public SCSIReadCapacity10(int logicalBlockAddress, boolean pmi)
    {
        this.dataTransportPhase = true;
        this.direction = 1;
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
        buffer.putInt(logicalBlockAddress);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        byte pmiByte = 0x00;
        if(pmi)
            pmiByte |= 1;
        buffer.put(pmiByte);
        buffer.put(control);
        return buffer.array();
    }

    @Override
    public CommandBlockWrapper getCbw()
    {
        byte[] rawCommand = getCbwcb(getSCSICommandBuffer());
        Log.i("Buffer state", "SCSI: " + HexUtil.hexToString(rawCommand));

        int dCBWDataTransferLength = 8;

        byte bmCBWFlags = 0x00;

        bmCBWFlags |= (1 << 7); // From device to host

        byte bCBWLUN = 0x00; // Check this!!!
        byte bCBWCBLength = (byte) (READCAPACITY10_COMMAND_LENGTH);

        CommandBlockWrapper cbw = new CommandBlockWrapper(dCBWDataTransferLength, bmCBWFlags, bCBWLUN, bCBWCBLength);
        cbw.setCommandBlock(rawCommand);
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
