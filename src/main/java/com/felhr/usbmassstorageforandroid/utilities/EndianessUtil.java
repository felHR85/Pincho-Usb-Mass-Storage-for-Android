package com.felhr.usbmassstorageforandroid.utilities;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 27/1/15.
 */
public class EndianessUtil
{
    private EndianessUtil()
    {

    }

    public static int swapEndianess(int i)
    {
        return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
    }
}
