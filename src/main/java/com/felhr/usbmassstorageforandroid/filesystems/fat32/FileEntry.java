package com.felhr.usbmassstorageforandroid.filesystems.fat32;



import com.felhr.usbmassstorageforandroid.utilities.UnsignedUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

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
        {
            entry.shortName = new String(buffer, 0, length);
        }else
        {
            entry.shortName = new String(buffer);
        }

        System.arraycopy(data, 8, buffer, 0, 3);
        entry.fileExtension = new String(Arrays.copyOf(buffer, 3));

        System.arraycopy(data, 11, buffer, 0, 1);
        entry.attr = new Attributes(buffer[0]);

        System.arraycopy(data, 13, buffer, 0, 1);
        entry.fileCreationTime = buffer[0];

        System.arraycopy(data, 14, buffer, 0, 2);

        int value0 = UnsignedUtil.byteToUint(buffer[0]);
        int value1 = UnsignedUtil.byteToUint(buffer[1]);

        int timeData = (value1 << 8) + value0;

        System.arraycopy(data, 16, buffer, 0, 2);

        int value2 = UnsignedUtil.byteToUint(buffer[0]);
        int value3 = UnsignedUtil.byteToUint(buffer[1]);
        int dateData = (value3 << 8) + value2;

        entry.creationDate = parseCreationDate(timeData, dateData);

        System.arraycopy(data, 18, buffer, 0, 2);
        int accessedDate = (buffer[0] << 8) + buffer[1];
        entry.lastAccessedDate = parseAccessedDate(accessedDate);

        System.arraycopy(data, 20, buffer, 0, 2);
        System.arraycopy(data, 26, buffer, 2, 2);
        // High and Low word of the cluster are Little-Endian
        entry.firstCluster = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0], buffer[3], buffer[2]);

        System.arraycopy(data, 22, buffer, 0, 2);
        value0 = UnsignedUtil.byteToUint(buffer[0]);
        value1 = UnsignedUtil.byteToUint(buffer[1]);
        timeData = (value1 << 8) + value0;

        System.arraycopy(data, 24, buffer, 0, 2);
        value2 = UnsignedUtil.byteToUint(buffer[0]);
        value3 = UnsignedUtil.byteToUint(buffer[1]);
        dateData =  (value3 << 8) + value2;

        entry.lastModifiedDate = parseCreationDate(timeData, dateData);

        System.arraycopy(data, 28, buffer, 0, 4);
        entry.size = UnsignedUtil.convertBytes2Long(buffer[3], buffer[2], buffer[1], buffer[0]);

        return entry;
    }

    public static FileEntry getEntry(String name, long firstCluster, long size, List<FileEntry> files,
                                     boolean isRead, boolean isHidden, boolean isdirectory, long lastModified)
    {
        /*
            System, volume and archive attributes not added yet because not accesible from Android API
         */
        FileEntry entry = new FileEntry();
        if(files != null)
        {
            String[] fileAndExtension = get8dot3NameExtension(name, files);
            entry.shortName = fileAndExtension[0];
            entry.fileExtension = fileAndExtension[1];
            entry.longName = name;
        }else // dot and dot-dot entries
        {
            entry.shortName = name;
            entry.fileExtension = "";
            entry.longName = "";
        }
        entry.firstCluster = firstCluster;
        entry.size = size;
        entry.attr = new Attributes();
        entry.attr.setReadOnly(isRead);
        entry.attr.setHidden(isHidden);
        entry.attr.setDirectory(isdirectory);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lastModified);
        /*
            Only lastModifiedDate can be accessed through Java API.
         */
        entry.lastModifiedDate = cal.getTime();
        entry.creationDate = cal.getTime();
        entry.lastAccessedDate = cal.getTime();
        entry.fileCreationTime = 0;

        return entry;
    }

    private static Date parseCreationDate(int timeData, int dateData)
    {
        int hour = timeData >> 11 ;
        int minute = (timeData >> 5) & 0x3f;
        int second = (timeData & 0x1f) / 2;


        int year = 1980 + (dateData >> 9);
        int month = (dateData >> 5) & 0x0f;
        int day = dateData & 0x1f;

        GregorianCalendar creationDate = new GregorianCalendar();
        creationDate.setTimeZone(TimeZone.getDefault());
        creationDate.set(year, --month, day, hour, minute, second);
        return creationDate.getTime();
    }

    private static Date parseAccessedDate(int accessedDate)
    {
        int year = 1980 + (accessedDate >> 9);
        int month = (accessedDate >> 5) & 0x0f;
        int day = accessedDate & 0x1f;

        GregorianCalendar accessDate = new GregorianCalendar();
        accessDate.setTimeZone(TimeZone.getDefault());
        accessDate.set(year, --month, day);
        return accessDate.getTime();
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

    public byte[] getRawFileEntry()
    {
        byte[] rawLongName = getRawLongName();
        byte[] rawFileEntry = new byte[rawLongName.length + 32];
        int index =0;

        System.arraycopy(rawLongName, 0, rawFileEntry, index, rawLongName.length);
        index = rawLongName.length;
        if(shortName.length() == 8)
            System.arraycopy(shortName.getBytes(), 0, rawFileEntry, index, 8);
        else
        {
            byte[] shortNameRawExpanded = new byte[]{0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
            byte[] shortNameRaw = shortName.getBytes();
            System.arraycopy(shortNameRaw, 0, shortNameRawExpanded, 0, shortNameRaw.length);
            System.arraycopy(shortNameRawExpanded, 0, rawFileEntry, index, 8);
        }
        index += 8;
        if(fileExtension.length() == 3)

            System.arraycopy(fileExtension.getBytes(), 0, rawFileEntry, index, 3);
        else
            System.arraycopy(new byte[]{0x20, 0x20, 0x20}, 0, rawFileEntry, index, 3);
        index += 3;
        System.arraycopy(new byte[]{attr.getAttrByte()}, 0, rawFileEntry, index, 1);
        index += 1;
        System.arraycopy(new byte[]{0x00}, 0, rawFileEntry, index, 1);
        index += 1;
        System.arraycopy(new byte[]{(byte)fileCreationTime}, 0, rawFileEntry, index, 1);
        index += 1;

        /*
          get Raw creation Date
         */
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(creationDate);
        int value = 0;
        int hours = dateCal.get(Calendar.HOUR_OF_DAY);
        int minutes = dateCal.get(Calendar.MINUTE);
        int seconds = dateCal.get(Calendar.SECOND) / 2;

        value += (hours << 11);
        value += (minutes << 5) & 0x07e0;
        value += (seconds & 0x1f);

        byte lsb = (byte) (value & 0xff);
        byte msb = (byte) (value >> 8);

        System.arraycopy(new byte[]{lsb, msb}, 0, rawFileEntry, index, 2);
        index += 2;
        value = 0;
        int years = dateCal.get(Calendar.YEAR) - 1980;
        int month = dateCal.get(Calendar.MONTH) + 1;
        int day = dateCal.get(Calendar.DAY_OF_MONTH);

        value += (years << 9);
        value += (month << 5) & 0x1e0;
        value += (day & 0x1f);

        lsb = (byte) (value & 0xff);
        msb = (byte) (value >> 8);

        System.arraycopy(new byte[]{lsb, msb}, 0, rawFileEntry, index, 2);
        index += 2;
        value = 0;

        /*
          get Raw Last accesed date
         */
        dateCal.setTime(lastAccessedDate);
        years = dateCal.get(Calendar.YEAR) - 1980;
        month = dateCal.get(Calendar.MONTH) + 1;
        day = dateCal.get(Calendar.DAY_OF_MONTH);

        value += (years << 9);
        value += (month << 5) & 0x1e0;
        value += (day & 0x1f);

        lsb = (byte) (value & 0xff);
        msb = (byte) (value >> 8);

        System.arraycopy(new byte[]{lsb, msb}, 0, rawFileEntry, index, 2);
        index += 2;
        value = 0;

        /*
          get high word cluster
         */
        int cluster = UnsignedUtil.ulongToInt(firstCluster);
        msb = (byte) (cluster >> 24);
        lsb = (byte) (cluster >> 16 & 0xff);
        System.arraycopy(new byte[]{lsb, msb}, 0, rawFileEntry, index, 2);
        index += 2;

        /*
          get last modified date
         */
        dateCal.setTime(lastModifiedDate);
        hours = dateCal.get(Calendar.HOUR_OF_DAY);
        minutes = dateCal.get(Calendar.MINUTE);
        seconds = dateCal.get(Calendar.SECOND) / 2;

        value += (hours << 11);
        value += (minutes << 5) & 0x07e0;
        value += (seconds & 0x1f);

        lsb = (byte) (value & 0xff);
        msb = (byte) (value >> 8);

        System.arraycopy(new byte[]{lsb, msb}, 0, rawFileEntry, index, 2);
        index += 2;
        value = 0;
        years = dateCal.get(Calendar.YEAR) - 1980;
        month = dateCal.get(Calendar.MONTH) + 1;
        day = dateCal.get(Calendar.DAY_OF_MONTH);

        value += (years << 9);
        value += (month << 5)  & 0x1e0;
        value += (day & 0x1f);

        lsb = (byte) (value & 0xff);
        msb = (byte) (value >> 8);

        System.arraycopy(new byte[]{lsb, msb}, 0, rawFileEntry, index, 2);
        index += 2;

        /*
         get low word cluster
         */
        msb = (byte) (cluster >> 8 & 0xff);
        lsb = (byte) (cluster & 0xff);
        System.arraycopy(new byte[]{lsb, msb}, 0, rawFileEntry, index, 2);
        index += 2;

        /*
         get file size
         */
        int uSize = UnsignedUtil.ulongToInt(size);
        byte b1 = (byte) (uSize >> 24);
        byte b2 = (byte) (uSize >> 16 & 0xff);
        byte b3 = (byte) (uSize >> 8 & 0xff);
        byte b4 = (byte) (uSize & 0xff);
        System.arraycopy(new byte[]{b4, b3, b2, b1}, 0, rawFileEntry, index, 4);

        return rawFileEntry;
    }


    public byte[] getRawLongName() //Get all LFN entries of a Long name
    {
        int numberOfLfn = (longName.length() / 13);
        if(longName.length() % 13 != 0)
            numberOfLfn += 1;

        String[] splitStrings = splitLongName();
        byte[] lfnBuffer = new byte[numberOfLfn * 32];
        byte ordinalEntry = (byte) (numberOfLfn + 0x40);
        int charIndex = 0;
        boolean endOfString = false;
        int n = 0;
        for(int k=0;k<=numberOfLfn-1;k++)
        {
            String sub = splitStrings[numberOfLfn - k - 1];
            while(n <= 31)
            {
                if(n == 0) // Ordinal Field
                {
                    lfnBuffer[32 * k + n] = ordinalEntry;
                    if(ordinalEntry > 0x40)
                        ordinalEntry -= 0x40;
                    ordinalEntry--;
                    n++;
                }else if(n == 11) // Attributes
                {
                    lfnBuffer[32 * k + n] = (byte) 0x0f;
                    n++;
                }else if(n == 12) // Type
                {
                    lfnBuffer[32 * k + n] = (byte) 0x00;
                    n++;
                }else if(n == 13) // Checksum
                {
                    lfnBuffer[32 * k + n] = createCheckSum();
                    n++;
                }else if(n == 26) // Cluster MSB, must equal 0
                {
                    lfnBuffer[32 * k + n] = (byte) 0x00;
                    n++;
                }else if(n == 27) // Cluster LSB, must equal 0
                {
                    lfnBuffer[32 * k + n] = (byte) 0x00;
                    n++;
                }else if(endOfString) // No more characters, fill with 0xFF
                {
                    lfnBuffer[32 * k + n] = (byte) 0xff;
                    n++;
                }else // Unicode characters
                {
                    if(charIndex <= sub.length()-1)
                    {
                        char nChar = sub.charAt(charIndex);
                        byte msb = (byte) (nChar >> 8);
                        byte lsb = (byte) (nChar & 0xff);
                        lfnBuffer[32 * k + n] = lsb;
                        lfnBuffer[32 * k + (n + 1)] = msb;
                        charIndex++;
                        n += 2;
                    }else if(charIndex > sub.length()-1 && !endOfString)
                    {
                        endOfString = true;
                        lfnBuffer[32 * k + n] = (byte) 0x00;
                        lfnBuffer[32 * k + (n + 1)] = (byte) 0x00;
                        n += 2;
                    }
                }
            }
            charIndex = 0;
            n = 0;
            endOfString = false;
        }
        return lfnBuffer;
    }

    private String[] splitLongName()
    {
        int numberOfSubs = (longName.length() / 13) + 1;
        String[] splitLongName = new String[numberOfSubs];
        int n = 0;
        int index1 = n * 13;
        int index2 = index1 + 14;
        for(int i=0;i<=numberOfSubs-1;i++)
        {
            if(index2 <= longName.length())
            {
                splitLongName[i] = longName.substring(index1, index2);
                n++;
                index1 = n * 13;
                index2 = index1 + 14;
            }else
            {
                splitLongName[i] = longName.substring(index1, longName.length());
            }
        }

        return splitLongName;
    }


    private byte createCheckSum()
    {
        String completeShortName;
        if(fileExtension.length() != 0)
            completeShortName = shortName + fileExtension;
        else
            completeShortName = shortName;

        int bit7;
        int checksum = 0;
        for(int character=0;character < completeShortName.length();character++)
        {
            if((1 & checksum) != 0)
                bit7 = 0x80;
            else
                bit7 = 0x00;

            checksum = checksum >> 1;
            checksum = checksum | bit7;
            checksum = checksum + (int) completeShortName.charAt(character);
            checksum = checksum & 0xff;
        }

        return (byte) checksum;
    }

    public static String[] get8dot3NameExtension(String nameFile, List<FileEntry> files)
    {
        String[] fileAndExtension = new String[2];

        if(nameFile.length() > 8)
        {
            String strStep1 = nameFile.replaceAll("\\s+", "");
            boolean first = false;
            StringBuilder strStep2Builder = new StringBuilder(strStep1);
            for(int i=strStep1.length()-1;i>=0;i--)
            {
                if(strStep1.charAt(i) == '.' && !first)
                {
                    first = true;
                }else if(strStep1.charAt(i) == '.' && first)
                {
                    strStep2Builder.deleteCharAt(i);
                }
            }

            String[] strStep3 = strStep2Builder.toString().split("\\.");
            String truncatedName= "";
            String truncatedExtension = "";

            if(strStep3[0].length() <= 8) // Not need to create a unique shortFileName
            {
                truncatedName = strStep3[0].substring(0, strStep3[0].length());
                if(strStep3.length > 1)
                {
                    if (strStep3[1].length() <= 3)
                        truncatedExtension = strStep3[1].substring(0, strStep3[1].length());
                    else
                        truncatedExtension = strStep3[1].substring(0, 3);
                }else // Dir or file without extension
                {
                    truncatedExtension = "";
                }

                truncatedName = truncatedName.replaceAll("[^\\p{ASCII}]", "_");
                truncatedExtension = truncatedExtension.replaceAll("[^\\p{ASCII}]", "_");
                truncatedName = truncatedName.toUpperCase();
                truncatedExtension = truncatedExtension.toUpperCase();

                fileAndExtension[0] = truncatedName;
                fileAndExtension[1] = truncatedExtension;

                return fileAndExtension;
            }else // Create a unique shortFileName
            {
                truncatedName = strStep3[0].substring(0, 6);
                if(strStep3.length > 1)
                {
                    if (strStep3[1].length() <= 3)
                        truncatedExtension = strStep3[1].substring(0, strStep3[1].length());
                    else
                        truncatedExtension = strStep3[1].substring(0, 3);
                }else
                {
                    truncatedExtension = "";
                }

                truncatedName = truncatedName.replaceAll("[^\\p{ASCII}]", "_");
                truncatedExtension = truncatedExtension.replaceAll("[^\\p{ASCII}]", "_");
                truncatedName = truncatedName.toUpperCase();
                truncatedExtension = truncatedExtension.toUpperCase();

                if(files != null)
                {
                    int counter = 1;
                    Iterator<FileEntry> e = files.iterator();
                    while(e.hasNext())
                    {
                        FileEntry next = e.next();
                        if(next.getShortName().length() == 8 && next.getShortName().substring(0, 6).equals(truncatedName))
                            counter++;
                    }

                    truncatedName += "~" + String.valueOf(counter);

                }else
                {
                    truncatedName += "~" + "1";
                }

                fileAndExtension[0] = truncatedName;
                fileAndExtension[1] = truncatedExtension;
                return fileAndExtension;
            }

        }else
        {
            String strNoSpaces =  nameFile.replaceAll("\\s+", "");
            String strAscii = strNoSpaces.replaceAll("[^\\p{ASCII}]", "_");
            String[] strTruncated = strAscii.split("\\.");
            fileAndExtension[0] = strTruncated[0];
            fileAndExtension[1] = strTruncated[1];
            return fileAndExtension;
        }
    }

    private static class Attributes
    {
        private boolean readOnly;
        private boolean hidden;
        private boolean system;
        private boolean volume;
        private boolean directory;
        private boolean archive;
        private byte attrByte;

        public Attributes()
        {
            this.attrByte = 0x00;
        }

        public Attributes(byte attrByte)
        {
            this.attrByte = attrByte;
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

        public byte getAttrByte()
        {
            return attrByte;
        }

        public void setReadOnly(boolean readOnly)
        {
            this.readOnly = readOnly;
            if(readOnly)
                attrByte |= 1;
        }

        public void setHidden(boolean hidden)
        {
            this.hidden = hidden;
            if(hidden)
                attrByte |= (1 << 1);
        }

        public void setSystem(boolean system)
        {
            this.system = system;
        }

        public void setVolume(boolean volume)
        {
            this.volume = volume;
        }

        public void setDirectory(boolean directory)
        {
            this.directory = directory;
            if(directory)
                attrByte |= (1 << 4);
        }

        public void setArchive(boolean archive)
        {
            this.archive = archive;
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