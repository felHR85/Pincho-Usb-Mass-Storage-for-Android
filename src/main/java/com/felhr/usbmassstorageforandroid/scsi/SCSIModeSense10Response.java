package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;
import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 22/12/14.
 */
public class SCSIModeSense10Response extends SCSIResponse
{
    private int modeDataLength;
    private int mediumType;
    private int deviceSpecificParameter;
    private boolean longLba;
    private int blockDescriptorLength;

    private SCSIModeSense10Response()
    {

    }

    public static SCSIModeSense10Response getResponse(byte[] data)
    {
        Log.i("Buffer state", "Data to host: " + HexUtil.hexToString(data));

        SCSIModeSense10Response response = new SCSIModeSense10Response();
        response.modeDataLength = (data[0] << 8) + (data[1]);
        response.mediumType = data[2];
        response.deviceSpecificParameter = data[3];
        response.longLba = (data[4] & 1) == 1;
        response.blockDescriptorLength = (data[6] << 8) + (data[7]);
        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        bundle.putString("modeDataLength", String.valueOf(modeDataLength));
        bundle.putString("mediumType", String.valueOf(mediumType));
        bundle.putString("deviceSpecificParameter", String.valueOf(deviceSpecificParameter));
        bundle.putString("longLba", String.valueOf(longLba));
        bundle.putString("blockDescriptorLength", String.valueOf(blockDescriptorLength));
        return bundle;
    }

    public int getModeDataLength()
    {
        return modeDataLength;
    }

    public void setModeDataLength(int modeDataLength)
    {
        this.modeDataLength = modeDataLength;
    }

    public int getMediumType()
    {
        return mediumType;
    }

    public void setMediumType(int mediumType)
    {
        this.mediumType = mediumType;
    }

    public int getDeviceSpecificParameter()
    {
        return deviceSpecificParameter;
    }

    public void setDeviceSpecificParameter(int deviceSpecificParameter)
    {
        this.deviceSpecificParameter = deviceSpecificParameter;
    }

    public boolean isLongLba()
    {
        return longLba;
    }

    public void setLongLba(boolean longLba)
    {
        this.longLba = longLba;
    }

    public int getBlockDescriptorLength()
    {
        return blockDescriptorLength;
    }

    public void setBlockDescriptorLength(int blockDescriptorLength)
    {
        this.blockDescriptorLength = blockDescriptorLength;
    }
}
