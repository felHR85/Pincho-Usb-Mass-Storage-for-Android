package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;
import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 16/12/14.
 */
public class SCSIRead10Response extends SCSIResponse
{
    private DynamicBuffer readBuffer;

    private SCSIRead10Response()
    {
        this.readBuffer = new DynamicBuffer(512, 2);
    }

    public static SCSIRead10Response getResponse(byte[] data)
    {
        SCSIRead10Response response = new SCSIRead10Response();
        response.addToBuffer(data);
        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        byte[] buffer = readBuffer.getBuffer();
        Log.i("length buffer", String.valueOf(buffer.length));
        bundle.putString("readBuffer", HexUtil.hexToString(buffer));
        return bundle;
    }

    private void addToBuffer(byte[] data)
    {
        readBuffer.addElements(data);
    }

    private byte[] getBuffer()
    {
        return readBuffer.getBuffer();
    }

    private class DynamicBuffer
    {
        private byte[] buffer;
        private int pointer;
        private int resizeFactor;

        public DynamicBuffer(int size, int resizeFactor)
        {
            this.buffer = new byte[size];
            this.pointer = 0;
            this.resizeFactor = resizeFactor;
        }

        public void addElements(byte[] newBuffer)
        {
            if((pointer + newBuffer.length) <= buffer.length)
            {
                System.arraycopy(newBuffer, 0, buffer, pointer, newBuffer.length);
                pointer = pointer + newBuffer.length;
            }else // Resize buffer
            {
                byte[] resizedBuffer = new byte[resizeFactor * buffer.length];
                pointer = buffer.length;
                System.arraycopy(buffer, 0, resizedBuffer, 0, buffer.length);
                buffer = resizedBuffer;
            }
        }

        public byte[] getBuffer()
        {
            return buffer;
        }

        private void resizeBuffer()
        {

        }
    }
}
