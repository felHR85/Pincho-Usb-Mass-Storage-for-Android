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

        mockedUsbFacade = new UsbFacade(Mockito.any(UsbDevice.class), Mockito.any(UsbDeviceConnection.class));

        comm = new BulkOnlyCommunicator(Mockito.any(UsbDevice.class), Mockito.any(UsbDeviceConnection.class));
        comm.injectUsbFacade(mockedUsbFacade);
    }


    @Test
    public void startBulkOnlyTest()
    {
        assertEquals(true, comm.startBulkOnly(Mockito.any(BulkOnlyStatusInterface.class)));
    }

    @Test
    public void sendCbwTest()
    {

    }


}
