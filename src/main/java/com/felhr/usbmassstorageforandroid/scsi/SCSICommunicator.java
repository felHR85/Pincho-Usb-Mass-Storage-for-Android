package com.felhr.usbmassstorageforandroid.scsi;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.felhr.usbmassstorageforandroid.bulkonly.BulkOnlyCommunicator;
import com.felhr.usbmassstorageforandroid.bulkonly.BulkOnlyStatusInterface;

import java.util.concurrent.atomic.AtomicBoolean;

import commandwrappers.CommandBlockWrapper;
import commandwrappers.CommandStatusWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 11/12/14.
 */
public class SCSICommunicator
{
    private BulkOnlyCommunicator communicator;
    private CommandBlockBuffer buffer;
    private SCSICommandHandler commandHandler;

    public SCSICommunicator(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.communicator = new BulkOnlyCommunicator(mDevice, mConnection);
        this.buffer = new CommandBlockBuffer();
        this.commandHandler = new SCSICommandHandler();
        this.commandHandler.start();
    }

    public boolean openSCSICommunicator()
    {
        return communicator.startBulkOnly(mCallback);
    }

    public void inquiry(boolean evpd, int pageCode, int allocationLength)
    {
        SCSIInquiry inquiry = new SCSIInquiry(evpd, pageCode, allocationLength);

        byte[] rawInstruction = inquiry.getSCSICommandBuffer();
        int dCBWDataTransferLength = allocationLength;

        byte bmCBWFlags = 0x00;

        bmCBWFlags |= (1 << 7); // From device to host (Inquiry Instruction)

        byte bCBWLUN = 0x00; // Check this!!!
        byte bCBWCBLength = (byte) (rawInstruction.length);

        CommandBlockWrapper cbw = new CommandBlockWrapper(dCBWDataTransferLength, bmCBWFlags, bCBWLUN, bCBWCBLength);
        buffer.putCommand(cbw);
    }

    public void readCapacity10()
    {
        //TODO
    }

    public void read10()
    {
        //TODO
    }

    public void requestSense()
    {
        //TODO
    }

    public void testUnitReady()
    {
        //TODO
    }

    public void write10()
    {
        //TODO
    }

    public void modeSelect10(boolean pageFormat, boolean savePages, int parameterListLength)
    {
        SCSIModeSelect10 modeSelect10 = new SCSIModeSelect10(pageFormat, savePages, parameterListLength);

        byte[] rawInstruction = modeSelect10.getSCSICommandBuffer();
        int dCBWDataTransferLength = parameterListLength; // MODE_SELECT10 Data phase OUT Endpoint???

        byte bmCBWFlags = 0x00;

        byte bCBWLUN = 0x00; // Check this!!!
        byte bCBWCBLength = (byte) (rawInstruction.length);

        CommandBlockWrapper cbw = new CommandBlockWrapper(dCBWDataTransferLength, bmCBWFlags, bCBWLUN, bCBWCBLength);
        buffer.putCommand(cbw);
    }

    public void formatUnit(boolean fmtpinfo, boolean rtoReq, boolean longList,
                           boolean fmtData, boolean cmplst, int defectListFormat)
    {
        SCSIFormatUnit formatUnit = new SCSIFormatUnit(fmtpinfo, rtoReq, longList,
                fmtData, cmplst, defectListFormat);

        byte[] rawInstruction = formatUnit.getSCSICommandBuffer();
        int dCBWDataTransferLength = 0;

        byte bmCBWFlags = 0x00;

        byte bCBWLUN = 0x00; // Check this!!!
        byte bCBWCBLength = (byte) (rawInstruction.length);

        CommandBlockWrapper cbw = new CommandBlockWrapper(dCBWDataTransferLength, bmCBWFlags, bCBWLUN, bCBWCBLength);
        buffer.putCommand(cbw);
    }

    private BulkOnlyStatusInterface mCallback = new BulkOnlyStatusInterface()
    {
        @Override
        public void onOperationStarted(boolean status)
        {

        }

        @Override
        public void onOperationCompleted(CommandStatusWrapper csw)
        {
            // Check Status
            buffer.goAhead();
        }

        @Override
        public void onDataToHost(byte[] data)
        {

        }
    };

    private class SCSICommandHandler extends Thread
    {
        private AtomicBoolean keep;

        public SCSICommandHandler()
        {
            this.keep = new AtomicBoolean(true);
        }

        @Override
        public void run()
        {
            while(keep.get())
            {
                CommandBlockWrapper cbw = buffer.getCommand();
                communicator.sendCbw(cbw, null); // NOT ALWAYS NULL!!!
            }
        }

        public void stopHandler()
        {
            keep.set(false);
        }
    }



}


