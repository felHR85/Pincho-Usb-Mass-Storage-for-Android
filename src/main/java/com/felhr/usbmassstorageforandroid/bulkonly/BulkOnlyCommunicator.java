package com.felhr.usbmassstorageforandroid.bulkonly;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import java.util.concurrent.atomic.AtomicBoolean;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 9/12/14.
 */
public class BulkOnlyCommunicator
{
    private UsbFacade usbFacade;

    private AtomicBoolean flagDataIN;
    private AtomicBoolean flagDataOUT;
    private byte[] dataBufferIn;
    private byte[] dataBufferOut;

    public BulkOnlyCommunicator(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.usbFacade = new UsbFacade(mDevice, mConnection);
        this.flagDataIN = new AtomicBoolean(false);
        this.flagDataOUT = new AtomicBoolean(false);
    }

    public boolean startBulkOnly()
    {
        usbFacade.setCallback(mCallback);
        return usbFacade.openDevice();
    }

    public void sendCbw(CommandBlockWrapper cbw, byte[] data)
    {
        int dataLength = cbw.getdCBWDataLength();
        if(data != null && dataLength > 0)
        {
            flagDataOUT.set(true);
            dataBufferOut = data;

        }else if(data == null && dataLength > 0)
        {
            flagDataIN.set(true);
        }else
        {
            flagDataOUT.set(false);
            flagDataIN.set(false);
        }
        usbFacade.sendCommand(cbw.getCWBuffer());
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
            // TODO: Send notification to upper layer (SCSI prototocol) through other callback (BulkOnly STARTED)
            if(response > 0 && flagDataOUT.get())
                usbFacade.sendData(dataBufferOut);
            else if(response > 0 && flagDataIN.get())
                usbFacade.requestData();
            else
                usbFacade.requestCsw();
        }

        @Override
        public void cswData(byte[] data)
        {
           // TODO: Send notification to upper layer (SCSI prototocol) through other callback (BulkOnly FINISHED)
        }

        @Override
        public void dataFromHost(int response)
        {
            if(response > 0)
                usbFacade.requestCsw();
        }

        @Override
        public void dataToHost(byte[] data)
        {
            if(data != null)
            {
                dataBufferIn = data;
                usbFacade.requestCsw();
            }
        }
    };

}
