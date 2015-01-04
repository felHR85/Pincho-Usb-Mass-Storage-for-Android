package com.felhr.usbmassstorageforandroid.bulkonly;

import commandwrappers.CommandStatusWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 11/12/14.
 */
public interface BulkOnlyStatusInterface
{
    public void onOperationStarted(boolean status);
    public void onOperationCompleted(CommandStatusWrapper csw);
    public void onDataToHost(byte[] data);
}
