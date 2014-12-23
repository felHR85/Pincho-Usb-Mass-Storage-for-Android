package com.felhr.usbmassstorageforandroid.scsi;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 23/12/14.
 */
public class SCSIReportLunsResponse extends SCSIResponse
{
    private SCSIReportLunsResponse()
    {

    }

    public SCSIReportLunsResponse getResponse(byte[] data)
    {
        SCSIReportLunsResponse response = new SCSIReportLunsResponse();
        // TODO
        return response;
    }
}
