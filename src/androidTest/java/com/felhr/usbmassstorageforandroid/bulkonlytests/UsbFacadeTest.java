package com.felhr.usbmassstorageforandroid.bulkonlytests;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import com.felhr.usbmassstorageforandroid.bulkonly.UsbFacade;

import android.hardware.usb.UsbEndpoint;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Mockito;
import org.junit.Before;
import static org.junit.Assert.assertEquals;


/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 17/12/14.
 */
public class UsbFacadeTest
{
    private UsbFacade usbFacade;
    private UsbDeviceConnection mConnection;
    private UsbDevice mDevice;

    @Before
    public void setUp()
    {
        mConnection = Mockito.mock(UsbDeviceConnection.class);
        mDevice = Mockito.mock(UsbDevice.class);

        // UsbDeviceConnection mocked methods
        Mockito.when(mConnection.claimInterface(Mockito.any(UsbInterface.class), true)).thenReturn(true);
        Mockito.when(mConnection.bulkTransfer(
                Mockito.any(UsbEndpoint.class),
                new byte[]{0x00},2, 500)).thenReturn(2);

        // UsbDevice mocked methods
        Mockito.when(mDevice.getInterfaceCount()).thenReturn(0);
        // TODO getInterface

        usbFacade = new UsbFacade(mDevice, mConnection);
    }

    @Test
    public void open()
    {

    }
}
