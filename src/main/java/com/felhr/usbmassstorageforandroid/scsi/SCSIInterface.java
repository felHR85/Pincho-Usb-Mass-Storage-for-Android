package com.felhr.usbmassstorageforandroid.scsi;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 12/1/15.
 */
public interface SCSIInterface
{
    public void onSCSIOperationCompleted(int status, int dataResidue);
    public void onSCSIDataReceived(SCSIResponse response);
    public void onSCSIOperationStarted(boolean status);
}
