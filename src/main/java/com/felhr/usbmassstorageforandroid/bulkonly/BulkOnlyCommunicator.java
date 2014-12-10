package com.felhr.usbmassstorageforandroid.bulkonly;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 9/12/14.
 */
public class BulkOnlyCommunicator
{
    private UsbFacade usbFacade;

    public BulkOnlyCommunicator(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.usbFacade = new UsbFacade(mDevice, mConnection);
    }

    public boolean startBulkOnly()
    {
        usbFacade.setCallback(mCallback);
        return usbFacade.openDevice();
    }

    public void sendCbw(CommandBlockWrapper cbw)
    {
        usbFacade.sendCommand(cbw.getCWBuffer());
        //TODO: Request data or cws automatically
    }

    public void sendData(byte[] data)
    {
        usbFacade.sendData(data);
    }

    public boolean reset()
    {
        return usbFacade.reset();
    }

    public int getMaxLun()
    {
        return usbFacade.getMaxLun();
    }

    private UsbFacadeInterface mCallback = new UsbFacadeInterface()
    {
        @Override
        public void cbwResponse(int response)
        {

        }

        @Override
        public void cswData(byte[] data)
        {

        }

        @Override
        public void dataFromHost(int response)
        {

        }

        @Override
        public void dataToHost(byte[] data)
        {

        }
    };

}
