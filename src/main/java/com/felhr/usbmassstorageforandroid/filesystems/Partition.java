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
    private int chsStart; // 3-bytes
    private int partitionType; // 1-byte
    private int chsEnd; // 3-bytes
    private long lbaStart; // 4-bytes
    private int sectorsNumber; // 4-bytes

    private Partition()
    {

    }

    public static Partition parsePartition(byte[] partitionData)
    {
        if(partitionData.length == 16 && !isEmptyPartition(partitionData))
        {
            Log.i("Buffer state", HexUtil.hexToString(partitionData));
            Partition partition = new Partition();
            partition.bootable = partitionData[0] == 0x80;
            partition.chsStart = (partitionData[3] << 16) + (partitionData[2] << 8) + partitionData[1];
            partition.partitionType = partitionData[4];
            partition.chsEnd = (partitionData[7] << 16) + (partitionData[6] << 8) + partitionData[5];
            partition.lbaStart = (partitionData[11] << 24) + (partitionData[10] << 16) + (partitionData[9] << 8) + partitionData[8];
            partition.sectorsNumber = (partitionData[15] << 24) + (partitionData[14] << 16) + (partitionData[13] << 8) + partitionData[12];
            return partition;
        }else
        {
            return null;
        }
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


    public long getLbaStart()
    {
        return lbaStart;
    }


    public int getSectorsNumber()
    {
        return sectorsNumber;
    }
}
