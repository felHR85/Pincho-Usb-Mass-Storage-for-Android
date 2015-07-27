package com.felhr.usbmassstorageforandroid.filesystems;

import android.os.Bundle;
import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.EndianessUtil;
import com.felhr.usbmassstorageforandroid.utilities.HexUtil;
import com.felhr.usbmassstorageforandroid.utilities.UnsignedUtil;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/2/15.
 */
public class Partition
{
    private boolean bootable;
    private long chsStart; // 3-bytes
    private int partitionType; // 1-byte
    private long chsEnd; // 3-bytes
    private long lbaStart; // 4-bytes
    private long sectorsNumber; // 4-bytes

    private Partition()
    {

    }

    public static Partition parsePartition(byte[] partitionData)
    {
        if(partitionData.length == 16 && !isEmptyPartition(partitionData))
        {
            Partition partition = new Partition();
            partition.bootable = partitionData[0] == 0x80;
            partition.chsStart = UnsignedUtil.convertBytes2Long(partitionData[3] ,partitionData[2], partitionData[1]);
            partition.partitionType = partitionData[4];
            partition.chsEnd = UnsignedUtil.convertBytes2Long(partitionData[7], partitionData[6], partitionData[5]);
            partition.lbaStart = UnsignedUtil.convertBytes2Long(partitionData[11], partitionData[10], partitionData[9], partitionData[8]);
            partition.sectorsNumber = UnsignedUtil.convertBytes2Long(partitionData[15], partitionData[14], partitionData[13], partitionData[12]);
            return partition;
        }else
        {
            return null;
        }
    }

    public boolean isFAT32()
    {
        return partitionType == 0x0b || partitionType == 0x0c;
    }

    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        bundle.putString("bootable", String.valueOf(bootable));
        bundle.putString("chsStart", String.valueOf(chsStart));
        bundle.putString("partitionType", String.valueOf(partitionType));
        bundle.putString("chsEnd", String.valueOf(chsEnd));
        bundle.putString("lbaStart", String.valueOf(lbaStart));
        bundle.putString("sectorsNumber", String.valueOf(sectorsNumber));
        return bundle;
    }

    private static boolean isEmptyPartition(byte[] partition)
    {
        return partition[4] == 0x00;
    }

    public boolean isBootable()
    {
        return bootable;
    }


    public long getChsStart()
    {
        return chsStart;
    }


    public int getPartitionType()
    {
        return partitionType;
    }


    public long getChsEnd()
    {
        return chsEnd;
    }


    public long getLbaStart()
    {
        return lbaStart;
    }


    public long getSectorsNumber()
    {
        return sectorsNumber;
    }
}
