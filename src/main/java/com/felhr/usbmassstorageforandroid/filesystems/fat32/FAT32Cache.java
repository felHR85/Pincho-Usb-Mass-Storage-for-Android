package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/8/15.
 */
public class FAT32Cache
{
    private List<Long> clusterList;
    private int index;

    public FAT32Cache()
    {
        clusterList = new ArrayList<Long>();
        index = 0;
    }

    public synchronized void addCluster(long cluster)
    {
        clusterList.add(cluster);
    }

    public synchronized void deleteCluster()
    {
        clusterList.remove(0);
    }

    public synchronized long getCluster()
    {
        if(index > clusterList.size()-1)
        {
            return 0;
        }
        long cluster = clusterList.get(index);
        index ++;
        return cluster;
    }

    public synchronized void resetIndex()
    {
        index = 0;
    }
}
