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
            int codePoint = (UnsignedUtil.byteToUint(lfnData[2]) << 8) + UnsignedUtil.byteToUint(lfnData[1]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[3] != 0x00 || lfnData[4] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[4]) << 8) + UnsignedUtil.byteToUint(lfnData[3]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[5] != 0x00 || lfnData[6] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[6]) << 8) + UnsignedUtil.byteToUint(lfnData[5]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[7] != 0x00 || lfnData[8] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[8]) << 8) + UnsignedUtil.byteToUint(lfnData[7]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[9] != 0x00 || lfnData[10] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[10]) << 8) + UnsignedUtil.byteToUint(lfnData[9]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[14] != 0x00 || lfnData[15] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[15]) << 8) + UnsignedUtil.byteToUint(lfnData[14]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[16] != 0x00 || lfnData[17] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[17]) << 8) + UnsignedUtil.byteToUint(lfnData[16]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[18] != 0x00 || lfnData[19] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[19]) << 8) + UnsignedUtil.byteToUint(lfnData[18]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[20] != 0x00 || lfnData[21] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[21]) << 8) + UnsignedUtil.byteToUint(lfnData[20]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[22] != 0x00 || lfnData[23] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[23]) << 8) + UnsignedUtil.byteToUint(lfnData[22]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[24] != 0x00 || lfnData[25] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[25]) << 8) + UnsignedUtil.byteToUint(lfnData[24]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[28] != 0x00 || lfnData[29] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[29]) << 8) + UnsignedUtil.byteToUint(lfnData[28]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
            }
        }else
            endChar = true;

        if((lfnData[30] != 0x00 || lfnData[31] != 0x00) && !endChar)
        {
            int codePoint = (UnsignedUtil.byteToUint(lfnData[31]) << 8) + UnsignedUtil.byteToUint(lfnData[30]);
            byte[] utf8Data = toUTF8(codePoint);
            for(byte b : utf8Data)
            {
                unicodeList.add(b);
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

    public static byte[] getRawLongName(String longName, String shortName, String fileExtension)
    {
        int numberOfLfn = (longName.length() / 13);
        if(longName.length() % 13 != 0)
            numberOfLfn += 1;

        String[] splitStrings = splitLongName(longName);
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
                    lfnBuffer[32 * k + n] = createCheckSum(shortName, fileExtension);
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
                        int codePoint = sub.codePointAt(charIndex);
                        byte msb = (byte) (codePoint >> 8);
                        byte lsb = (byte) (codePoint & 0xff);
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


    /*
        Encode two-byte codepoints to UTF-8
     */

    private static byte[] toUTF8(int codepoint)
    {
        byte[] utf8Data = null;

        if(codepoint <= 127) // 1-byte encoding
        {
            utf8Data = new byte[]{(byte) codepoint};
            return utf8Data;
        }else if(codepoint <= 2047) // 2-byte encoding
        {
            int utf8Value = codepoint & 0x3f;
            utf8Data = new byte[2];

            utf8Value &= ~(1 << 6);
            utf8Value |= (1 << 7);
            utf8Value &= ~(1 << 13);
            utf8Value |= (1 << 14);
            utf8Value |= (1 << 15);

            for(int i=6;i<=10;i++)
            {
                if((codepoint >> i & 1) == 1)
                {
                    utf8Value |= (1 << (i+2));
                }
            }

            byte low = (byte) (utf8Value & 0xff);
            byte high = (byte) ((utf8Value & 0xff00) >> 8);
            utf8Data[0] = high;
            utf8Data[1] = low;
        }else // 3-byte encoding
        {
            int utf8Value = codepoint & 0x3f;
            utf8Data = new byte[3];

            utf8Value &= ~(1 << 6);
            utf8Value |= (1 << 7);
            utf8Value &= ~(1 << 14);
            utf8Value |= (1 << 15);

            utf8Value &= ~(1 << 20);
            utf8Value |= (1 << 21);
            utf8Value |= (1 << 22);
            utf8Value |= (1 << 23);

            for(int i=6;i<=15;i++)
            {
                if((codepoint >> i & 1) == 1 && (i < 12))
                {
                    utf8Value |= (1 << (i+2));
                }else if((codepoint >> i & 1) == 1 && (i >= 12))
                {
                    utf8Value |= (1 << (i+4));
                }
            }
            byte low = (byte) (utf8Value & 0xff);
            byte mid = (byte) ((utf8Value & 0xff00) >> 8);
            byte high = (byte) ((utf8Value & 0xff0000) >> 16);
            utf8Data[0] = high;
            utf8Data[1] = mid;
            utf8Data[2] = low;
        }
        return utf8Data;
    }

    private static String[] splitLongName(String longName)
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

    private static byte createCheckSum(String shortName, String fileExtension)
    {
        String completeShortName;
        if(fileExtension.length() != 0)
            completeShortName = shortName + fileExtension;
        else
            completeShortName = shortName;

        if(completeShortName.length() < 11)
        {
            byte[] spaces = new byte[11 - completeShortName.length()];
            for(int i=0;i<=spaces.length-1;i++)
            {
                spaces[i] = 0x20;
            }
            completeShortName += new String(spaces);
        }

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
}
