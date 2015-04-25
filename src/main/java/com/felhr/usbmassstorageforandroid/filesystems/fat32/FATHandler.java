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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 20/2/15.
 */
public class FATHandler
{
    private SCSICommunicator comm;
    private final Object monitor;
    private SCSIResponse currentResponse;
    private boolean currentStatus;
    private AtomicBoolean waiting;

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
        this.waiting = new AtomicBoolean(true);
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
            int maxEntries = (int) (clustersRoot.size() * reservedRegion.getBytesPerSector()) / 32;
            byte[] data = readClusters(clustersRoot);
            path.setDirectoryContent(getFileEntries(data), maxEntries);
            return true;
        }else
        {
            return false;
        }
    }

    public boolean unMount()
    {
        return preventAllowRemoval(false);
    }

    public List<FileEntry> list()
    {
        return path.getDirectoryContent();
    }

    public List<FileEntry> getPath()
    {
        return path.getAbsolutePath();
    }

    public boolean changeDir(String directoryName)
    {
        Iterator<FileEntry> e = path.getDirectoryContent().iterator();
        while(e.hasNext())
        {
            FileEntry entry = e.next();
            String name;
            if(!entry.getLongName().equals(""))
                name = entry.getLongName();
            else
                name = entry.getShortName();
            if(name.equalsIgnoreCase(directoryName) && entry.isDirectory())
            {
                path.addDirectory(entry);
                long firstCluster = entry.getFirstCluster();
                List<Long> clusterChain = getClusterChain(firstCluster);
                int maxEntries = (int) (clusterChain.size() * reservedRegion.getBytesPerSector()) / 32;
                byte[] data = readClusters(clusterChain);
                path.clearDirectoryContent();
                path.setDirectoryContent(getFileEntries(data), maxEntries);
                return true;
            }
        }
        return false;
    }

    public boolean changeDirBack()
    {
        FileEntry currentEntry = path.getCurrentDirectory();
        if(currentEntry != null)
        {
            path.clearDirectoryContent();
            if(path.deleteLastDir())
            {
                if(!path.isRoot())
                {
                    FileEntry backEntry = path.getCurrentDirectory();
                    long firstCluster = backEntry.getFirstCluster();
                    List<Long> clusterChain = getClusterChain(firstCluster);
                    int maxEntries = (int) (clusterChain.size() * reservedRegion.getBytesPerSector()) / 32;
                    byte[] data = readClusters(clusterChain);
                    path.setDirectoryContent(getFileEntries(data), maxEntries);
                    return true;
                }else
                {
                    List<Long> clustersRoot = getClusterChain(2);
                    int maxEntries = (int) (clustersRoot.size() * reservedRegion.getBytesPerSector()) / 32;
                    byte[] data = readClusters(clustersRoot);
                    path.setDirectoryContent(getFileEntries(data), maxEntries);
                    return true;
                }
            }else
            {
                //You are in root directory, no back dir to go!!
                return false;
            }
        }else
        {
            return false;
        }
    }

    public byte[] readFile(String fileName)
    {
        Iterator<FileEntry> e = path.getDirectoryContent().iterator();
        while(e.hasNext())
        {
            FileEntry entry = e.next();
            String name;
            if(!entry.getLongName().equals(""))
                name = entry.getLongName();
            else
                name = entry.getShortName();
            if(name.equalsIgnoreCase(fileName) && !entry.isDirectory())
            {
                long firstCluster = entry.getFirstCluster();
                List<Long> clusterChain = getClusterChain(firstCluster);
                byte[] data = readClusters(clusterChain);
                return Arrays.copyOf(data, (int) entry.getSize());
            }
        }
        return null;
    }

    /*
        Write a file in the current Path
        TODO: Another possible prototype would be: public boolean writeNewFile(java.io.File file);
     */
    public boolean writeNewFile(String fileName, byte[] data, boolean isRead, boolean isHidden, boolean isdirectory, long lastModified)
    {
        if(path.getFreeEntries() > 0) // There is space for at least one more fileEntry TODO: Not correct check. Fix!
        {
            // TODO: WRITE FILE Entry
            // TODO: GET FILE CLUSTER CHAIN
            // TODO: WRITE FILE
        }else // There is no space for a new entry. resize the directory.
        {
                // Add a new cluster to the dir clusterchain
                List<Long> clusterChain;
                if(!path.isRoot())
                {
                    FileEntry dir = path.getCurrentDirectory();
                    clusterChain = getClusterChain(dir.getFirstCluster());
                }else
                {
                    clusterChain = getClusterChain(2);
                }

                long lastCluster = clusterChain.get(clusterChain.size()-1);
                long newCluster = getNewClusterLink(lastCluster);
                clusterChain.add(newCluster);

                // get dir fileEntries and obtain a valid cluster chain for the new file
                byte[] dirData = readClusters(clusterChain);
                List<Long> fileClusterChain = new ArrayList<Long>();
                int clusters = (int) (data.length / (reservedRegion.getSectorsPerCluster() * reservedRegion.getBytesPerSector())) + 1;
                long indexCluster = 2;
                while(fileClusterChain.size() != clusters)
                {
                    indexCluster = getNewClusterLink(indexCluster);
                    fileClusterChain.add(indexCluster);
                }

                // get a raw FileEntry
                FileEntry newEntry = FileEntry.getEntry(
                        fileName, fileClusterChain.get(0), data.length, path.getDirectoryContent()
                        , isRead, isHidden, isdirectory, lastModified);
                byte[] rawFileEntry = newEntry.getRawFileEntry();

                // Write fileEntry in dir clusters
                int index = getFirstFileEntryIndex(dirData);
                System.arraycopy(rawFileEntry, 0, dirData, index, rawFileEntry.length);

                // Write file in
                return writeClusters(fileClusterChain, data);
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
        byte[] data = readBytes(0, 1);
        if(data != null)
            return MasterBootRecord.parseMbr(data);
        else
            return null;
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

    /*
        Get a new link for the clusterchain of a file.
        TODO: Check carefully this method
     */
    private long getNewClusterLink(long cluster)
    {
        boolean right = true;
        boolean left = false;
        long lbaFat = getEntryLBA(cluster);
        long lbaStart = getEntryLBA(0);
        long lbaFatEnd = lbaStart + reservedRegion.getNumberSectorsPerFat();
        long indexLba = lbaFat + 1;
        if(indexLba > lbaFatEnd)
        {
            right = false;
            left = true;
            indexLba = lbaFat - 1;
        }
        boolean keep = true;
        while(keep)
        {
            byte[] data = readBytes(indexLba, 1);
            if(data == null)
                return 0;

            for(int indexEntry=0;indexEntry<=127;indexEntry++)
            {
                int[] indexes = getRealIndexes(indexEntry);
                long value = UnsignedUtil.convertBytes2Long(data[indexes[3]], data[indexes[2]], data[indexes[1]], data[indexes[0]]);
                if(value == 0x0000000) // Free entry
                {
                        /*
                            Free Entry: This node will point to 0xfffffff
                            and the previous one to this node
                         */

                    long clusterEntry = getFatEntryFromLBA(indexLba, indexes[0]);
                    byte[] nullPointerValue = UnsignedUtil.convertULong2Bytes(0xfffffff);
                    data[indexes[0]] = nullPointerValue[3];
                    data[indexes[1]] = nullPointerValue[2];
                    data[indexes[2]] = nullPointerValue[1];
                    data[indexes[3]] = nullPointerValue[0];

                    // Write 0xfffffff to the last node
                    if(!writeBytes(indexLba, data))
                        return 0;

                    // Update previous node
                    byte[] clusterEntryBytes = UnsignedUtil.convertULong2Bytes(clusterEntry);
                    byte[] dataPreviousCluster = readBytes(lbaFat, 1);
                    if(dataPreviousCluster == null)
                        return 0;

                    int sectorIndex = getEntrySectorIndex(cluster); // range 0-127
                    int[] previousClusterIndexes = getRealIndexes(sectorIndex);

                    dataPreviousCluster[previousClusterIndexes[0]] = clusterEntryBytes[3];
                    dataPreviousCluster[previousClusterIndexes[1]] = clusterEntryBytes[2];
                    dataPreviousCluster[previousClusterIndexes[2]] = clusterEntryBytes[1];
                    dataPreviousCluster[previousClusterIndexes[3]] = clusterEntryBytes[0];
                    if(!writeBytes(indexLba, dataPreviousCluster))
                        return 0;

                    return clusterEntry;
                }
            }

            if(right && indexLba < lbaFatEnd) // keep looking to the right
            {
                indexLba++;
            }
            else if(right && indexLba == lbaFatEnd) // No space on your right. Go left
            {
                right = false;
                left = true;
                indexLba = lbaFat - 1;
            }else if(left && indexLba > lbaStart) // Keep looking to the left
            {
                indexLba--;
            }else // No space available
            {
                keep = false;
            }

        }
        return 0;
    }

    private boolean writeClusters(List<Long> clusters, byte[] data)
    {
        int bufferLength = (int) (reservedRegion.getBytesPerSector() * reservedRegion.getSectorsPerCluster());
        int k = 0;
        byte[] buffer = new byte[bufferLength];
        long firstClusterLba = partition.getLbaStart() + reservedRegion.getNumberReservedSectors()
                + (reservedRegion.getFatCopies() * reservedRegion.getNumberSectorsPerFat());
        Iterator<Long> e = clusters.iterator();
        while(e.hasNext())
        {
            long cluster = e.next();
            long lbaCluster =  firstClusterLba + (cluster - 2) * reservedRegion.getSectorsPerCluster();
            if(k * bufferLength + bufferLength <= data.length)
                System.arraycopy(data, k * bufferLength, buffer, 0, bufferLength);
            else
                System.arraycopy(data, k * bufferLength, buffer, 0, data.length - k * bufferLength);
            boolean result = writeBytes(lbaCluster, buffer);
            if(!result)
                return false;
            k++;
        }
        return true;
    }

    private byte[] readClusters(List<Long> clusters)
    {
        long firstClusterLba = partition.getLbaStart() + reservedRegion.getNumberReservedSectors()
                + (reservedRegion.getFatCopies() * reservedRegion.getNumberSectorsPerFat());
        int lengthData = clusters.size() * ((int) (reservedRegion.getSectorsPerCluster() * reservedRegion.getBytesPerSector()));
        byte[] data = new byte[lengthData];
        int index = 0;
        Iterator<Long> e = clusters.iterator();
        while(e.hasNext())
        {
            long cluster = e.next();
            long lbaCluster =  firstClusterLba + (cluster - 2) * reservedRegion.getSectorsPerCluster();
            byte[] clusterData = readBytes(lbaCluster, (int) reservedRegion.getSectorsPerCluster());
            System.arraycopy(clusterData, 0, data, index, clusterData.length);
            index += reservedRegion.getSectorsPerCluster() * reservedRegion.getBytesPerSector();
        }
        return data;
    }

    private ReservedRegion getReservedRegion()
    {
        long lbaPartitionStart = partition.getLbaStart();
        byte[] data = readBytes(lbaPartitionStart, 1);
        if(data != null)
            return ReservedRegion.getReservedRegion(data);
        else
            return null;
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

    private boolean writeBytes(long lba, byte[] data)
    {
        //TODO: writeBytes function
        return false;
    }

    private boolean preventAllowRemoval(boolean prevent)
    {
        comm.preventAllowRemoval(0, prevent);
        waitTillNotification();
        return currentStatus;
    }

    private int getFirstFileEntryIndex(byte[] data)
    {
        int k = 0;
        boolean keep = true;
        while(keep)
        {
            if(data[k * 32] == 0x00)
                keep = false;
            else
                k++;
        }
        return k * 32;
    }

    private List<FileEntry> getFileEntries(byte[] data)
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
            if((bufferEntry[0] != 0x00 && bufferEntry[0] != (byte) 0xe5)
                    && (bufferEntry[11] == 0x0f || bufferEntry[11] == 0x1f || bufferEntry[11] == 0x2f
                    || bufferEntry[11] == 0x3f)) // LFN Entry
            {
                longFileEntryNames.add(parseLFN(bufferEntry));
            }else if((bufferEntry[0] != 0x00 && bufferEntry[0] != (byte) 0xe5)) // Normal entry
            {

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
        return entries;
    }

    private String parseLFN(byte[] lfnData)
    {
        boolean endChar = false;
        List<Byte> unicodeList = new ArrayList<Byte>();
        if((lfnData[1] != 0x00 || lfnData[2] != 0x00))
        {
            unicodeList.add(lfnData[1]);
            if(lfnData[2] != 0x00)
                unicodeList.add(lfnData[2]);
        }else
            endChar = true;

        if((lfnData[3] != 0x00 || lfnData[4] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[3]);
            if(lfnData[4] != 0x00)
                unicodeList.add(lfnData[4]);
        }else
            endChar = true;

        if((lfnData[5] != 0x00 || lfnData[6] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[5]);
            if(lfnData[6] != 0x00)
                unicodeList.add(lfnData[6]);
        }else
            endChar = true;

        if((lfnData[7] != 0x00 || lfnData[8] != 0x00) && !endChar)
        {

            unicodeList.add(lfnData[7]);
            if(lfnData[8] != 0x00)
                unicodeList.add(lfnData[8]);
        }else
            endChar = true;

        if((lfnData[9] != 0x00 || lfnData[10] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[9]);
            if(lfnData[10] != 0x00)
                unicodeList.add(lfnData[10]);
        }else
            endChar = true;

        if((lfnData[14] != 0x00 || lfnData[15] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[14]);
            if(lfnData[15] != 0x00)
                unicodeList.add(lfnData[15]);
        }else
            endChar = true;

        if((lfnData[16] != 0x00 || lfnData[17] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[16]);
            if(lfnData[17] != 0x00)
                unicodeList.add(lfnData[17]);
        }else
            endChar = true;

        if((lfnData[18] != 0x00 || lfnData[19] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[18]);
            if(lfnData[19] != 0x00)
                unicodeList.add(lfnData[19]);
        }else
            endChar = true;

        if((lfnData[20] != 0x00 || lfnData[21] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[20]);
            if(lfnData[21] != 0x00)
                unicodeList.add(lfnData[21]);
        }else
            endChar = true;

        if((lfnData[22] != 0x00 || lfnData[23] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[22]);
            if(lfnData[23] != 0x00)
                unicodeList.add(lfnData[23]);
        }else
            endChar = true;

        if((lfnData[24] != 0x00 || lfnData[25] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[24]);
            if(lfnData[25] != 0x00)
                unicodeList.add(lfnData[25]);
        }else
            endChar = true;

        if((lfnData[28] != 0x00 || lfnData[29] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[28]);
            if(lfnData[29] != 0x00)
             unicodeList.add(lfnData[29]);
        }else
            endChar = true;

        if((lfnData[30] != 0x00 || lfnData[31] != 0x00) && !endChar)
        {
            unicodeList.add(lfnData[30]);
            if(lfnData[31] != 0x00)
                unicodeList.add(lfnData[31]);
        }

        byte[] unicodeBuffer = new byte[unicodeList.size()];
        int i  = 0;
        while(i <= unicodeBuffer.length -1)
        {
            unicodeBuffer[i] = unicodeList.get(i);
            i++;
        }

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

    private long getFatEntryFromLBA(long lba, int index)
    {
        return (lba - getEntryLBA(0)) * 128 + (index / 4);
    }


    private void waitTillNotification()
    {
        synchronized(monitor)
        {
            while(waiting.get())
            {
                try
                {
                    monitor.wait();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            waiting.set(true);
        }
    }

    private void scsiSuccessNotification()
    {
        synchronized(monitor)
        {
            waiting.set(false);
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
                currentStatus = true;
                scsiSuccessNotification();
            }else
            {
                currentStatus = false;
                scsiSuccessNotification();
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