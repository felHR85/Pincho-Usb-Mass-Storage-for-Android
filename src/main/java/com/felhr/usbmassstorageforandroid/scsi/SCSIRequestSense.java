package com.felhr.usbmassstorageforandroid.scsi;

import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 15/12/14.
 */
public class SCSIRequestSense extends SCSICommand
{
    public static final byte REQUESTSENSE_OPERATION_CODE = 0x03;
    private static final int REQUESTSENSE_COMMAND_LENGTH = 6;

    private byte operationCode;
    private boolean desc;
    private int allocationLength;
    private byte control;

    public SCSIRequestSense(boolean desc, int allocationLength)
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
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put(operationCode);
        if(desc)
            buffer.put((byte) 0x01);
        else
            buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put(convertToByte(allocationLength, 1));
        buffer.put(control);
        return buffer.array();
    }

    @Override
    public CommandBlockWrapper getCbw()
    {
        byte[] rawCommand = getCbwcb(getSCSICommandBuffer());
        Log.i("Buffer state", "SCSI: " + HexUtil.hexToString(rawCommand));

        int dCBWDataTransferLength = allocationLength;

        byte bmCBWFlags = 0x00;

        bmCBWFlags |= (1 << 7); // From device to host

        byte bCBWLUN = 0x00; // Check this!!!
        byte bCBWCBLength = (byte) (REQUESTSENSE_COMMAND_LENGTH);

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
