package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/8/15.
 */
public class FAT32Cache
{
    private Map<Long, byte[]> fatSectors;
    private Handler wHandler;

    public FAT32Cache(int size)
    {
        fatSectors = new HashMap<Long, byte[]>(size);
    }

}
