package com.felhr.usbmassstorageforandroid.bulkonly;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 10/12/14.
 */
public interface UsbFacadeInterface
{
    public void cbwResponse(int response);
    public void cswData(byte[] data);
    public void dataFromHost(int response);
    public void dataToHost(byte[] data);
}
