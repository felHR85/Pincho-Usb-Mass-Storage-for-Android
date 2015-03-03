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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

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
    private Path path;

    public FATHandler(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.comm = new SCSICommunicator(mDevice, mConnection);
        this.comm.openSCSICommunicator(scsiInterface);
        this.monitor = new Object();
        this.path = new Path();
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
            List<Long> clustersRoot = getClusterChain(2);

        }else
        {
            return false;
        }
        return false;
    }

    public void changeDirectory(String directory)
    {
        //TODO
    }

    public byte[] openFile(String fileName)
    {
        //TODO
        return null;
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

    private List<Long> getClusterChain(long cluster)
    {
        boolean keepSearching = true;
        List<Long> clusterChain = new ArrayList<Long>();
        clusterChain.add(cluster);
        while(keepSearching)
        {
            long lbaCluster = getEntryLBA(cluster);
            byte[] sector = readBytes(lbaCluster, 1);
            int entrySectorIndex = getEntrySectorIndex(cluster);
            int[] indexes = getRealIndexes(entrySectorIndex);
            cluster = UnsignedUtil.convertBytes2Long(sector[indexes[3]], sector[indexes[2]], sector[indexes[1]], sector[indexes[0]]);
            if(cluster != 0xfffffff)
            {
                clusterChain.add(cluster);
            }else
            {
                keepSearching = false;
            }
        }
        return clusterChain;
    }

    private byte[] readClusters(long[] cluster)
    {
        //TODO
        return null;
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

    private byte[] readBytes(long lba, int length)
    {
        comm.read10(0, false, false, false, UnsignedUtil.ulongToInt(lba), 0, length);
        waitTillNotification();
        if(currentStatus)
        {
            return ((SCSIRead10Response) currentResponse).getBuffer();
        }else
        {
            return null;
        }
    }

    private FileEntry[] getFileEntries(byte[] data)
    {
        List<FileEntry> entries = new ArrayList<FileEntry>();
        List<String> longFileEntryNames = new ArrayList<String>();
        int entrySize = 32;
        byte[] bufferEntry = new byte[entrySize];
        int i = 0;
        int index1 = entrySize * i;
        while(index1 < data.length)
        {
            System.arraycopy(data, index1, bufferEntry, 0, entrySize);
            if((bufferEntry[11] & 1) == 1 && (bufferEntry[11] & 2) == 2 && (bufferEntry[11] & 4) == 4) // LFN Entry
            {
                longFileEntryNames.add(parseLFN(bufferEntry));
            }else // Normal entry
            {
                //TODO It could be a all 0x00 entry so it has not to be added (TODO)
                if(longFileEntryNames != null) // LFN is present
                {
                    String lfn = "";
                    int index2 = longFileEntryNames.size() - 1;
                    while(index2 >= 0)
                    {
                        lfn += longFileEntryNames.get(index2);
                        index2--;
                    }
                    entries.add(FileEntry.getEntry(lfn, bufferEntry));
                    longFileEntryNames.clear();
                }else // No LFN
                {
                    entries.add(FileEntry.getEntry(null, bufferEntry));
                }
            }
            i++;
            index1 = entrySize * i;
        }
        return (FileEntry[]) entries.toArray();
    }

    private String parseLFN(byte[] lfnData)
    {
        byte[] unicodeBuffer = new byte[26];
        unicodeBuffer[0] = lfnData[1];
        unicodeBuffer[1] = lfnData[2];
        unicodeBuffer[2] = lfnData[3];
        unicodeBuffer[3] = lfnData[4];
        unicodeBuffer[4] = lfnData[5];
        unicodeBuffer[5] = lfnData[6];
        unicodeBuffer[6] = lfnData[7];
        unicodeBuffer[7] = lfnData[8];
        unicodeBuffer[8] = lfnData[9];
        unicodeBuffer[9] = lfnData[10];
        unicodeBuffer[10] = lfnData[14];
        unicodeBuffer[11] = lfnData[15];
        unicodeBuffer[12] = lfnData[16];
        unicodeBuffer[13] = lfnData[17];
        unicodeBuffer[14] = lfnData[18];
        unicodeBuffer[15] = lfnData[19];
        unicodeBuffer[16] = lfnData[20];
        unicodeBuffer[17] = lfnData[21];
        unicodeBuffer[18] = lfnData[22];
        unicodeBuffer[19] = lfnData[23];
        unicodeBuffer[20] = lfnData[24];
        unicodeBuffer[21] = lfnData[25];
        unicodeBuffer[22] = lfnData[28];
        unicodeBuffer[23] = lfnData[29];
        unicodeBuffer[24] = lfnData[30];
        unicodeBuffer[25] = lfnData[31];

        try
        {
            return new String(unicodeBuffer, "UTF-8");
        }catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private long getEntryLBA(long entry)
    {
        long fatLBA = partition.getLbaStart() + reservedRegion.getNumberReservedSectors();
        return fatLBA + (entry / 128);
    }

    private int getEntrySectorIndex(long entry) // range of returned value: [0-127]
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