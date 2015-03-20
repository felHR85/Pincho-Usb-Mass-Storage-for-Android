package com.felhr.usbmassstorageforandroid.filesystems.fat32;


import com.felhr.usbmassstorageforandroid.utilities.UnsignedUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

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
    private long firstCluster;
    private Date lastModifiedDate;
    private long size;

    private FileEntry()
    {

    }

    public static FileEntry getEntry(String longName ,byte[] data) // data 32-byte long and the longName if available
    {
        FileEntry entry = new FileEntry();

        if(longName != null)
            entry.longName = longName;
        else
            entry.longName = "";

        byte[] buffer = new byte[8];

        System.arraycopy(data, 0, buffer, 0, 8);
        int length = findNullChar(buffer);
        if(length > 0)
            entry.shortName = new String(buffer, 0, length);
        else
            entry.shortName = new String(buffer);

        System.arraycopy(data, 8, buffer, 0, 3);
        entry.fileExtension = new String(Arrays.copyOf(buffer, 3));

        System.arraycopy(data, 11, buffer, 0, 1);
        entry.attr = new Attributes(buffer[0]);

        System.arraycopy(data, 13, buffer, 0, 1);
        entry.fileCreationTime = buffer[0];

        System.arraycopy(data, 14, buffer, 0, 2);
        int timeData = (buffer[0] << 8) + buffer[1];
        System.arraycopy(data, 16, buffer, 0, 2);
        int dateData = (buffer[0] << 8) + buffer[1];
        entry.creationDate = parseCreationDate(dateData, timeData);

        System.arraycopy(data, 18, buffer, 0, 2);
        int accessedDate = (buffer[0] << 8) + buffer[1];
        entry.lastAccessedDate = parseAccessedDate(accessedDate);

        System.arraycopy(data, 20, buffer, 0, 2);
        System.arraycopy(data, 26, buffer, 2, 2);
        // High and Low word of the cluster are Little-Endian
        entry.firstCluster = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0], buffer[3], buffer[2]);

        System.arraycopy(data, 22, buffer, 0, 2);
        timeData = (buffer[0] << 8) + buffer[1];
        System.arraycopy(data, 24, buffer, 0, 2);
        dateData = (buffer[0] << 8) + buffer[1];
        entry.lastModifiedDate = parseCreationDate(dateData, timeData);

        System.arraycopy(data, 28, buffer, 0, 4);
        entry.size = UnsignedUtil.convertBytes2Long(buffer[3], buffer[2], buffer[1], buffer[0]);

        return entry;
    }

    private static Date parseCreationDate(int timeData, int dateData)
    {
        int hour = timeData >> 11;
        int minute = (timeData >> 5) & 0xff;
        int second = (timeData & 0x1f) / 2;

        int year = 1980 + (dateData >> 9);
        int month = (dateData >> 5) & 0xff;
        int day = dateData & 0x1f;

        return new GregorianCalendar(year, month, day, hour, minute, second).getTime();
    }

    private static Date parseAccessedDate(int accessedDate)
    {
        int year = 1980 + (accessedDate >> 9);
        int month = (accessedDate >> 5) & 0xff;
        int day = accessedDate & 0x1f;

        return new GregorianCalendar(year, month, day).getTime();
    }

    private static int findNullChar(byte[] data)
    {
        for(int i=0;i<=data.length-1;i++)
        {
            if(data[i] == (byte) 0x20)
            {
                return i;
            }
        }
        return 0;
    }

    private static class Attributes
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

    public String getLongName()
    {
        return longName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public String getFileExtension()
    {
        return fileExtension;
    }

    public int getFileCreationTime()
    {
        return fileCreationTime;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public Date getLastAccessedDate()
    {
        return lastAccessedDate;
    }

    public long getFirstCluster()
    {
        return firstCluster;
    }

    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public long getSize()
    {
        return size;
    }

    public boolean isReadOnly()
    {
        return attr.isReadOnly();
    }

    public boolean isHidden()
    {
        return attr.isHidden();
    }

    public boolean isSystem()
    {
        return attr.isSystem();
    }

    public boolean isVolume()
    {
        return attr.isVolume();
    }

    public boolean isDirectory()
    {
        return attr.isDirectory();
    }

    public boolean isArchive()
    {
        return attr.isArchive();
    }
}
