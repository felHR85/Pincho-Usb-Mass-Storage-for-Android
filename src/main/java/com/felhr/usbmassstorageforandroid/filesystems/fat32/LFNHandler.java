package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.UnsignedUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/7/15.
 */
public class LFNHandler
{
    private LFNHandler()
    {

    }

    public static String parseLFNEntry(byte[] lfnData)
    {
        boolean endChar = false;
        List<Byte> unicodeList = new ArrayList<Byte>();
        if((lfnData[1] != 0x00 || lfnData[2] != 0x00))
        {
            if((lfnData[1] & 0x80) ==  0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[2] << 8)) + UnsignedUtil.byteToUint(lfnData[1]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[1]);
            }
        }else
            endChar = true;

        if((lfnData[3] != 0x00 || lfnData[4] != 0x00) && !endChar)
        {
            if((lfnData[3] & 0x80) ==  0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[4] << 8)) + UnsignedUtil.byteToUint(lfnData[3]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[3]);
            }
        }else
            endChar = true;

        if((lfnData[5] != 0x00 || lfnData[6] != 0x00) && !endChar)
        {
            if((lfnData[5] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[6] << 8)) + UnsignedUtil.byteToUint(lfnData[5]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[5]);
            }
        }else
            endChar = true;

        if((lfnData[7] != 0x00 || lfnData[8] != 0x00) && !endChar)
        {
            if((lfnData[7] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[8] << 8)) + UnsignedUtil.byteToUint(lfnData[7]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[7]);
            }
        }else
            endChar = true;

        if((lfnData[9] != 0x00 || lfnData[10] != 0x00) && !endChar)
        {
            if((lfnData[9] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[10] << 8)) + UnsignedUtil.byteToUint(lfnData[9]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[9]);
            }
        }else
            endChar = true;

        if((lfnData[14] != 0x00 || lfnData[15] != 0x00) && !endChar)
        {
            if((lfnData[14] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[15] << 8)) + UnsignedUtil.byteToUint(lfnData[14]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[14]);
            }
        }else
            endChar = true;

        if((lfnData[16] != 0x00 || lfnData[17] != 0x00) && !endChar)
        {
            if((lfnData[16] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[17] << 8)) + UnsignedUtil.byteToUint(lfnData[16]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[16]);
            }
        }else
            endChar = true;

        if((lfnData[18] != 0x00 || lfnData[19] != 0x00) && !endChar)
        {
            if((lfnData[18] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[19] << 8)) + UnsignedUtil.byteToUint(lfnData[18]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[18]);
            }
        }else
            endChar = true;

        if((lfnData[20] != 0x00 || lfnData[21] != 0x00) && !endChar)
        {
            if((lfnData[20] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[21] << 8)) + UnsignedUtil.byteToUint(lfnData[20]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[20]);
            }
        }else
            endChar = true;

        if((lfnData[22] != 0x00 || lfnData[23] != 0x00) && !endChar)
        {
            if((lfnData[22] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[23] << 8)) + UnsignedUtil.byteToUint(lfnData[22]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[22]);
            }
        }else
            endChar = true;

        if((lfnData[24] != 0x00 || lfnData[25] != 0x00) && !endChar)
        {
            if((lfnData[24] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[25] << 8)) + UnsignedUtil.byteToUint(lfnData[24]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[24]);
            }
        }else
            endChar = true;

        if((lfnData[28] != 0x00 || lfnData[29] != 0x00) && !endChar)
        {
            if((lfnData[28] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[29] << 8)) + UnsignedUtil.byteToUint(lfnData[28]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[28]);
            }
        }else
            endChar = true;

        if((lfnData[30] != 0x00 || lfnData[31] != 0x00) && !endChar)
        {
            if((lfnData[30] & 0x80) == 0x80)
            {
                int codePoint = UnsignedUtil.byteToUint((byte) (lfnData[31] << 8)) + UnsignedUtil.byteToUint(lfnData[30]);
                int utf8Word = toUTF8(codePoint);
                byte low = (byte) (utf8Word & 0xff);
                byte high = (byte) ((utf8Word & 0xff00) >> 8);
                unicodeList.add(high);
                unicodeList.add(low);
            }else
            {
                unicodeList.add(lfnData[30]);
            }
        }

        byte[] unicodeBuffer = new byte[unicodeList.size()];
        int i  = 0;
        while(i <= unicodeBuffer.length -1)
        {
            unicodeBuffer[i] = unicodeList.get(i);
            i++;
        }

        try
        {
            return new String(unicodeBuffer, "UTF-8");
        }catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] createLFN(String fileName)
    {
        return null;
    }



    /*
        Encode a code point to UTF-8
     */
    private static int toUTF8(int codePoint)
    {
        int utf8Value = codePoint & 0x3f;

        if((codePoint >> 6 & 1) == 1)
        {
            utf8Value &= ~(1 << 6);
            utf8Value |= (1 << 8);
        }else
        {
            utf8Value &= ~(1 << 6);
            utf8Value &= ~(1 << 8);
        }

        if((codePoint >> 7 & 1) == 1)
        {
            utf8Value |= (1 << 7);
            utf8Value |= (1 << 9);
        }else
        {
            utf8Value |= (1 << 7);
            utf8Value &= ~(1 << 9);
        }

        if((codePoint >> 8 & 1) == 1)
            utf8Value |= (1 << 10);
        else
            utf8Value &= ~(1 << 10);

        if((codePoint >> 9 & 1) == 1)
            utf8Value |= (1 << 11);
        else
            utf8Value &= ~(1 << 11);

        if((codePoint >> 10 & 1) == 1)
            utf8Value |= (1 << 12);
        else
            utf8Value &= ~(1 << 12);

        utf8Value &= ~(1 << 13);
        utf8Value |= (1 << 14);
        utf8Value |= (1 << 15);

        return utf8Value;
    }
}
