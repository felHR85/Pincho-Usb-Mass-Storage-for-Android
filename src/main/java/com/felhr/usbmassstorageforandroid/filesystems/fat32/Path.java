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
        path.remove(path.size()-1);
        return entry;
    }

    public List<FileEntry> getAbsolutePath()
    {
        return path;
    }

    /*
        Directory Content
     */

    public void setDirectoryContent(List<FileEntry> entries)
    {
        this.directoryContent = entries;
    }

    public List<FileEntry> getDirectoryContent()
    {
        return directoryContent;
    }

    public void clearDirectoryContent()
    {
        directoryContent.clear();
    }

}
