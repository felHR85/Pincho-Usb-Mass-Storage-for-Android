package com.felhr.usbmassstorageforandroid.com.felhr.usbmassstorageforandroid.filesystems;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/2/15.
 */
public class MasterBootRecord
{
    private byte[] codeArea;
    private Partition[] partitions;

    public MasterBootRecord()
    {

    }

    private static MasterBootRecord parseMbr(byte[] data)
    {
        if(data.length == 512)
        {
            // TODO
        }else
        {
            return null; // MBR must be 512 length
        }
       return null;
    }

}
