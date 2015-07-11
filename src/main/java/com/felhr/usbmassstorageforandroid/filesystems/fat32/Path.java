package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 3/3/15.
 */
public class Path
{
    private List<FileEntry> path;
    private List<FileEntry> directoryContent;
    private int freeEntries;

    public Path()
    {
        this.path = new ArrayList<FileEntry>();
        this.directoryContent = new ArrayList<FileEntry>();
    }

    /*
        PATH
     */
    public void addDirectory(FileEntry entry)
    {
        path.add(entry);
    }

    public FileEntry retrieveDirectory()
    {
        FileEntry entry = path.get(path.size()-1);
        path.remove(path.size() - 1);
        return entry;
    }

    public List<FileEntry> getAbsolutePath()
    {
        return path;
    }

    public FileEntry getCurrentDirectory()
    {
        if(path.size() == 0)
            return null;
        else
            return path.get(path.size() - 1);
    }

    public boolean isRoot()
    {
        return path.size() == 0;
    }

    public boolean deleteLastDir()
    {
        if(path.size() != 0)
        {
            path.remove(path.size() - 1);
            return true;
        }else
        {
            return false;
        }
    }

    /*
        Directory Content
     */
    public void setDirectoryContent(List<FileEntry> entries)
    {
        directoryContent.addAll(entries);
    }

    public void setFreeEntries(int freeEntries)
    {
        this.freeEntries = freeEntries;
    }

    public List<FileEntry> getDirectoryContent()
    {
        return directoryContent;
    }

    public void addFileEntry(FileEntry newEntry)
    {
        directoryContent.add(newEntry);
    }

    public boolean deleteFileEntry(int index)
    {
        try
        {
            directoryContent.remove(index);
            return true;
        }catch(IndexOutOfBoundsException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void clearDirectoryContent()
    {
        directoryContent.clear();
    }

    public int getFreeEntries()
    {
        return freeEntries;
    }

}
