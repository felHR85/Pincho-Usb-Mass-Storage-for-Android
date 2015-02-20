package com.felhr.usbmassstorageforandroid.filesystems.fat32;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 20/2/15.
 */
public class FATEntry
{
    private long cluster;
    private FATEntry nextEntry;

    public FATEntry(long cluster, FATEntry nextEntry)
    {
        this.cluster = cluster;
        this.nextEntry = nextEntry;
    }

    public long getCluster()
    {
        return cluster;
    }

    public FATEntry getNextEntry()
    {
        return nextEntry;
    }
}
