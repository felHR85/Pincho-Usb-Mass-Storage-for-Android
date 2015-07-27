package com.felhr.usbmassstorageforandroid.scsi;


import java.nio.ByteBuffer;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIRead10 extends SCSICommand
{
    public static final byte READ10_OPERATION_CODE = 0x28;
    private static final byte READ10_COMMAND_LENGTH = 10;

    private int rdProtect;
    private boolean dpo;
    private boolean fua;
    private boolean fuaNv;
    private int logicalBlockAddress; // 4 bytes
    private int groupNumber; // 6bits
    private int transferLength; // 2 bytes
    private byte control;

    public SCSIRead10(int rdProtect, boolean dpo, boolean fua,
                      boolean fuaNv, int logicalBlockAddress,
                      int groupNumber, int transferLength)
    {
        this.dataTransportPhase = true;
        this.direction = 1;
        this.rdProtect = rdProtect;
        this.dpo = dpo;
        this.fua = fua;
        this.fuaNv = fuaNv;
        this.logicalBlockAddress = logicalBlockAddress;
        this.groupNumber = groupNumber;
        this.transferLength = transferLength;
        this.control = 0x00;

    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(READ10_OPERATION_CODE);

        byte firstByte = 0x00;
        switch(rdProtect)
        {
            case 0:
                firstByte &= ~(1 << 7);
                firstByte &= ~(1 << 6);
                firstByte &= ~(1 << 5);
                break;
            case 1:
                firstByte &= ~(1 << 7);
                firstByte &= ~(1 << 6);
                firstByte |= (1 << 5);
                break;
            case 2:
                firstByte &= ~(1 << 7);
                firstByte |= (1 << 6);
                firstByte &= ~(1 << 5);
            case 3:
                firstByte &= ~(1 << 7);
                firstByte |= (1 << 6);
                firstByte |= (1 << 5);
                break;
            case 4:
                firstByte |= (1 << 7);
                firstByte &= ~(1 << 6);
                firstByte &= ~(1 << 5);
                break;
            case 5:
                firstByte |= (1 << 7);
                firstByte &= ~(1 << 6);
                firstByte |= (1 << 5);
                break;
            case 6:
                firstByte |= (1 << 7);
                firstByte |= (1 << 6);
                firstByte &= ~(1 << 5);
                break;
            case 7:
                firstByte |= (1 << 7);
                firstByte |= (1 << 6);
                firstByte |= (1 << 5);
                break;
        }

        if(dpo)
            firstByte |= (1 << 4);
        if(fua)
            firstByte |= (1 << 3);
        if(fuaNv)
            firstByte |= (1 << 1);

        buffer.put(firstByte);
        buffer.putInt(logicalBlockAddress);

        buffer.put(convertToByte(groupNumber, 1));
        buffer.put(convertToByte(transferLength, 2));
        buffer.put(control);
        return buffer.array();
    }

    @Override
    public CommandBlockWrapper getCbw()
    {
        byte[] rawCommand = getCbwcb(getSCSICommandBuffer());
        int dCBWDataTransferLength = transferLength * 512;

        byte bmCBWFlags = 0x00;

        bmCBWFlags |= (1 << 7); // From device to host

        byte bCBWLUN = 0x00; // Check this!!!
        byte bCBWCBLength = READ10_COMMAND_LENGTH;

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

    public int getRdProtect()
    {
        return rdProtect;
    }

    public void setRdProtect(int rdProtect)
    {
        this.rdProtect = rdProtect;
    }

    public boolean isDpo()
    {
        return dpo;
    }

    public void setDpo(boolean dpo)
    {
        this.dpo = dpo;
    }

    public boolean isFua()
    {
        return fua;
    }

    public void setFua(boolean fua)
    {
        this.fua = fua;
    }

    public boolean isFuaNv()
    {
        return fuaNv;
    }

    public void setFuaNv(boolean fuaNv)
    {
        this.fuaNv = fuaNv;
    }

    public int getLogicalBlockAddress()
    {
        return logicalBlockAddress;
    }

    public void setLogicalBlockAddress(int logicalBlockAddress)
    {
        this.logicalBlockAddress = logicalBlockAddress;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public int getTransferLength() {
        return transferLength;
    }

    public void setTransferLength(int transferLength) {
        this.transferLength = transferLength;
    }

    public byte getControl() {
        return control;
    }

    public void setControl(byte control) {
        this.control = control;
    }
}
