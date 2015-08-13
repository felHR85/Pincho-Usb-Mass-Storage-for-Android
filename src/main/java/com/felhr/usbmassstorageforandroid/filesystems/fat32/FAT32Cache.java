package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/8/15.
 */
public class FAT32Cache
{
    private List<Long> clusterList;

    public FAT32Cache()
    {
        clusterList = new ArrayList<Long>();
    }

    public synchronized void addCluster(long cluster)
    {
        clusterList.add(cluster);
    }

    public synchronized void deleteCluster()
    {
        if(clusterList.size() > 0)
        {
            clusterList.remove(0);
        }
    }

    public synchronized long getCluster()
    {
        if(clusterList.size() > 0)
        {
            long cluster = clusterList.get(0);
            return cluster;
        }else
        {
            return 0;
        }
    }
}
