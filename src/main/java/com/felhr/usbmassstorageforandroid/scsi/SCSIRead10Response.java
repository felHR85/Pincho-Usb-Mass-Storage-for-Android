package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIRead10Response extends SCSIResponse
{
    private byte[] readBuffer;

    private SCSIRead10Response()
    {

    }

    public static SCSIRead10Response getResponse(byte[] data)
    {
        SCSIRead10Response response = new SCSIRead10Response();
        response.readBuffer = data;
        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        // TODO
        return bundle;
    }



    public byte[] getReadBuffer()
    {
        return readBuffer;
    }

    public void setReadBuffer(byte[] readBuffer)
    {
        this.readBuffer = readBuffer;
    }
}
