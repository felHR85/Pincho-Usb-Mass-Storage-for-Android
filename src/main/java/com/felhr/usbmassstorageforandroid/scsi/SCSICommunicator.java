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
    private SCSIInterface scsiInterfaceCallback;
    private BulkOnlyCommunicator communicator;
    private SCSICommandBuffer buffer;
    private SCSICommandHandler commandHandler;

    /*
        Read10 response can be greater than a sector (512 bytes)
        In order to do not send 512 bytes packets to the upper layers
        currentRead10Response will append all packets and will send them altogether.
     */
    private SCSIRead10Response currentRead10Response;
    private int lengthResponse;

    public SCSICommunicator(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.communicator = new BulkOnlyCommunicator(mDevice, mConnection);
        this.buffer = new SCSICommandBuffer();
        this.commandHandler = new SCSICommandHandler();
        this.commandHandler.start();
    }

    public boolean openSCSICommunicator(SCSIInterface scsiInterfaceCallback)
    {
        this.scsiInterfaceCallback = scsiInterfaceCallback;
        return communicator.startBulkOnly(mCallback);
    }

    public void closeSCSICommunicator()
    {
        commandHandler.stopHandler();
    }

    public void reset()
    {
        communicator.reset();
    }

    public void resetRecovery()
    {
        communicator.resetRecovery();
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
        lengthResponse = transferLength * 512;
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

    public void modeSense10(boolean llbaa, boolean dbd, int pc,
                            int pageCode, int subPageCode, int allocationLength)
    {
        SCSIModeSense10 modeSense10 = new SCSIModeSense10(llbaa, dbd, pc, pageCode, subPageCode, allocationLength);
        buffer.putCommand(modeSense10);
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

    public void preventAllowRemoval(int lun, boolean prevent)
    {
        SCSIPreventAllowRemoval preventAllowRemoval = new SCSIPreventAllowRemoval(lun, prevent);
        buffer.putCommand(preventAllowRemoval);
    }

    private BulkOnlyStatusInterface mCallback = new BulkOnlyStatusInterface()
    {
        @Override
        public void onOperationStarted(boolean status)
        {
            scsiInterfaceCallback.onSCSIOperationStarted(status);
        }

        @Override
        public void onOperationCompleted(CommandStatusWrapper csw)
        {
            if(csw.getbCSWStatus() == 0x00 && currentRead10Response != null)
            {
                scsiInterfaceCallback.onSCSIDataReceived(currentRead10Response);
                currentRead10Response = null;
            }

            if(csw.getbCSWStatus() == 0x02)
              communicator.resetRecovery();

            scsiInterfaceCallback.onSCSIOperationCompleted((int) csw.getbCSWStatus(), csw.getdCSWDataResidue());
            buffer.goAhead();
        }

        @Override
        public void onDataToHost(byte[] data)
        {
            SCSICommand lastCommand = commandHandler.getLastSCSICommand();
            SCSIResponse response = null;
            if(lastCommand instanceof SCSIInquiry)
            {
                response = SCSIInquiryResponse.getResponse(data);
                scsiInterfaceCallback.onSCSIDataReceived(response);
            }else if(lastCommand instanceof SCSIModeSense10)
            {
                response = SCSIModeSense10Response.getResponse(data);
                scsiInterfaceCallback.onSCSIDataReceived(response);
            }else if(lastCommand instanceof SCSIRead10)
            {
                // This case is different because more than one sector are probably be read
                if(currentRead10Response == null)
                    currentRead10Response = SCSIRead10Response.getResponse(data, lengthResponse);
                else
                    currentRead10Response.addToBuffer(data);
            }else if(lastCommand instanceof SCSIReadCapacity10)
            {
                response = SCSIReadCapacity10Response.getResponse(data);
                scsiInterfaceCallback.onSCSIDataReceived(response);
            }else if(lastCommand instanceof SCSIReportLuns)
            {
                response = SCSIReportLunsResponse.getResponse(data);
                scsiInterfaceCallback.onSCSIDataReceived(response);
            }else if(lastCommand instanceof SCSIRequestSense)
            {
                response = SCSIRequestSenseResponse.getResponse(data);
                scsiInterfaceCallback.onSCSIDataReceived(response);
            }
        }
    };

    private class SCSICommandHandler extends Thread
    {
        private AtomicBoolean keep;

        private SCSICommand lastCommand;

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
                lastCommand = scsiCommand;
                CommandBlockWrapper cbw = scsiCommand.getCbw();
                communicator.sendCbw(cbw, scsiCommand.getDataPhaseBuffer());
            }
        }

        public void stopHandler()
        {
            keep.set(false);
        }

        public SCSICommand getLastSCSICommand()
        {
            return lastCommand;
        }
    }
}