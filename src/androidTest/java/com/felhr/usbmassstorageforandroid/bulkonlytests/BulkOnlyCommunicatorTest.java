package com.felhr.usbmassstorageforandroid.bulkonlytests;


import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.test.InstrumentationTestCase;

import com.felhr.usbmassstorageforandroid.bulkonly.BulkOnlyCommunicator;
import com.felhr.usbmassstorageforandroid.bulkonly.BulkOnlyStatusInterface;
import com.felhr.usbmassstorageforandroid.bulkonly.UsbFacade;
import com.felhr.usbmassstorageforandroid.bulkonly.UsbFacadeInterface;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import commandwrappers.CommandBlockWrapper;
import commandwrappers.CommandStatusWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 17/12/14.
 */
public class BulkOnlyCommunicatorTest extends InstrumentationTestCase
{
    private BulkOnlyCommunicator comm;

    @Mock
    private UsbFacade mockedUsbFacade;

    @Before
    public void setUp()
    {
        System.setProperty("dexmaker.dexcache",
                getInstrumentation().getTargetContext().getCacheDir().getPath());

        mockedUsbFacade = Mockito.mock(UsbFacade.class);

        Mockito.when(mockedUsbFacade.openDevice()).thenReturn(true);
        Mockito.when(mockedUsbFacade.reset()).thenReturn(true);
        Mockito.when(mockedUsbFacade.getMaxLun()).thenReturn(1);

        Mockito.doNothing().when(mockedUsbFacade).setCallback(Mockito.any(UsbFacadeInterface.class));
        Mockito.doNothing().when(mockedUsbFacade).sendCommand(Mockito.any(byte[].class));
        Mockito.doNothing().when(mockedUsbFacade).sendData(Mockito.any(byte[].class));
        Mockito.doNothing().when(mockedUsbFacade).requestCsw();
        Mockito.doNothing().when(mockedUsbFacade).requestData(Mockito.anyInt());
        Mockito.doNothing().when(mockedUsbFacade).close();

        UsbDeviceConnection mConnection = Mockito.mock(UsbDeviceConnection.class);
        UsbDevice mDevice = Mockito.mock(UsbDevice.class);

        comm = new BulkOnlyCommunicator(mDevice,  mConnection);
        comm.injectUsbFacade(mockedUsbFacade);
    }


    @Test
    public void testStartBulkOnly()
    {
        BulkOnlyStatusInterface mockedInterface = Mockito.mock(BulkOnlyStatusInterface.class);
        Mockito.doNothing().when(mockedInterface).onOperationStarted(Mockito.anyBoolean());
        Mockito.doNothing().when(mockedInterface).onOperationCompleted(Mockito.any(CommandStatusWrapper.class));

        assertEquals(true, comm.startBulkOnly(mockedInterface));
    }

    @Test
    public void testSendCbwTest()
    {
        byte[] data = new byte[16];
        CommandBlockWrapper mockedCb = Mockito.mock(CommandBlockWrapper.class);
        Mockito.when(mockedCb.getdCBWDataLength()).thenReturn(16);
        Mockito.when(mockedCb.getCWBuffer()).thenReturn(new byte[31]);
        comm.sendCbw(mockedCb, data);
    }

    @Test
    public void testReset()
    {
        comm.reset();
    }

    @Test
    public void testGetMaxLuns()
    {
        assertEquals(1, comm.getMaxLun());
    }


}
