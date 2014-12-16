package com.felhr.usbmassstorageforandroid.scsi;

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
        SCSIReadCapacity10Response response = new SCSIReadCapacity10Response();
        response.returnedLogicalBlockAddress = (data[0] << 24) + (data[1] << 16) + (data[2] << 8) + data[3];
        response.blockLength = (data[4] << 24) + (data[5] << 16) + (data[6] << 8) + data[7];
        return response;
    }
}
