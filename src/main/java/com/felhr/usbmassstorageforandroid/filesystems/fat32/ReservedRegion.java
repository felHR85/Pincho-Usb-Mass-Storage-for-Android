package com.felhr.usbmassstorageforandroid.filesystems.fat32;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 18/2/15.
 */
public class ReservedRegion
{
    private ReservedRegion()
    {

    }

    public static ReservedRegion getReservedRegion(byte[] data)
    {
        ReservedRegion reservedRegion = new ReservedRegion();

        return reservedRegion;
    }
}
