package com.felhr.usbmassstorageforandroid.filesystems;

import com.felhr.usbmassstorageforandroid.utilities.EndianessUtil;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/2/15.
 */
public class Partition
{
    private boolean bootable;
    private int chsStart; // 3-bytes
    private int partitionType; // 1-byte
    private int chsEnd; // 3-bytes
    private int lbaStart; // 4-bytes
    private int sectorsNumber; // 4-bytes

    private Partition()
    {

    }

    public static Partition parsePartition(byte[] partitionData)
    {
        if(partitionData.length == 16 && !isEmptyPartition(partitionData))
        {
            Partition partition = new Partition();
            partition.bootable = partitionData[0] == 0x80;
            partition.chsStart = partitionData[3] << 16 + partitionData[2] << 8 + partitionData[1];
            partition.partitionType = partitionData[4];
            partition.chsEnd = partitionData[7] << 16 + partitionData[6] << 8 + partitionData[5];
            partition.lbaStart = partitionData[11] << 24 + partitionData[10] << 16 + partitionData[9] << 8 + partitionData[8];
            partition.sectorsNumber = partitionData[15] << 24 + partitionData[14] << 16 + partitionData[13] << 8 + partitionData[12];
            return partition;
        }else
        {
            return null;
        }
    }

    private static boolean isEmptyPartition(byte[] partition)
    {
        return partition[4] == 0x00;
    }

    public boolean isBootable()
    {
        return bootable;
    }


    public int getChsStart()
    {
        return chsStart;
    }


    public int getPartitionType()
    {
        return partitionType;
    }


    public int getChsEnd()
    {
        return chsEnd;
    }


    public int getLbaStart()
    {
        return lbaStart;
    }


    public int getSectorsNumber()
    {
        return sectorsNumber;
    }
}
