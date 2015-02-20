package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 20/2/15.
 */
public class FAT
{
    private List<FATEntry> entries;

    public FAT()
    {
        this.entries = new ArrayList<FATEntry>();
    }

    public void updateFATList(List<FATEntry> entries)
    {
        this.entries = entries;
    }

}
