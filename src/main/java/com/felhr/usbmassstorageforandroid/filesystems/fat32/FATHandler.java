package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.felhr.usbmassstorageforandroid.filesystems.MasterBootRecord;
import com.felhr.usbmassstorageforandroid.filesystems.Partition;
import com.felhr.usbmassstorageforandroid.scsi.SCSICommunicator;
import com.felhr.usbmassstorageforandroid.scsi.SCSIInterface;
import com.felhr.usbmassstorageforandroid.scsi.SCSIRead10Response;
import com.felhr.usbmassstorageforandroid.scsi.SCSIResponse;
import com.felhr.usbmassstorageforandroid.utilities.UnsignedUtil;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 20/2/15.
 */
public class FATHandler
{
    private SCSICommunicator comm;
    private Object monitor;
    private SCSIResponse currentResponse;
    private boolean currentStatus;

    private MasterBootRecord mbr;

    //Mounted Partition
    private Partition partition;
    private ReservedRegion reservedRegion;
    private FAT fat;

    public FATHandler(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.comm = new SCSICommunicator(mDevice, mConnection);
        this.comm.openSCSICommunicator(scsiInterface);
        this.monitor = new Object();
    }

    public boolean mount(int partitionIndex)
    {

        testUnitReady();

        if(currentStatus)
            mbr = getMbr();
        else
            return false;

        if(mbr.getPartitions().length >= partitionIndex + 1)
        {
            partition = mbr.getPartitions()[partitionIndex];
            reservedRegion = getReservedRegion();
            // Read root level
        }else
        {
            return false;
        }


        return false;
    }

    private void testUnitReady()
    {
        comm.testUnitReady();
        waitTillNotification();
    }

    private MasterBootRecord getMbr()
    {
        comm.read10(0, false, false, false, UnsignedUtil.ulongToInt(0), 0, 1);
        waitTillNotification();
        if(currentStatus)
        {
            byte[] data = ((SCSIRead10Response) currentResponse).getBuffer();
            return MasterBootRecord.parseMbr(data);
        }else
        {
            return null;
        }
    }

    private ReservedRegion getReservedRegion()
    {
        long lbaPartitionStart = partition.getLbaStart();
        comm.read10(0, false, false, false, UnsignedUtil.ulongToInt(lbaPartitionStart), 0, 1);
        waitTillNotification();
        if(currentStatus)
        {
            byte[] data = ((SCSIRead10Response) currentResponse).getBuffer();
            return ReservedRegion.getReservedRegion(data);
        }else
        {
            return null;
        }

    }

    private long getEntryLBA(long entry)
    {
        long fatLBA = partition.getLbaStart() + reservedRegion.getNumberReservedSectors();
        return fatLBA + (entry / 128);
    }

    private int getEntryBlockIndex(long entry) // range [0-127]
    {
        return ((int) (entry - ((entry / 128) * 128)));
    }

    private int[] getRealIndexes(int entryBlock)
    {
        int[] indexes = new int[4];
        int value = 4 * entryBlock;
        indexes[0] = value;
        indexes[1] = value + 1;
        indexes[2] = value + 2;
        indexes[3] = value + 3;
        return indexes;
    }

    private FAT getFat()
    {
        //TODO
        return null;
    }

    private void waitTillNotification()
    {
        synchronized(monitor)
        {
            try
            {
                monitor.wait();
            }catch(InterruptedException e)
            {
                e.printStackTrace();
            }

        }
    }

    private void scsiSucessNotification()
    {
        synchronized(monitor)
        {
            monitor.notify();
        }
    }

    private SCSIInterface scsiInterface = new SCSIInterface()
    {
        @Override
        public void onSCSIOperationCompleted(int status, int dataResidue)
        {
            if(status == 0)
            {
                currentStatus = status == 0;
                scsiSucessNotification();
            }
        }

        @Override
        public void onSCSIDataReceived(SCSIResponse response)
        {
            currentResponse = response;
        }

        @Override
        public void onSCSIOperationStarted(boolean status)
        {

        }
    };
}
