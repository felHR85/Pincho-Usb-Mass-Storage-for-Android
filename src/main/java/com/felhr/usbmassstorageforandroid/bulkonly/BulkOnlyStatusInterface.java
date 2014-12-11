package com.felhr.usbmassstorageforandroid.bulkonly;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 11/12/14.
 */
public interface BulkOnlyStatusInterface
{
    public void onOperationStarted();
    public void onOperationCompleted(byte[] data);
}
