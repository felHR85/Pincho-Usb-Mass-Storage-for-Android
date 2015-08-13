package com.felhr.usbmassstorageforandroid.filesystems;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.felhr.usbmassstorageforandroid.filesystems.fat32.FATHandler;
import com.felhr.usbmassstorageforandroid.filesystems.fat32.FileEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 10/6/15.
 */
public class VirtualFileSystem
{
    public static final int CACHE_NONE = 0; // No cache
    public static final int CACHE_LOW = 1; // Cache for a 100 Mbytes allocation
    public static final int CACHE_MEDIUM = 2; // Cache half of the FAT
    public static final int CACHE_HIGH = 3; // Cache the whole FAT

    private FATHandler fatHandler;

    public VirtualFileSystem(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        fatHandler = new FATHandler(mDevice, mConnection);
    }

    public boolean mount(int index)
    {
        return fatHandler.mount(index, CACHE_NONE);
    }

    public boolean mount(int index, int cacheMode)
    {
        return fatHandler.mount(index, cacheMode);
    }

    public List<String> list()
    {
        List<FileEntry> entries = fatHandler.list();
        List<String> files = new ArrayList<String>();
        Iterator<FileEntry> e = entries.iterator();
        while(e.hasNext())
        {
            FileEntry entry = e.next();
            if(!entry.getLongName().equals(""))
            {
                files.add(entry.getLongName());
            }else
            {
                files.add(entry.getShortName());
            }
        }
        return files;
    }

    public List<VFSFile> listFiles()
    {
        List<FileEntry> entries = fatHandler.list();
        List<VFSFile> files = new ArrayList<VFSFile>();
        Iterator<FileEntry> e = entries.iterator();
        while(e.hasNext())
        {
            FileEntry entry = e.next();
            VFSFile file = VFSFile.getVFSFile(entry);
            files.add(file);
        }
        return files;
    }

    public String getPath()
    {
        String path = "/";
        List<FileEntry> pathDirsEntries = fatHandler.getPath();
        if(pathDirsEntries.isEmpty())
            return path;
        Iterator<FileEntry> e = pathDirsEntries.iterator();
        while(e.hasNext())
        {
            FileEntry entry = e.next();
            if(!entry.getLongName().equals(""))
                path += entry.getLongName();
            else
                path += entry.getShortName();
            path += "/";
        }
        return path;
    }

    public boolean changeDir(String dirName)
    {
        boolean result = fatHandler.changeDir(dirName);
        return result;
    }

    public boolean changeDir(VFSFile file)
    {
        boolean result = fatHandler.changeDir(file.getFileName());
        return result;
    }

    public boolean changeDirBack()
    {
        boolean result = fatHandler.changeDirBack();
        return result;
    }

    public boolean writeFile(File file)
    {
        boolean result = fatHandler.writeNewFile(file);
        return result;
    }

    public byte[] readFile(String fileName)
    {
        byte[] data = fatHandler.readFile(fileName);
        return data;
    }

    public byte[] readFile(VFSFile file)
    {
        byte[] data = fatHandler.readFile(file.getFileName());
        return data;
    }

    public boolean deleteFile(String fileName)
    {
        boolean result = fatHandler.deleteFile(fileName);
        return result;
    }

    public boolean unMount()
    {
        return fatHandler.unMount();
    }
}
