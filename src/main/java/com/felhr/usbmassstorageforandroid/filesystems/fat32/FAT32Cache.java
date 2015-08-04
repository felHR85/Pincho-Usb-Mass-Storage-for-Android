package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/8/15.
 */
public class FAT32Cache
{
    private static final int LOAD_CACHE = 0;
    private static final int FIND_EMPTY_CLUSTERCHAIN = 1;

    private Map<Long, byte[]> fatSectors;
    private CacheThread cacheThread;
    private Handler wHandler;

    public FAT32Cache(int size)
    {
        fatSectors = new HashMap<Long, byte[]>(size);
        cacheThread = new CacheThread();
        cacheThread.start();
    }


    private class CacheThread extends Thread
    {
        @Override
        public void run()
        {
            Looper.prepare();
            wHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    switch(msg.what)
                    {
                        case LOAD_CACHE:
                            break;
                        case FIND_EMPTY_CLUSTERCHAIN:
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }
}
