package com.felhr.usbmassstorageforandroid.bulkonly;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 9/12/14.
 */
public class UsbFacade
{

    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbInterface massStorageInterface;

    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;

    public UsbFacade(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.mDevice = mDevice;
        this.mConnection = mConnection;
    }

    public boolean openDevice()
    {
        int index = mDevice.getInterfaceCount();
        for(int i=0;i<=index-1;i++)
        {
            UsbInterface iface = mDevice.getInterface(index);
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
}
