package com.felhr.usbmassstorageforandroid.utilities;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 8/2/15.
 */
public class UnsignedUtil {
    private final static short MAX_UNSIGNED_BYTE_VALUE = 256;
    private final static int MAX_UNSIGNED_SHORT_VALUE = 65536;
    private final static long MAX_UNSIGNED_INT_VALUE = 4294967296L;

    private UnsignedUtil() {

    }

    public static short byteToUshort(byte value)
    {
        if (value < 0)
        {
            short complement2Value = (short) (~value + 1);
            return (short) (MAX_UNSIGNED_BYTE_VALUE - complement2Value);
        } else
        {
            return (short) value;
        }
    }

    public static byte ushortToByte(short value)
    {
        if (value <= MAX_UNSIGNED_SHORT_VALUE)
        {
            if (value >= MAX_UNSIGNED_BYTE_VALUE / 2)
            {
                return (byte) ((~(MAX_UNSIGNED_BYTE_VALUE - value)) + 1);
            } else
            {
                return (byte) value;
            }
        } else
        {
            throw new IllegalArgumentException("Value out of range for a byte");
        }
    }


    public static int shortToUint(short value)
    {
        if (value < 0)
        {
            return MAX_UNSIGNED_SHORT_VALUE - (~value + 1);
        } else
        {
            return value;
        }
    }


    public static short uintToShort(int value)
    {
        if (value <= MAX_UNSIGNED_SHORT_VALUE)
        {
            if (value >= MAX_UNSIGNED_SHORT_VALUE / 2)
            {
                return (short) (~(MAX_UNSIGNED_SHORT_VALUE - value) + 1);
            } else
            {
                return (short) value;
            }
        } else
        {
            throw new IllegalArgumentException("Value out of range for a short");
        }
    }

    public static long intToUlong(int value)
    {
        if (value < 0)
        {
            long complement2Value = (~((long) value) + 1);
            return MAX_UNSIGNED_INT_VALUE - complement2Value;
        } else
        {
            return value;
        }
    }

    public static int ulongToInt(long value)
    {
        if (value <= MAX_UNSIGNED_INT_VALUE)
        {
            if (value >= MAX_UNSIGNED_INT_VALUE / 2)
            {
                return (int) (~(MAX_UNSIGNED_INT_VALUE - value) + 1);
            } else
            {
                return (int) value;
            }
        } else
        {
            throw new IllegalArgumentException("Value out of range for a int");
        }
    }

    public static byte ulongToByte(long value) throws IllegalArgumentException
    {
        if (value <= MAX_UNSIGNED_BYTE_VALUE)
        {
            if (value >= MAX_UNSIGNED_BYTE_VALUE / 2)
            {
                long originalComplementValue = (~(MAX_UNSIGNED_BYTE_VALUE - value)) + 1;
                return (byte) originalComplementValue;
            } else
            {
                return (byte) value;
            }
        } else
        {
            throw new IllegalArgumentException("Value out of range for a byte");
        }
    }

    public static long byteToUlong(byte value)
    {
        if (value < 0)
        {
            long complement2Value = ~value + 1;
            return MAX_UNSIGNED_BYTE_VALUE - complement2Value;
        } else
        {
            return (long) value;
        }
    }

    public static int byteToUint(byte value)
    {
        if (value < 0)
        {
            int complement2Value = ~value + 1;
            return MAX_UNSIGNED_BYTE_VALUE - complement2Value;
        } else
        {
            return (int) value;
        }
    }

    public static long convertBytes2Long(byte... bytes)
    {
        long value = 0;
        int length = bytes.length;
        int firstIndex = 8 * (length - 1);
        int n = 0;
        for (byte b : bytes)
        {
            value += (byteToUlong(b) << (firstIndex - n));
            n += 8;
        }
        return value;
    }

    public static byte[] convertULong2Bytes(long value)
    {
        byte[] byteValues = new byte[4];
        int signedValue = ulongToInt(value);
        byteValues[0] = (byte) ((signedValue & 0xff000000) >> 24);
        byteValues[1] = (byte) ((signedValue & 0x00ff0000) >> 16);
        byteValues[2] = (byte) ((signedValue & 0x0000ff00) >> 8);
        byteValues[3] = (byte) (signedValue & 0x000000ff);
        return byteValues;
    }

}
