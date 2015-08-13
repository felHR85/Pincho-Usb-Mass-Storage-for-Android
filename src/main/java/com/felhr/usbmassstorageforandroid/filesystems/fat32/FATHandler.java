package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.felhr.usbmassstorageforandroid.filesystems.MasterBootRecord;
import com.felhr.usbmassstorageforandroid.filesystems.Partition;
import com.felhr.usbmassstorageforandroid.scsi.SCSICommunicator;
import com.felhr.usbmassstorageforandroid.scsi.SCSIInterface;
import com.felhr.usbmassstorageforandroid.scsi.SCSIRead10Response;
import com.felhr.usbmassstorageforandroid.scsi.SCSIResponse;
import com.felhr.usbmassstorageforandroid.utilities.UnsignedUtil;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 20/2/15.
 */
public class FATHandler
{
    private static final int LOAD_CACHE = 0;
    private static final int FIND_EMPTY_CLUSTERCHAIN = 1;

    private SCSICommunicator comm;
    private final Object monitor;
    private final Object cacheMonitor;
    private SCSIResponse currentResponse;
    private boolean currentStatus;
    private AtomicBoolean waiting;

    private MasterBootRecord mbr;

    //Mounted Partition
    private Partition partition;
    private ReservedRegion reservedRegion;
    private Path path;

    //CacheThread vars
    private Handler wHandler;
    private FAT32Cache cache;
    private CacheThread cacheThread;

    public FATHandler(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        this.comm = new SCSICommunicator(mDevice, mConnection);
        this.monitor = new Object();
        this.cacheMonitor = new Object();
        this.cache = new FAT32Cache();
        this.path = new Path();
        this.waiting = new AtomicBoolean(true);
        this.cacheThread = new CacheThread();
    }

