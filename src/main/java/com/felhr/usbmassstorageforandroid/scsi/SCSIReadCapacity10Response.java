package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;
import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIReadCapacity10Response extends SCSIResponse
{
    private int returnedLogicalBlockAddress;
    private int blockLength;

    private SCSIReadCapacity10Response()
    {

    }


    public static SCSIReadCapacity10Response getResponse(byte[] data)
    {
        Log.i("Buffer state", "Data to host: " + HexUtil.hexToString(data));

        SCSIReadCapacity10Response response = new SCSIReadCapacity10Response();
        response.returnedLogicalBlockAddress = (data[0] << 24) + (data[1] << 16) + (data[2] << 8) + data[3];
        response.blockLength = (data[4] << 24) + (data[5] << 16) + (data[6] << 8) + data[7];
        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        bundle.putString("returnedLogicalBlockAddress", String.valueOf(returnedLogicalBlockAddress));
        bundle.putString("blockLength", String.valueOf(blockLength));
        return bundle;
    }

    public int getReturnedLogicalBlockAddress()
    {
        return returnedLogicalBlockAddress;
    }

    public void setReturnedLogicalBlockAddress(int returnedLogicalBlockAddress)
    {
        this.returnedLogicalBlockAddress = returnedLogicalBlockAddress;
    }

    public int getBlockLength()
    {
        return blockLength;
    }

    public void setBlockLength(int blockLength)
    {
        this.blockLength = blockLength;
    }
}
