package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 23/12/14.
 */
public class SCSIReportLunsResponse extends SCSIResponse
{
    private int lunListLength;
    private long[] lunList;

    private SCSIReportLunsResponse()
    {

    }

    public static SCSIReportLunsResponse getResponse(byte[] data)
    {
        SCSIReportLunsResponse response = new SCSIReportLunsResponse();
        response.lunListLength = (data[0] << 24) + (data[1] << 16) + (data[2] << 8) + data[3];

        int length = response.lunListLength;
        response.lunList = new long[length];

        for(int i=0;i<=length;i++)
        {
            int j = 8 * i;
            response.lunList[i] = (data[8+j] << 56) + (data[9+j] << 48) +
                    (data[10+j] << 40) + (data[11+j] << 32) + (data[12+j] << 24) +
                    (data[13+j] << 16) + (data[14+j] << 8) + data[15+j];

        }
        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        // TODO
        return bundle;
    }

    public int getLunListLength()
    {
        return lunListLength;
    }

    public void setLunListLength(int lunListLength)
    {
        this.lunListLength = lunListLength;
    }

    public long[] getLunList()
    {
        return lunList;
    }

    public void setLunList(long[] lunList)
    {
        this.lunList = lunList;
    }
}
