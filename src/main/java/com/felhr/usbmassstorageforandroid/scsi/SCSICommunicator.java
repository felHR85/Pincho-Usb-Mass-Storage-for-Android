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
    private SCSICommandBuffer buffer;
    private SCSICommandHandler commandHandler;

    public SCSICommunicator(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.communicator = new BulkOnlyCommunicator(mDevice, mConnection);
        this.buffer = new SCSICommandBuffer();
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
        buffer.putCommand(inquiry);
    }

    public void readCapacity10(int logicalBlockAddress, boolean pmi)
    {
        SCSIReadCapacity10 readCapacity10 = new SCSIReadCapacity10(logicalBlockAddress, pmi);
        buffer.putCommand(readCapacity10);
    }

    public void read10(int rdProtect, boolean dpo, boolean fua,
                       boolean fuaNv, int logicalBlockAddress,
                       int groupNumber, int transferLength)
    {
        SCSIRead10 read10 = new SCSIRead10(rdProtect, dpo, fua,
                fuaNv, logicalBlockAddress, groupNumber,
                transferLength);

        buffer.putCommand(read10);
    }

    public void requestSense(boolean desc, int allocationLength)
    {
        SCSIRequestSense requestSense = new SCSIRequestSense(desc, allocationLength);
        buffer.putCommand(requestSense);
    }

    public void testUnitReady()
    {
        SCSITestUnitReady testUnitReady = new SCSITestUnitReady();
        buffer.putCommand(testUnitReady);
    }

    public void write10(int wrProtect, boolean dpo, boolean fua,
                        boolean fuaNv, int logicalBlockAddress, int groupNumber,
                        int transferLength, byte[] data)
    {
       SCSIWrite10 write10 = new SCSIWrite10(wrProtect, dpo, fua,
               fuaNv, logicalBlockAddress, groupNumber,
               transferLength);

        write10.setDataPhaseBuffer(data);
        buffer.putCommand(write10);
    }

    public void modeSelect10(boolean pageFormat, boolean savePages, int parameterListLength)
    {
        SCSIModeSelect10 modeSelect10 = new SCSIModeSelect10(pageFormat, savePages, parameterListLength);
        buffer.putCommand(modeSelect10);
    }

    public void formatUnit(boolean fmtpinfo, boolean rtoReq, boolean longList,
                           boolean fmtData, boolean cmplst, int defectListFormat)
    {
        SCSIFormatUnit formatUnit = new SCSIFormatUnit(fmtpinfo, rtoReq, longList,
                fmtData, cmplst, defectListFormat);

        buffer.putCommand(formatUnit);
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
                SCSICommand scsiCommand = buffer.getCommand();
                CommandBlockWrapper cbw = scsiCommand.getCbw();
                communicator.sendCbw(cbw, scsiCommand.getDataPhaseBuffer());
            }
        }

        public void stopHandler()
        {
            keep.set(false);
        }
    }
}