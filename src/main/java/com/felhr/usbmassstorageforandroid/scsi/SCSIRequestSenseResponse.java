package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;
import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

import java.util.Arrays;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 15/12/14.
 */
public class SCSIRequestSenseResponse extends SCSIResponse
{
    private boolean valid;
    private int responseCode;
    private boolean fileMark;
    private boolean eom;
    private boolean ili;
    private int senseKey;
    private int information;
    private int additionalSenseLength;
    private int commandSpecificInformation;
    private int additionalSenseCode;
    private int additionalSenseCodeCodeQualifier;
    private int fieldReplaceableUnitCode;
    private boolean sksv;
    private int senseKeySpecific;
    private byte[] additionalSenseBytes;

    private SCSIRequestSenseResponse()
    {

    }

    public static SCSIRequestSenseResponse getResponse(byte[] data)
    {
        Log.i("Buffer state", "Data to host: " + HexUtil.hexToString(data));

        SCSIRequestSenseResponse response = new SCSIRequestSenseResponse();
        response.valid = (data[0] & (1 << 7)) == (1 << 7);

        data[0] &= ~(1 << 7);
        response.responseCode = data[0];

        response.fileMark = (data[2] & (1 << 7)) == (1 << 7);
        response.eom = (data[2] & (1 << 6)) == (1 << 6);
        response.ili = (data[2] & (1 << 5)) == (1 << 5);

        data[2] &= ~(1 << 7);
        data[2] &= ~(1 << 6);
        data[2] &= ~(1 << 5);
        data[2] &= ~(1 << 4);
        response.senseKey = data[2];

        response.information = (data[6] << 24) + (data[5] << 16) + (data[4] << 8) + data[3];

        response.additionalSenseLength = data[7];

        response.commandSpecificInformation = (data[11] << 24) + (data[10] << 16) + (data[9] << 8) + data[8];

        response.additionalSenseCode = data[12];

        response.additionalSenseCodeCodeQualifier = data[13];

        response.fieldReplaceableUnitCode = data[14];

        response.sksv = (data[15] & (1 << 7)) == (1 << 7);

        data[15] &= ~(1 << 7);
        response.senseKeySpecific = (data[17] << 16) + (data[16] << 8) + data[15];

        try
        {
            response.additionalSenseBytes = Arrays.copyOfRange(data, 18, data.length);
        }catch (IllegalArgumentException e)
        {
            response.additionalSenseBytes = new byte[]{};
            e.printStackTrace();
        }catch (ArrayIndexOutOfBoundsException e)
        {
            response.additionalSenseBytes = new byte[]{};
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        bundle.putString("valid", String.valueOf(valid));
        bundle.putString("responseCode", String.valueOf(responseCode));
        bundle.putString("fileMark", String.valueOf(fileMark));
        bundle.putString("eom", String.valueOf(eom));
        bundle.putString("ili", String.valueOf(ili));
        bundle.putString("senseKey", String.valueOf(senseKey));
        bundle.putString("information", String.valueOf(information));
        bundle.putString("additionalSenseLength", String.valueOf(additionalSenseLength));
        bundle.putString("commandSpecificInformation", String.valueOf(commandSpecificInformation));
        bundle.putString("additionalSenseCode", String.valueOf(additionalSenseCode));
        bundle.putString("additionalSenseCodeCodeQualifier", String.valueOf(additionalSenseCodeCodeQualifier));
        bundle.putString("fieldReplaceableUnitCode", String.valueOf(fieldReplaceableUnitCode));
        bundle.putString("sksv", String.valueOf(sksv));
        bundle.putString("senseKeySpecific", String.valueOf(senseKeySpecific));
        return bundle;
    }

    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public int getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }

    public boolean isFileMark()
    {
        return fileMark;
    }

    public void setFileMark(boolean fileMark)
    {
        this.fileMark = fileMark;
    }

    public boolean isEom()
    {
        return eom;
    }

    public void setEom(boolean eom)
    {
        this.eom = eom;
    }

    public boolean isIli()
    {
        return ili;
    }

    public void setIli(boolean ili)
    {
        this.ili = ili;
    }

    public int getSenseKey()
    {
        return senseKey;
    }

    public void setSenseKey(int senseKey)
    {
        this.senseKey = senseKey;
    }

    public int getInformation()
    {
        return information;
    }

    public void setInformation(int information)
    {
        this.information = information;
    }

    public int getAdditionalSenseLength()
    {
        return additionalSenseLength;
    }

    public void setAdditionalSenseLength(int additionalSenseLength)
    {
        this.additionalSenseLength = additionalSenseLength;
    }

    public int getCommandSpecificInformation()
    {
        return commandSpecificInformation;
    }

    public void setCommandSpecificInformation(int commandSpecificInformation)
    {
        this.commandSpecificInformation = commandSpecificInformation;
    }

    public int getAdditionalSenseCode()
    {
        return additionalSenseCode;
    }

    public void setAdditionalSenseCode(int additionalSenseCode)
    {
        this.additionalSenseCode = additionalSenseCode;
    }

    public int getAdditionalSenseCodeCodeQualifier()
    {
        return additionalSenseCodeCodeQualifier;
    }

    public void setAdditionalSenseCodeCodeQualifier(int additionalSenseCodeCodeQualifier)
    {
        this.additionalSenseCodeCodeQualifier = additionalSenseCodeCodeQualifier;
    }

    public int getFieldReplaceableUnitCode()
    {
        return fieldReplaceableUnitCode;
    }

    public void setFieldReplaceableUnitCode(int fieldReplaceableUnitCode)
    {
        this.fieldReplaceableUnitCode = fieldReplaceableUnitCode;
    }

    public boolean isSksv()
    {
        return sksv;
    }

    public void setSksv(boolean sksv)
    {
        this.sksv = sksv;
    }

    public int getSenseKeySpecific()
    {
        return senseKeySpecific;
    }

    public void setSenseKeySpecific(int senseKeySpecific)
    {
        this.senseKeySpecific = senseKeySpecific;
    }

    public byte[] getAdditionalSenseBytes()
    {
        return additionalSenseBytes;
    }

    public void setAdditionalSenseBytes(byte[] additionalSenseBytes)
    {
        this.additionalSenseBytes = additionalSenseBytes;
    }
}
