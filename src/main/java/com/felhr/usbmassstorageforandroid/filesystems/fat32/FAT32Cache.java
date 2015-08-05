package com.felhr.usbmassstorageforandroid.filesystems.fat32;

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

    public void addCluster(long cluster)
    {
        clusterList.add(cluster);
    }

    public void deleteCluster()
    {
        clusterList.remove(0);
    }
}
