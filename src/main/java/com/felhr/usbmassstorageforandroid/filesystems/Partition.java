package com.felhr.usbmassstorageforandroid.filesystems;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/2/15.
 */
public class Partition
{
    private boolean bootable;
    private int cylinderHeadSectorAddressFirst;
    private int partitionType;
    private int cylinderHeadSectorAddressLast;
    private int lbaStart;
    private int sectorsNumber;

    public Partition()
    {

    }

    public static Partition parsePartitionTable(byte[] partitionTable)
    {
        // TODO
        return null;
    }
}
