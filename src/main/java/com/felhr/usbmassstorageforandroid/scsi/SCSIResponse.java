package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 12/12/14.
 */
public abstract class SCSIResponse
{
    public  SCSIResponse()
    {

    }

    public abstract Bundle getReadableResponse();
}
