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
    private FATHandler fatHandler;

    public VirtualFileSystem(UsbDevice mDevice, UsbDeviceConnection mConnection)
    {
        fatHandler = new FATHandler(mDevice, mConnection);
    }

    public boolean mount(int index)
    {
        return fatHandler.mount(index);
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
        fatHandler.stopCaching();
        boolean result = fatHandler.changeDir(dirName);
        fatHandler.continueCaching();
        return result;
    }

    public boolean changeDir(VFSFile file)
    {
        fatHandler.stopCaching();
        boolean result = fatHandler.changeDir(file.getFileName());
        fatHandler.continueCaching();
        return result;
    }

    public boolean changeDirBack()
    {
        fatHandler.stopCaching();
        boolean result = fatHandler.changeDirBack();
        fatHandler.continueCaching();
        return result;
    }

    public boolean writeFile(File file)
    {
        fatHandler.stopCaching();
        boolean result = fatHandler.writeNewFile(file);
        fatHandler.continueCaching();
        return result;
    }

    public byte[] readFile(String fileName)
    {
        fatHandler.stopCaching();
        byte[] data = fatHandler.readFile(fileName);
        fatHandler.continueCaching();
        return data;
    }

    public byte[] readFile(VFSFile file)
    {
        fatHandler.stopCaching();
        byte[] data = fatHandler.readFile(file.getFileName());
        fatHandler.continueCaching();
        return data;
    }

    public boolean deleteFile(String fileName)
    {
        fatHandler.stopCaching();
        boolean result = fatHandler.deleteFile(fileName);
        fatHandler.continueCaching();
        return result;
    }

    public boolean unMount()
    {
        fatHandler.stopCaching();
        return fatHandler.unMount();
    }
}
