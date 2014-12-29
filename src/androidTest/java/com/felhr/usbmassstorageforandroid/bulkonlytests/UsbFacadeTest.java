package com.felhr.usbmassstorageforandroid.bulkonlytests;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import com.felhr.usbmassstorageforandroid.bulkonly.UsbFacade;
import com.felhr.usbmassstorageforandroid.bulkonly.UsbFacadeInterface;

import android.hardware.usb.UsbEndpoint;
import android.test.InstrumentationTestCase;
import android.util.Log;


import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.Before;


/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 17/12/14.
 */
public class UsbFacadeTest extends InstrumentationTestCase
{
    private UsbFacade usbFacade;

    // Mocked Usb objects
    @Mock
    private UsbDeviceConnection mConnection;
    private UsbDevice mDevice;
    private UsbInterface ifaceMocked;
    private UsbEndpoint mockedInEndpoint;
    private UsbEndpoint mockedOutEndpoint;

    @Before
    public void setUp()
    {
        System.setProperty("dexmaker.dexcache",
                getInstrumentation().getTargetContext().getCacheDir().getPath());
        initUsb(0);
    }

    @Test
    public void testOpenDevice()
    {
        assertEquals(true, usbFacade.openDevice());
    }


    @Test
    public void testSendCommand()
    {
        usbFacade.openDevice();
        waitXtime(1000);
        changeBulkMethod(31);
        usbFacade.sendCommand(new byte[31]);
        waitXtime(1000);
    }


    @Test
    public void testSendData()
    {
        usbFacade.openDevice();
        changeBulkMethod(10);
        usbFacade.sendData(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09});
    }



    @Test
    public void testRequestCsw()
    {
        usbFacade.openDevice();
        waitXtime(1000); // A handler must be declared inside a thread. This prevents a nullPointer
        usbFacade.requestCsw();
    }


    @Test
    public void testRequestData()
    {
        usbFacade.openDevice();
        waitXtime(1000);
        usbFacade.requestData(50);
    }

    private void initUsb(int bulkResponse)
    {
        mConnection = Mockito.mock(UsbDeviceConnection.class);
        mDevice = Mockito.mock(UsbDevice.class);

        // UsbInterface Mass storage device, Must be injected using a setter.
        ifaceMocked = Mockito.mock(UsbInterface.class);
        Mockito.when(ifaceMocked.getInterfaceClass()).thenReturn(UsbConstants.USB_CLASS_MASS_STORAGE);
        Mockito.when(ifaceMocked.getInterfaceSubclass()).thenReturn(0x06);
        Mockito.when(ifaceMocked.getInterfaceProtocol()).thenReturn(0x50);
        Mockito.when(ifaceMocked.getEndpointCount()).thenReturn(0);

        // UsbEndpoints IN,OUT. Must be injected using a setter
        mockedInEndpoint = Mockito.mock(UsbEndpoint.class);
        mockedOutEndpoint = Mockito.mock(UsbEndpoint.class);

        // UsbDeviceConnection mocked methods
        Mockito.when(mConnection.claimInterface(ifaceMocked, true)).thenReturn(true);
        Mockito.when(mConnection.bulkTransfer(Mockito.any(UsbEndpoint.class), Mockito.any(byte[].class) ,Mockito.anyInt(), Mockito.anyInt())).thenReturn(bulkResponse);

        // UsbDevice mocked methods
        Mockito.when(mDevice.getInterfaceCount()).thenReturn(1);


        // Initialize and inject dependencies
        usbFacade = new UsbFacade(mDevice, mConnection);
        usbFacade.setCallback(mCallback);
        usbFacade.injectInterface(ifaceMocked);
        usbFacade.injectInEndpoint(mockedInEndpoint);
        usbFacade.injectOutEndpoint(mockedOutEndpoint);
    }

    private void changeBulkMethod(int response)
    {
        Mockito.when(mConnection.bulkTransfer(Mockito.any(UsbEndpoint.class), Mockito.any(byte[].class) , Mockito.anyInt(), Mockito.anyInt())).thenReturn(response);
    }

    private synchronized void waitXtime(long millis)
    {
        try
        {
            wait(millis);
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private UsbFacadeInterface mCallback = new UsbFacadeInterface()
    {
        @Override
        public void cbwResponse(int response)
        {
            Log.d("UsbFacadeTest", "cbwResponse: " + String.valueOf(response));

        }

        @Override
        public void cswData(byte[] data)
        {
            Log.d("UsbFacadeTest", "cswData: " + String.valueOf(data.length));
        }

        @Override
        public void dataFromHost(int response)
        {
            Log.d("UsbFacadeTest", "Data from Host response: " + String.valueOf(response));
        }

        @Override
        public void dataToHost(byte[] data)
        {
            Log.d("UsbFacadeTest", "Length buffer: " + String.valueOf(data.length));
        }
    };
}
