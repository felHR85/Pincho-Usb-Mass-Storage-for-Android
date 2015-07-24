package com.felhr.usbmassstorageforandroid.bulkonly;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.felhr.usbmassstorageforandroid.utilities.EndianessUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import commandwrappers.CommandBlockWrapper;
import commandwrappers.CommandWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 9/12/14.
 */
public class UsbFacade
{
    private final int USB_ENDPOINT_LENGTH = 512;
    private int USB_IN_BUFFER_LENGTH;

    private static final int USB_TIMEOUT = 0;

    private static final int CBW_TRANSPORT = 0;
    private static final int DATA_FROM_HOST = 1;

    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbInterface massStorageInterface;

    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;

    private DataOutThread dataOutThread;
    private DataInThread dataInThread;
    private Handler outHandler;

    private UsbFacadeInterface facadeInterface;

    public UsbFacade(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.mDevice = mDevice;
        this.mConnection = mConnection;
        USB_IN_BUFFER_LENGTH = USB_ENDPOINT_LENGTH;
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
            if(massStorageInterface == null) // Silly check only meaningful when testing
                massStorageInterface = mDevice.getInterface(i);

            if(massStorageInterface.getInterfaceClass() == UsbConstants.USB_CLASS_MASS_STORAGE && massStorageInterface.getInterfaceSubclass() == 0x06
                    && massStorageInterface.getInterfaceProtocol() == 0x50)
            {
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

                        while(outHandler == null)
                        {
                            //Busy waiting to avoid outHandler being null :(
                        }

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

    /*
        http://www.beyondlogic.org/usbnutshell/usb6.shtml
     */
    public boolean clearFeatureIN()
    {
        return mConnection.controlTransfer(0x02, 0x01, 0x00, 0x00, null, 0, USB_TIMEOUT) == 0;
    }

    public boolean clearFeatureOUT()
    {
        return mConnection.controlTransfer(0x02, 0x01, 0x00, 0x81, null, 0, USB_TIMEOUT) == 0;
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

    public void sendCommand(byte[] cbwBuffer, byte[] data)
    {
        USB_IN_BUFFER_LENGTH = 13;
        dataInThread.setBuffer(USB_IN_BUFFER_LENGTH);
        outHandler.obtainMessage(CBW_TRANSPORT, cbwBuffer).sendToTarget();
        if(data != null)
        {
            outHandler.obtainMessage(DATA_FROM_HOST, data).sendToTarget();
        }
    }

    /*
        sendCommand when it is necessary to perform a operation with IN data
     */
    public void sendCommand(byte[] cbwBuffer, int dataLength)
    {
        USB_IN_BUFFER_LENGTH = dataLength;
        dataInThread.setBuffer(USB_IN_BUFFER_LENGTH);
        outHandler.obtainMessage(CBW_TRANSPORT, cbwBuffer).sendToTarget();
    }

    public void close()
    {
        outHandler.getLooper().quit();
    }

    // Setter Injectors for Testing
    public void injectInterface(UsbInterface usbInterface)
    {
        this.massStorageInterface = usbInterface;
    }

    public void injectInEndpoint(UsbEndpoint inEndpoint)
    {
        this.inEndpoint = inEndpoint;
    }

    public void injectOutEndpoint(UsbEndpoint outEndpoint)
    {
        this.outEndpoint = outEndpoint;
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
        private byte[] buffer;
        private AtomicBoolean keep;
        private AtomicBoolean waiting;

        public DataInThread()
        {
            this.buffer = new byte[USB_ENDPOINT_LENGTH];
            this.keep = new AtomicBoolean(true);
            this.waiting = new AtomicBoolean(true);
        }

        @Override
        public void run()
        {
            waitTillBufferSet();
            while(keep.get())
            {
                int response = mConnection.bulkTransfer(inEndpoint, buffer, USB_IN_BUFFER_LENGTH, 0);

                if(response > 0)
                {
                    byte[] receivedData = new byte[response];
                    System.arraycopy(buffer, 0, receivedData, 0, response);
                    int cswSignature = 0;
                    if(response == CommandWrapper.CBS_SIZE)
                        cswSignature = EndianessUtil.swapEndianess(
                                ByteBuffer.wrap(Arrays.copyOfRange(receivedData, 0, 4)).getInt());

                    if(receivedData.length == CommandWrapper.CBS_SIZE && cswSignature == CommandBlockWrapper.CBS_SIGNATURE) // It is a CSW
                    {
                        if(facadeInterface != null)
                            facadeInterface.cswData(receivedData);
                        waitTillBufferSet();
                    }else // It is data to host
                    {
                        if(facadeInterface != null)
                            facadeInterface.dataToHost(receivedData);
                    }
                }
            }
        }

        public synchronized void setBuffer(int bufferLength)
        {
            if(bufferLength != buffer.length)
                this.buffer = new byte[bufferLength];
            waiting.set(false);
            notify();
        }

        private synchronized void waitTillBufferSet()
        {
            while(waiting.get())
            {
                try
                {
                    wait();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            waiting.set(true);
        }

        public void stopThread()
        {
            keep.set(false);
        }
    }
}