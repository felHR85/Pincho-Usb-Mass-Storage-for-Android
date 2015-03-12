package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIRead10Response extends SCSIResponse
{
    private byte[] readBuffer;
    private int pointer;

    private SCSIRead10Response(int lengthResponse)
    {
        this.readBuffer = new byte[lengthResponse];
        this.pointer = 0;
    }

    public static SCSIRead10Response getResponse(byte[] data, int lengthResponse)
    {
        SCSIRead10Response response = new SCSIRead10Response(lengthResponse);
        response.addToBuffer(data);
        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        bundle.putString("readBuffer", HexUtil.hexToString(readBuffer));
        return bundle;
    }

    public void addToBuffer(byte[] data)
    {
        System.arraycopy(data, 0, readBuffer, pointer, data.length);
        pointer += data.length;
    }

    public byte[] getBuffer()
    {
        return readBuffer;
    }

}
