package com.felhr.usbmassstorageforandroid.filesystems;

import com.felhr.usbmassstorageforandroid.filesystems.fat32.FileEntry;

import java.util.Date;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 10/6/15.
 */

/*
  VFSFile implements an abstraction over FAT entries, hiding the cluster pointer and other stuff
 */
public class VFSFile
{
    private String fileName;
    private boolean isReadOnly;
    private boolean isHidden;
    private boolean isSystem;
    private boolean isVolume;
    private boolean isDirectory;
    private boolean isArchive;
    private Date creationDate;
    private Date lastAccessedDate;
    private Date lastModifiedDate;
    private long size;

    public VFSFile()
    {

    }

    public VFSFile(String fileName, boolean isReadOnly, boolean isHidden,
                   boolean isSystem, boolean isVolume, boolean isDirectory, boolean isArchive,
                   Date creationDate, Date lastAccessedDate, Date lastModifiedDate, long size)
    {
        this.fileName = fileName;
        this.isReadOnly = isReadOnly;
        this.isHidden = isHidden;
        this.isSystem = isSystem;
        this.isVolume = isVolume;
        this.isDirectory = isDirectory;
        this.isArchive = isArchive;
        this.creationDate = creationDate;
        this.lastAccessedDate = lastAccessedDate;
        this.lastModifiedDate = lastModifiedDate;
        this.size = size;
    }

    public static VFSFile getVFSFile(FileEntry entry)
    {
        VFSFile file = new VFSFile();
        if(!entry.getLongName().equals(""))
        {
            file.setFileName(entry.getLongName());
        }else if(!entry.isDirectory() && entry.getLongName().equals("") && !entry.getShortName().equals(".") && !entry.getShortName().equals("..")
                && !entry.getFileExtension().equals(""))
        {
            file.setFileName(entry.getShortName().toLowerCase() + "." + entry.getFileExtension().toLowerCase());
        }else
        {
            file.setFileName(entry.getShortName().toLowerCase());
        }
        file.setIsReadOnly(entry.isReadOnly());
        file.setIsHidden(entry.isHidden());
        file.setIsSystem(entry.isSystem());
        file.setIsVolume(entry.isVolume());
        file.setIsDirectory(entry.isDirectory());
        file.setIsArchive(entry.isArchive());
        file.setCreationDate(entry.getCreationDate());
        file.setLastAccessedDate(entry.getLastAccessedDate());
        file.setLastModifiedDate(entry.getLastModifiedDate());
        file.setSize(entry.getSize());
        return file;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public boolean isReadOnly()
    {
        return isReadOnly;
    }

    public void setIsReadOnly(boolean isReadOnly)
    {
        this.isReadOnly = isReadOnly;
    }

    public boolean isHidden()
    {
        return isHidden;
    }

    public void setIsHidden(boolean isHidden)
    {
        this.isHidden = isHidden;
    }

    public boolean isSystem()
    {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem)
    {
        this.isSystem = isSystem;
    }

    public boolean isVolume() {
        return isVolume;
    }

    public void setIsVolume(boolean isVolume)
    {
        this.isVolume = isVolume;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setIsArchive(boolean isArchive)
    {
        this.isArchive = isArchive;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public Date getLastAccessedDate()
    {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(Date lastAccessedDate)
    {
        this.lastAccessedDate = lastAccessedDate;
    }

    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }
}