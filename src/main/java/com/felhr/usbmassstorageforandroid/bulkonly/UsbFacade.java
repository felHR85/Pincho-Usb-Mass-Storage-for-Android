package com.felhr.usbmassstorageforandroid.bulkonly;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import commandwrappers.CommandWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 9/12/14.
 */
public class UsbFacade
{
    private static final int USB_TIMEOUT = 500;
    private static final int USB_TIMEOUT_2 = 5000;

    private static final int CBW_TRANSPORT = 0;
    private static final int DATA_FROM_HOST = 1;
    private static final int DATA_TO_HOST = 3;
    private static final int CBS_TRANSPORT = 4;

    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbInterface massStorageInterface;

    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;

    private DataOutThread dataOutThread;
    private DataInThread dataInThread;
    private Handler outHandler;
    private Handler inHandler;

    private UsbFacadeInterface facadeInterface;

    public UsbFacade(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.mDevice = mDevice;
        this.mConnection = mConnection;
    }

    public void setCallback(UsbFacadeInterface facadeInterface)
    {
        this.facadeInterface = facadeInterface;
    }

    public boolean openDevice()
    {
        int index = mDevice.getInterfaceCount();
        for(int i=0;i<=index-1;i++)
        {
            UsbInterface iface = mDevice.getInterface(i);
            if(iface.getInterfaceClass() == UsbConstants.USB_CLASS_MASS_STORAGE && iface.getInterfaceSubclass() == 0x06
                    && iface.getInterfaceProtocol() == 0x50)
            {
                massStorageInterface = iface;
                if(mConnection.claimInterface(massStorageInterface, true))
                {
                    int endpointCount = massStorageInterface.getEndpointCount();
                    for(int j=0;j<=endpointCount-1;j++)
                    {
                       UsbEndpoint endpoint = massStorageInterface.getEndpoint(j);
                       if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                               && endpoint.getDirection() == UsbConstants.USB_DIR_IN)
                       {
                            inEndpoint = endpoint;
                       }else if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                               && endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
                       {
                           outEndpoint = endpoint;
                       }
                    }

                    if(inEndpoint != null && outEndpoint != null)
                    {
                        dataOutThread = new DataOutThread();
                        dataOutThread.start();
                        dataInThread = new DataInThread();
                        dataInThread.start();
                        return true;
                    }else
                    {
                        return false;
                    }

                }else
                {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean reset()
    {
        return mConnection.controlTransfer(0x21, 0xff, 0x0000, massStorageInterface.getId(), null, 0, USB_TIMEOUT) >= 0;
    }

    public int getMaxLun()
    {
        byte[] buff = new byte[1];
        int status = mConnection.controlTransfer(0xa1, 0xfe, 0x0000, massStorageInterface.getId(), buff, 1, USB_TIMEOUT);
        if(status == 0)
            return (int) buff[0];
        else
            return -1;
    }

    public void sendCommand(byte[] cbwBuffer)
    {
        outHandler.obtainMessage(CBW_TRANSPORT, cbwBuffer).sendToTarget();
    }

    public void sendData(byte[] data)
    {
        outHandler.obtainMessage(DATA_FROM_HOST, data).sendToTarget();
    }

    public void requestCsw()
    {
        inHandler.obtainMessage(CBS_TRANSPORT).sendToTarget();;
    }

    public void requestData(int dataSize)
    {
        inHandler.obtainMessage(DATA_TO_HOST, dataSize).sendToTarget();
    }

    public void close()
    {
        inHandler.getLooper().quit();
        outHandler.getLooper().quit();
    }

    private class DataOutThread extends Thread
    {
        @Override
        public void run()
        {
            Looper.prepare();
            outHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    byte[] buffer = (byte[]) msg.obj;
                    int response = mConnection.bulkTransfer(outEndpoint, buffer, buffer.length, USB_TIMEOUT);
                    switch(msg.what)
                    {
                        case CBW_TRANSPORT:
                            if(facadeInterface != null)
                                facadeInterface.cbwResponse(response);
                            break;
                        case DATA_FROM_HOST:
                            if(facadeInterface != null)
                                facadeInterface.dataFromHost(response);
                            break;
                    }
                }
            };
            Looper.loop();
        }

    }

    private class DataInThread extends Thread
    {
       @Override
        public  void run()
        {
           Looper.prepare();
           inHandler = new Handler()
           {
               @Override
               public void handleMessage(Message msg)
               {
                    switch(msg.what)
                    {
                        case CBS_TRANSPORT:
                            byte[] buffer = new byte[CommandWrapper.CBS_SIZE];
                            mConnection.bulkTransfer(inEndpoint, buffer, CommandWrapper.CBS_SIZE, USB_TIMEOUT);
                            if(facadeInterface != null)
                                facadeInterface.cswData(buffer);
                            break;
                        case DATA_TO_HOST:
                            int bufferLength = msg.arg1;
                            byte[] dataBuffer = new byte[bufferLength];
                            mConnection.bulkTransfer(inEndpoint, dataBuffer, bufferLength, USB_TIMEOUT_2);
                            if(facadeInterface != null)
                                facadeInterface.dataToHost(dataBuffer);
                            break;
                    }
               }
           };
           Looper.loop();
       }
    }
}
