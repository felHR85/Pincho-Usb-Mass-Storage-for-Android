package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIWrite10 extends SCSICommand
{
    public static byte WRITE10_OPERATION_CODE = 0x2a;

    private int wrProtect;
    private boolean dpo;
    private boolean fua;
    private boolean fuaNv;
    private int logicalBlockAddress;
    private int groupNumber;
    private int transferLength;
    private int control;

    private byte[] dataBuffer;

    public SCSIWrite10(int wrProtect, boolean dpo, boolean fua,
                       boolean fuaNv, int logicalBlockAddress, int groupNumber,
                       int transferLength)
    {
        this.dataTransportPhase = true;
        this.direction = 2;
        this.wrProtect = wrProtect;
        this.dpo = dpo;
        this.fua = fua;
        this.fuaNv = fuaNv;
        this.logicalBlockAddress = logicalBlockAddress;
        this.groupNumber = groupNumber;
        this.transferLength = transferLength;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(WRITE10_OPERATION_CODE);
        byte firstByte = 0x00;
        switch(wrProtect)
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

        if(dpo)
            firstByte |= (1 << 4);
        if(fua)
            firstByte |= (1 << 3);
        if(fuaNv)
            firstByte |= (1 << 1);

        buffer.put(firstByte);
        buffer.putInt(logicalBlockAddress);

        buffer.putInt(groupNumber);
        return buffer.array();
    }

    public void setDataBuffer(byte[] buffer)
    {
        this.dataBuffer = buffer;
    }

    public byte[] getDataBuffer()
    {
        return dataBuffer;
    }
}
