package com.felhr.usbmassstorageforandroid.bulkonlytests;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import com.felhr.usbmassstorageforandroid.bulkonly.UsbFacade;
import com.felhr.usbmassstorageforandroid.bulkonly.UsbFacadeInterface;

import android.hardware.usb.UsbEndpoint;
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

    // Mocked Usb objects
    private UsbDeviceConnection mConnection;
    private UsbDevice mDevice;
    private UsbInterface ifaceMocked;
    private UsbEndpoint mockedInEndpoint;
    private UsbEndpoint mockedOutEndpoint;

    @Before
    public void setUp()
    {
        mConnection = Mockito.mock(UsbDeviceConnection.class);
        mDevice = Mockito.mock(UsbDevice.class);

        // UsbDeviceConnection mocked methods
        Mockito.when(mConnection.claimInterface(Mockito.any(UsbInterface.class), true)).thenReturn(true);
        Mockito.when(mConnection.bulkTransfer(
                Mockito.any(UsbEndpoint.class),
                new byte[]{0x00},2, 500)).thenReturn(1);

        // UsbDevice mocked methods
        Mockito.when(mDevice.getInterfaceCount()).thenReturn(0);

        // UsbInterface Mass storage device, Must be injected using a setter.
        ifaceMocked = Mockito.mock(UsbInterface.class);
        Mockito.when(ifaceMocked.getInterfaceClass()).thenReturn(UsbConstants.USB_CLASS_MASS_STORAGE);
        Mockito.when(ifaceMocked.getInterfaceSubclass()).thenReturn(0x06);
        Mockito.when(ifaceMocked.getInterfaceProtocol()).thenReturn(0x50);
        Mockito.when(ifaceMocked.getEndpointCount()).thenReturn(0);

        // UsbEndpoints IN,OUT. Must be injected using a setter
        mockedInEndpoint = Mockito.mock(UsbEndpoint.class);
        mockedOutEndpoint = Mockito.mock(UsbEndpoint.class);

        // Initialize and inject dependencies
        usbFacade = new UsbFacade(mDevice, mConnection);
        usbFacade.injectInterface(ifaceMocked);
        usbFacade.injectInEndpoint(mockedInEndpoint);
        usbFacade.injectOutEndpoint(mockedOutEndpoint);
        usbFacade.setCallback(mCallback);
    }

    @Test
    public void openDevice()
    {
        assertEquals(true,usbFacade.openDevice());
    }

    @Test
    public void sendCommand()
    {
        //TODO Instantiate again UsbFacade changing bulkTransfer behaviour
        usbFacade.sendCommand(new byte[31]);
    }

    @Test
    public void sendData(byte[] data)
    {
        usbFacade.sendData(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09});
    }

    @Test
    public void requestCsw()
    {
        usbFacade.requestCsw();
    }

    @Test
    public void requestData()
    {

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
