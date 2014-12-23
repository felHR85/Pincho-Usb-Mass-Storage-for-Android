package com.felhr.usbmassstorageforandroid.scsi;

import java.nio.ByteBuffer;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 22/12/14.
 */
public class SCSIPreventAllowRemoval extends SCSICommand
{
    public static final byte PREVENTALLOWREMOVAL_OPERATION_CODE = 0x1e;

    private int logicalUnitNumber;
    private boolean prevent;
    private byte control;

    public SCSIPreventAllowRemoval(int logicalUnitNumber, boolean prevent)
    {
        this.dataTransportPhase = false;
        this.direction = 0;
        this.logicalUnitNumber = logicalUnitNumber;
        this.prevent = prevent;
        this.control = 0x00;
    }

    @Override
    public byte[] getSCSICommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put(PREVENTALLOWREMOVAL_OPERATION_CODE);

        byte firstByte = 0x00;
        switch(logicalUnitNumber)
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
                firstByte |= (1 << 4);
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

        buffer.put(firstByte);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);

        byte fourthByte = 0x00;
        if(prevent)
            fourthByte |= 1;
        buffer.put(fourthByte);

        buffer.put(control);

        return buffer.array();
    }
}
