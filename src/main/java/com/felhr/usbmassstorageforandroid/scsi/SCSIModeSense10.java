package com.felhr.usbmassstorageforandroid.scsi;

import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 22/12/14.
 */
public class SCSIModeSense10 extends SCSICommand
{
    public static final byte MODESENSE10_OPERATION_CODE = 0x5a;
    private static final int MODESENSE10_COMMAND_LENGTH = 10;

    private boolean llbaa;
    private boolean dbd;
    private int pc; //2 bits
    private int pageCode; // 6 bits
    private int subPageCode; // 1 byte
    private int allocationLength; // 2 bytes
    private byte control;

    public SCSIModeSense10(boolean llbaa, boolean dbd, int pc,
                           int pageCode, int subPageCode, int allocationLength)
    {
        this.dataTransportPhase = true;
        this.direction = 1;
        this.llbaa = llbaa;
        this.dbd = dbd;
        this.pc = pc;
        this.pageCode = pageCode;
        this.subPageCode = subPageCode;
        this.allocationLength = allocationLength;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(MODESENSE10_OPERATION_CODE);

        byte firstByte = 0x00;
        if(llbaa)
            firstByte |= (1 << 4);
        if(dbd)
            firstByte |= (1 << 3);
        buffer.put(firstByte);

        byte secondByte = (byte) pageCode;
        switch(pc)
        {
            case 0:
                secondByte &= ~(1 << 7);
                secondByte &= ~(1 << 6);
                break;
            case 1:
                secondByte &= ~(1 << 7);
                secondByte |= (1 << 6);
                break;
            case 2:
                secondByte |= (1 << 7);
                secondByte &= ~(1 << 6);
                break;
            case 3:
                secondByte |= (1 << 7);
                secondByte |= (1 << 6);
                break;
        }
        buffer.put(secondByte);

        buffer.put(convertToByte(subPageCode, 1));
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put(convertToByte(allocationLength, 2));
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
        byte bCBWCBLength = (byte) MODESENSE10_COMMAND_LENGTH;

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
