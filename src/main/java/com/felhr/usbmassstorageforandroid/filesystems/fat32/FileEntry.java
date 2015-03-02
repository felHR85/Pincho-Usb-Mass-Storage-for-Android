package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import java.util.Date;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 2/3/15.
 */
public class FileEntry
{
    private String longName;
    private String shortName;
    private String fileExtension;
    private Attributes attr;
    private int fileCreationTime;
    private Date creationDate;
    private Date lastAccessedDate;
    private long firstClusterDirectory;
    private Date lastModifiedDate;
    private long size;

    private FileEntry()
    {

    }

    public static FileEntry getEntry(byte[] data)
    {
        FileEntry entry = new FileEntry();

        return entry;
    }

    private class Attributes
    {
        private boolean readOnly;
        private boolean hidden;
        private boolean system;
        private boolean volume;
        private boolean directory;
        private boolean archive;

        public Attributes(byte attrByte)
        {
            readOnly = (attrByte & 1) == 1;
            hidden = (attrByte & (1 << 1)) == 2;
            system = (attrByte & (1 << 2)) == 4;
            volume = (attrByte & (1 << 3)) == 8;
            directory = (attrByte & (1 << 4)) == 16;
            archive = (attrByte & (1 << 5)) == 32;
        }

        public boolean isReadOnly()
        {
            return readOnly;
        }

        public boolean isHidden()
        {
            return hidden;
        }

        public boolean isSystem()
        {
            return system;
        }

        public boolean isVolume()
        {
            return volume;
        }

        public boolean isDirectory()
        {
            return directory;
        }

        public boolean isArchive()
        {
            return archive;
        }
    }
}
