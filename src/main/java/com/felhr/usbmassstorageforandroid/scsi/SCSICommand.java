package com.felhr.usbmassstorageforandroid.scsi;

import java.util.Arrays;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 12/12/14.
 */
public abstract class SCSICommand
{
    protected byte[] dataBuffer;
    protected boolean dataTransportPhase;
    protected int direction; // 0:NONE 1:IN(to host) 2:OUT(from host)

    public SCSICommand()
    {

    }

    /*
        Serialize a SCSI command object into a stream of bytes
     */
    public abstract byte[] getSCSICommandBuffer();

    /*
        Get a Command Block wrapper from a SCSI command
     */
    public abstract CommandBlockWrapper getCbw();

    /*
        Set data from the data-phase buffer
     */
    public abstract void setDataPhaseBuffer(byte[] data);

    /*
        Get data from the data-phase buffer
     */
    public abstract byte[] getDataPhaseBuffer();



    public boolean hasDataPhase() // This will be called by the SCSI command handler
    {
        return dataTransportPhase;
    }

    public int getDirection() // This will be called by the SCSI command handler
    {
        return direction;
    }

    protected byte[] convertToByte(int number, int numberOfBytes)
    {
        byte[] buffer;
        if(numberOfBytes == 1)
        {
            buffer = new byte[1];
            buffer[0] = (byte) number;
        }else if(numberOfBytes == 2)
        {
            buffer = new byte[2];
            buffer[1] = (byte) (number & 0xff);
            buffer[0] = (byte) (number >> 8 & 0xff);
        }else if(numberOfBytes == 3)
        {
            buffer = new byte[3];
            buffer[2] = (byte) (number & 0xff);
            buffer[1] = (byte) (number >> 8 & 0xff);
            buffer[0] = (byte) (number >> 16 & 0xff);
        }else if(numberOfBytes == 4)
        {
            buffer = new byte[4];
            buffer[3] = (byte) (number & 0xff);
            buffer[2] = (byte) (number >> 8 & 0xff);
            buffer[1] = (byte) (number >> 16 & 0xff);
            buffer[0] = (byte) (number >> 24 & 0xff);

        }else
        {
            buffer = null;
        }
        return buffer;
    }

    protected byte[] getCbwcb(byte[] data)
    {
        if(data.length < 16)
        {
            byte[] dst = new byte[16];
            System.arraycopy(data, 0, dst, 0, data.length);
            return dst;
        }else if(data.length == 16)
        {
            return data;
        }else if(data.length > 16)
        {
            return null;
        }
        return null;
    }

}