    public boolean mount(int partitionIndex, int cacheMode)
    {
        boolean isOpen = comm.openSCSICommunicator(scsiInterface);

        if(!isOpen)
            return false;

        testUnitReady();

        if(currentStatus)
            mbr = getMbr();
        else
            return false;

        if(mbr.getPartitions().length >= partitionIndex + 1)
        {
            partition = mbr.getPartitions()[partitionIndex];
            if(!partition.isFAT32())
                return false;
            reservedRegion = getReservedRegion();
            List<Long> clustersRoot = getClusterChain(2);
            byte[] data = readClusters(clustersRoot);
            path.setDirectoryContent(getFileEntries(data));

            if(cacheMode != 0)
            {
                cacheThread.start();
                while(wHandler == null){}
                wHandler.obtainMessage(LOAD_CACHE, cacheMode, 0).sendToTarget();
                cacheThread.waitTillCaching();
            }
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
                byte[] data = readClusters(clusterChain);
                path.clearDirectoryContent();
                path.setDirectoryContent(getFileEntries(data));
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
                    byte[] data = readClusters(clusterChain);
                    path.clearDirectoryContent();
                    path.setDirectoryContent(getFileEntries(data));
                    return true;
                }else
                {
                    List<Long> clustersRoot = getClusterChain(2);
                    byte[] data = readClusters(clustersRoot);
                    path.clearDirectoryContent();
                    path.setDirectoryContent(getFileEntries(data));
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

    /*
        Filename: fileName should be a LFN or a short file name + extension
     */
    public byte[] readFile(String fileName)
    {
        Iterator<FileEntry> e = path.getDirectoryContent().iterator();
        while(e.hasNext())
        {
            FileEntry entry = e.next();
            String name;
            if(!entry.getLongName().equals(""))
                name = entry.getLongName();
            else if(!entry.getFileExtension().equals(""))
                name = entry.getShortName() + "." + entry.getFileExtension();
            else
                name = entry.getShortName();

            if(name.equalsIgnoreCase(fileName) && !entry.isDirectory())
            {
                long firstCluster = entry.getFirstCluster();
                if(firstCluster != 0) // File size is not 0
                {
                    List<Long> clusterChain = getClusterChain(firstCluster);
                    byte[] data = readClusters(clusterChain);
                    return Arrays.copyOf(data, (int) entry.getSize());
                }else
                {
                    return new byte[0];
                }
            }
        }
        return null;
    }

    public boolean writeNewFile(java.io.File file)
    {
        String fileName = file.getName();
        boolean isReadOnly = !file.canWrite();
        boolean isHidden = file.isHidden();
        boolean isDirectory = file.isDirectory();
        long lastModified = file.lastModified();

        if(!file.isDirectory())
        {
            byte[] fileData = new byte[(int) file.length()];
            try
            {
                DataInputStream dis = new DataInputStream(new FileInputStream(file));
                try
                {
                    dis.readFully(fileData);
                } catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }

            }catch(FileNotFoundException e)
            {
                e.printStackTrace();
                return false;
            }

            return writeNewFile(fileName, fileData, isReadOnly, isHidden, isDirectory, lastModified);
        }else
        {
            boolean result = writeNewFile(fileName, null, isReadOnly, isHidden, isDirectory, lastModified);
            changeDir(fileName);

            if(!result)
                return false;

            java.io.File[] files = file.listFiles();
            for(int i=0;i<=files.length-1;i++)
            {
                java.io.File subFile = files[i];
                writeNewFile(subFile);
            }
            changeDirBack();
        }
        return true;
    }


    /*
        Write a file in the current Path

     */
    public boolean writeNewFile(String fileName, byte[] data, boolean isReadOnly, boolean isHidden, boolean isDirectory, long lastModified)
    {
        // Get clusterchain of the current folder
        List<Long> clusterChain;
        if(!path.isRoot())
        {
            FileEntry dir = path.getCurrentDirectory();
            clusterChain = getClusterChain(dir.getFirstCluster());
        }else
        {
            clusterChain = getClusterChain(2);
        }


        // LFN entries required + 1 fileEntry + 1 more if fileName.length() % 11 != 0
        int fileEntriesRequired = fileName.length() / 11 + 1;
        if(fileName.length() % 11 != 0)
            fileEntriesRequired += 1;

        // There is no space for a new entry. resize the folder.
        if(path.getFreeEntries() < fileEntriesRequired)
        {
            long lastCluster = clusterChain.get(clusterChain.size()-1);
            long newLastCluster = resizeClusterChain(lastCluster);
            int freeEntriesNewCluster = (int) (reservedRegion.getSectorsPerCluster() * reservedRegion.getBytesPerSector()) / 32;
            path.setFreeEntries(freeEntriesNewCluster + path.getFreeEntries());
            if(newLastCluster != 0)
                clusterChain.add(newLastCluster);
            else
                return false;
        }

        // get dir fileEntries and obtain a valid cluster chain for the new file
        byte[] dirData = readClusters(clusterChain);
        List<Long> fileClusterChain = new ArrayList<Long>();
        if(!isDirectory)
        {
            int clusters;
            if(data.length != 0)
            {
                clusters = (int) (data.length / (reservedRegion.getSectorsPerCluster() * reservedRegion.getBytesPerSector()));
                if(data.length % (reservedRegion.getSectorsPerCluster() * reservedRegion.getBytesPerSector()) != 0)
                    clusters += 1;
            }else
            {
                clusters = 1;
            }
            fileClusterChain = setClusterChain(clusters, true);
            if(fileClusterChain == null) // It was no possible to get a clusterchain
                return false;
        }else
        {
            // It is a dir, it just needs one cluster at least at this moment
            fileClusterChain = setClusterChain(1, true);
            if(fileClusterChain == null) // It was no possible to get a clusterchain
                return false;
        }

        // get a raw FileEntry
        long size;
        if(!isDirectory)
            size = data.length;
        else
            size = 0;

        FileEntry newEntry = FileEntry.getEntry(
                fileName, fileClusterChain.get(0), size, path.getDirectoryContent()
                , isReadOnly, isHidden, isDirectory, lastModified);
        byte[] rawFileEntry = newEntry.getRawFileEntry();


        // Write fileEntry in dir clusters
        int index = getFirstFileEntryIndex(dirData);
        System.arraycopy(rawFileEntry, 0, dirData, index, rawFileEntry.length);

        // Write file entry
        writeClusters(clusterChain, dirData);

        // update free entries
        path.setFreeEntries(path.getFreeEntries() - fileEntriesRequired);

        // Write file only if file entry is not a directory and the size is not 0
        if(!isDirectory && size != 0)
        {
            boolean result = writeClusters(fileClusterChain, data);
            if(result)
            {
                path.addFileEntry(newEntry);
            }
            return result;
        }else if(isDirectory)
        {
            // Add . and .. entries
            FileEntry dotEntry = FileEntry.getEntry(
                    ".", fileClusterChain.get(0), 0, null
                    , false, false, true, lastModified);

            FileEntry dotDotEntry = FileEntry.getEntry(
                    "..", clusterChain.get(0), 0, null
                    , false, false, true, lastModified);

            byte[] dotEntryRaw = dotEntry.getRawFileEntry();
            byte[] dotDotEntryRaw = dotDotEntry.getRawFileEntry();

            byte[] dotEntriesRaw = new byte[64];
            System.arraycopy(dotEntryRaw, 0, dotEntriesRaw, 0, 32);
            System.arraycopy(dotDotEntryRaw, 0, dotEntriesRaw, 32, 32);

            writeClusters(fileClusterChain, dotEntriesRaw);
        }

        path.addFileEntry(newEntry);
        return true;
    }

    public boolean deleteFile(String fileName)
    {
        Iterator<FileEntry> e = path.getDirectoryContent().iterator();
        int i = 0;
        while(e.hasNext())
        {
            FileEntry entry = e.next();
            String name;
            if(!entry.getLongName().equals(""))
                name = entry.getLongName();
            else
                name = entry.getShortName();
            if(name.equalsIgnoreCase(fileName))
            {
                long firstCluster;
                if(!path.isRoot())
                    firstCluster = path.getCurrentDirectory().getFirstCluster();
                else
                    firstCluster = 2;

                List<Long> clusterChainFolder = getClusterChain(firstCluster);
                List<Long> clusterChainFile = getClusterChain(entry.getFirstCluster());
                // if no elements in clusterchain get out
                byte[] data = readClusters(clusterChainFolder);
                boolean result = setEntryToErased(data, i, entry.getLongName());
                if(result)
                    writeClusters(clusterChainFolder, data);
                else
                    return false;

                // Delete the FileEntry object
                path.deleteFileEntry(i);

                return deleteClusterChain(clusterChainFile);
            }
            i++;
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

    /*
        Optimization required: if next cluster pointer is the next sector
        there is no need to use readBytes again.
     */
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
      Set a clusterchain on the FAT
      Return null if is not possible to get clusterchain
     */
    private List<Long> setClusterChain(int clusters, boolean forceCache)
    {
        List<Long> clusterChainList = new ArrayList<Long>();
        long[] lbaChain = new long[clusters];
        int[] entries = new int[clusters]; // 0-127 range
        int i = 0; // index for clusterchain
        long lbaFatStart = getEntryLBA(0);
        long lbaIndex;
        if(!forceCache)
            lbaIndex = lbaFatStart;
        else
        {
            lbaIndex = cache.getCluster();
            if(lbaIndex == 0) // No sectors in the cache
                lbaIndex = lbaFatStart;
        }
        long lbaFatEnd = lbaFatStart + reservedRegion.getNumberSectorsPerFat();
        boolean keep = true;
        while(keep)
        {
            byte[] data = readBytes(lbaIndex, 1);
            for(int indexEntry=0;indexEntry<=127;indexEntry++)
            {
                int[] indexes = getRealIndexes(indexEntry);
                long value = UnsignedUtil.convertBytes2Long(data[indexes[3]], data[indexes[2]], data[indexes[1]], data[indexes[0]]);
                if(value == 0x0000000)
                {
                    long clusterEntry = getFatEntryFromLBA(lbaIndex, indexes[0]);
                    clusterChainList.add(clusterEntry);
                    lbaChain[i] = lbaIndex;
                    entries[i] = indexEntry;
                    if(++i == clusters) // All empty clusters has been located. Set the clusterchain
                    {
                        for(int j=0;j<=clusters-1;j++)
                        {
                            long lba = lbaChain[j];
                            byte[] data2 = readBytes(lba, 1);
                            long nextCluster;
                            if(j < clusters-1)
                                nextCluster = clusterChainList.get(j+1);
                            else
                                nextCluster = 0xfffffff;

                            int fatEntry = entries[j]; // 0-127 range

                            int[] currentIndexes = getRealIndexes(fatEntry);

                            byte[] nextClusterRaw = UnsignedUtil.convertULong2Bytes(nextCluster);
                            data2[currentIndexes[0]] = nextClusterRaw[3];
                            data2[currentIndexes[1]] = nextClusterRaw[2];
                            data2[currentIndexes[2]] = nextClusterRaw[1];
                            data2[currentIndexes[3]] = nextClusterRaw[0];

                            writeBytes(lba, data2);
                        }
                        keep = false;
                        break;
                    }
                }
            }
            if(!forceCache)
                lbaIndex++;
            else
            {
                if(keep)
                {
                    cache.deleteCluster();
                    lbaIndex = cache.getCluster();
                    if (lbaIndex == 0)
                        lbaIndex += 2; // No more clusters in cache.
                }
            }
            if(lbaIndex > lbaFatEnd)
                return null;
        }

        return clusterChainList;
    }

    /*
       Resize a clusterchain. Returns the new last cluster or 0 if it was not possible to resize
     */
    private long resizeClusterChain(long lastCluster)
    {
        long lbaFATLastCluster = getEntryLBA(lastCluster);
        byte[] data = readBytes(lbaFATLastCluster, 1);
        int sectorIndex = getEntrySectorIndex(lastCluster); // 0-127
        if(sectorIndex < 127)
            sectorIndex++;
        long indexFat = lbaFATLastCluster;
        long lbaFatEnd = getEntryLBA(0) + reservedRegion.getNumberSectorsPerFat();
        boolean keep = true;
        while(keep)
        {
            for(int i=sectorIndex; i<=127;i++)
            {
                int[] indexes = getRealIndexes(i);
                long value = UnsignedUtil.convertBytes2Long(data[indexes[3]], data[indexes[2]], data[indexes[1]], data[indexes[0]]);
                if(value == 0x0000000)
                {
                    long clusterEntry = getFatEntryFromLBA(indexFat, indexes[0]);
                    List<Long> singleList = new ArrayList<Long>();
                    singleList.add(clusterEntry);
                    byte[] zeroedCluster = new byte[(int) (reservedRegion.getBytesPerSector() * reservedRegion.getSectorsPerCluster())];
                    writeClusters(singleList, zeroedCluster); // Set the referred cluster to 0x00 (whole cluster is empty)

                    // Previous last cluster FAT entry now points to the new last cluster
                    byte[] dataPrevLBA;

                    if(lbaFATLastCluster == indexFat)
                    {
                        dataPrevLBA = data;
                    }else
                    {
                        dataPrevLBA = readBytes(lbaFATLastCluster, 1);
                    }

                    sectorIndex = getEntrySectorIndex(lastCluster); // 0-127
                    int[] prevIndexes = getRealIndexes(sectorIndex);
                    byte[] lastClusterRaw = UnsignedUtil.convertULong2Bytes(clusterEntry);
                    dataPrevLBA[prevIndexes[0]] = lastClusterRaw[3];
                    dataPrevLBA[prevIndexes[1]] = lastClusterRaw[2];
                    dataPrevLBA[prevIndexes[2]] = lastClusterRaw[1];
                    dataPrevLBA[prevIndexes[3]] = lastClusterRaw[0];
                    if(!writeBytes(lbaFATLastCluster, dataPrevLBA))
                        return 0;

                    // Current last cluster FAT entry points to NUL (0xfffffff)
                    lastClusterRaw = UnsignedUtil.convertULong2Bytes(0xfffffff);
                    data[indexes[0]] = lastClusterRaw[3];
                    data[indexes[1]] = lastClusterRaw[2];
                    data[indexes[2]] = lastClusterRaw[1];
                    data[indexes[3]] = lastClusterRaw[0];
                    if(!writeBytes(indexFat, data))
                        return 0;

                    return clusterEntry;
                }
            }
            indexFat++;
            sectorIndex = 0;
            if(indexFat > lbaFatEnd)
                return 0; // 0 is not a valid cluster
            data = readBytes(indexFat, 1);
        }

        return 0;
    }

    private boolean deleteClusterChain(List<Long> clusterChain)
    {
        Iterator<Long> e = clusterChain.iterator();
        while(e.hasNext())
        {
            long cluster = e.next();
            long lbaCluster = getEntryLBA(cluster);
            int sectorIndex = getEntrySectorIndex(cluster); // 0-127
            int realIndexes[] = getRealIndexes(sectorIndex);

            byte[] data = readBytes(lbaCluster, 1);

            data[realIndexes[0]] = 0x00;
            data[realIndexes[1]] = 0x00;
            data[realIndexes[2]] = 0x00;
            data[realIndexes[3]] = 0x00;

            if(!writeBytes(lbaCluster, data))
                return false;
        }
        return true;
    }

    private boolean writeClusters(List<Long> clusters, byte[] data)
    {
        int maxLength = 16384; // Linux/libusb internally can only handle a buffer of 16834 for bulk transfers
        int maxClusters = (int) (maxLength / (reservedRegion.getBytesPerSector() * reservedRegion.getSectorsPerCluster()));
        long firstClusterLba = partition.getLbaStart() + reservedRegion.getNumberReservedSectors()
                + (reservedRegion.getFatCopies() * reservedRegion.getNumberSectorsPerFat());
        ListIterator<Long> e = clusters.listIterator();
        int pointer = 0;

        while(e.hasNext())
        {
            long cluster = e.next();
            int i = 1;
            boolean keep = true;
            while(i < maxClusters && keep)
            {
                if(e.hasNext())
                {
                    if(cluster == e.next() - i)
                        i++;
                    else
                    {
                        if(e.hasPrevious())
                            e.previous();
                        keep = false;
                    }
                }else
                {
                    keep = false;
                }
            }

            long lbaCluster = firstClusterLba + (cluster - 2) * reservedRegion.getSectorsPerCluster();
            int bufferLength = (int) (reservedRegion.getBytesPerSector() * reservedRegion.getSectorsPerCluster() * i);
            byte[] buffer = new byte[bufferLength];

            if(pointer + bufferLength <= data.length)
                System.arraycopy(data, pointer, buffer, 0, bufferLength);
            else
                System.arraycopy(data, pointer, buffer, 0, data.length - pointer);

            boolean result = writeBytes(lbaCluster, buffer);
            if(!result)
                return false;

            pointer += bufferLength;
        }

        return true;
    }

    private byte[] readClusters(List<Long> clusters)
    {
        int maxLength = 16384; // Linux/libusb internally can only handle a buffer of 16834 for bulk transfers
        int maxClusters = (int) (maxLength / (reservedRegion.getBytesPerSector() * reservedRegion.getSectorsPerCluster()));
        long firstClusterLba = partition.getLbaStart() + reservedRegion.getNumberReservedSectors()
                + (reservedRegion.getFatCopies() * reservedRegion.getNumberSectorsPerFat());

        int lengthData = clusters.size() * ((int) (reservedRegion.getSectorsPerCluster() * reservedRegion.getBytesPerSector()));
        byte[] data = new byte[lengthData];

        ListIterator<Long> e = clusters.listIterator();
        int pointer = 0;

        while(e.hasNext())
        {
            long cluster = e.next();
            int i = 1;
            boolean keep = true;
            while(i < maxClusters && keep)
            {
                if(e.hasNext())
                {
                    if(cluster == e.next() - i)
                        i++;
                    else
                    {
                        if(e.hasPrevious())
                            e.previous();
                        keep = false;
                    }
                }else
                {
                    keep = false;
                }
            }

            long lbaCluster = firstClusterLba + (cluster - 2) * reservedRegion.getSectorsPerCluster();
            int clustersLength = i *  (int) (reservedRegion.getSectorsPerCluster());
            int bufferLength = clustersLength * ((int) reservedRegion.getBytesPerSector());

            byte[] rawClusters = readBytes(lbaCluster, clustersLength);
            if(rawClusters == null)
                return null;

            System.arraycopy(rawClusters, 0, data, pointer, bufferLength);

            pointer += bufferLength;
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
        int length = data.length / 512;
        if(data.length % 512 != 0)
            length += 1;

        comm.write10(0, false, false, false, UnsignedUtil.ulongToInt(lba), 0, length, data);
        waitTillNotification();
        return currentStatus;
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
        int freeEntries = 0;
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
                longFileEntryNames.add(LFNHandler.parseLFNEntry(bufferEntry));
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
            }else if(bufferEntry[0] == 0x00) // Free entries batch started. Calculate free entries and break
            {
                int freeBytes = data.length - index1;
                freeEntries = freeBytes / 32;
                break;
            }
            i++;
            index1 = entrySize * i;
        }
        path.setFreeEntries(freeEntries);
        return entries;
    }

    private boolean setEntryToErased(byte[] data, int indexEntry, String longName)
    {
        int counterEntries = 0;
        int lfnEntries;
        if(!longName.equals(""))
        {
            lfnEntries = longName.length() / 11;
            if(longName.length() % 11 != 0)
                lfnEntries += 1;
        }else
        {
            lfnEntries = 0;
        }

        int i = 0;
        while(i < data.length-1)
        {
            byte firstByte = data[i * 32];
            byte attr = data[i * 32 + 11];
            if(firstByte != 0x00 && firstByte != (byte) 0xe5
                    && attr != 0x0f && attr != 0x1f && attr != 0x2f && attr != 0x3f)
            {
                if(counterEntries == indexEntry) // given entry has been found
                {
                    data[i * 32] = (byte) 0xe5; // Mark entry as delete
                    for (int j = 1; j <= lfnEntries; j++) // Mark all lfn entries as deleted
                    {
                        int k = i * 32;
                        data[k - (j * 32)] = (byte) 0xe5;
                    }
                    return true;
                }else
                {
                    counterEntries++;
                }
            }
            i++;
        }
        return false;
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

    private class CacheThread extends Thread
    {
        private AtomicBoolean keep;
        private AtomicBoolean cacheReading;
        private int maxElements;

        public CacheThread()
        {
            keep = new AtomicBoolean(false);
            cacheReading = new AtomicBoolean(true);
        }

        @Override
        public void run()
        {
            Looper.prepare();
            wHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    switch(msg.what)
                    {
                        case LOAD_CACHE:
                            populateCache(msg.arg1);
                            break;
                        case FIND_EMPTY_CLUSTERCHAIN:
                            break;
                    }
                }
            };
            Looper.loop();
        }

        public boolean waitTillCaching()
        {
            synchronized(cacheMonitor)
            {
                while(cacheReading.get())
                {
                    try
                    {
                        cacheMonitor.wait();
                    }catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }

        public void notifyCacheRead()
        {
            synchronized(cacheMonitor)
            {
                cacheReading.set(false);
                cacheMonitor.notify();
            }
        }

        private void populateCache(int cacheMode)
        {
            int j = 0;

            long lbaFATStart = getEntryLBA(0);
            long lbaFATEnd = lbaFATStart + reservedRegion.getNumberSectorsPerFat();

            if(cacheMode == 1) // Low Cache
                maxElements = 200;
            else if(cacheMode == 2) // Medium Cache
                maxElements = (int) (reservedRegion.getNumberSectorsPerFat() / 2);
            else if(cacheMode == 3) // High Cache
                maxElements = (int) reservedRegion.getNumberSectorsPerFat();

            for(long i=lbaFATStart;i<=lbaFATEnd-1;i++)
            {
                byte[] rawFATLba = readBytes(i, 1);
                if(isCacheable(rawFATLba))
                {
                    cache.addCluster(i);
                    if(++j == maxElements)
                    {
                        notifyCacheRead();
                        return;
                    }
                }
            }
            notifyCacheRead();
        }

        /*
            Consider the sector cacheable if at least 1/4 is available
         */
        private boolean isCacheable(byte[] rawSector)
        {
            int counter = 0;
            for(int j=0;j<=rawSector.length-1;j+=4)
            {
                long entry = UnsignedUtil.convertBytes2Long(rawSector[j+3],
                        rawSector[j+2], rawSector[j+1], rawSector[j]);
                if(entry == 0)
                    counter++;
                if(counter > 100)
                    return true;
            }
            return false;
        }
    }
}