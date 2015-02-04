package com.felhr.usbmassstorageforandroid.filesystems;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/2/15.
 */
public class MasterBootRecord
{
    private byte[] codeArea;
    private int diskSignature;
    private Partition[] partitions;

    public MasterBootRecord()
    {

    }

    public static MasterBootRecord parseMbr(byte[] data)
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

    private static Partition[] parsePartitionTable(byte[] partitionTable)
    {
        Partition[] partitions = new Partition[getPartitionsCount(partitionTable)];
        int partitionIndex = 0;
        if(partitionTable.length == 64)
        {
            for(int startIndex = 0;startIndex<= 48;startIndex+=16)
            {
                byte[] partitionData = new byte[16];
                System.arraycopy(partitionTable, startIndex, partitionData, 0, 16);
                Partition partition = Partition.parsePartition(partitionData);
                if(partition != null)
                    partitions[partitionIndex] = partition;
            }
            return partitions;

        }else
        {
            return null; // Partition table must be 64 byte length
        }
    }

    private static int getPartitionsCount(byte[] partitionTable)
    {
        int index = 0;
        int counter = 0;
        while(index <= 48)
        {
            if(partitionTable[index + 4] != 0x00)
                counter++;
            index += 16;
        }
        return counter;
    }

}
