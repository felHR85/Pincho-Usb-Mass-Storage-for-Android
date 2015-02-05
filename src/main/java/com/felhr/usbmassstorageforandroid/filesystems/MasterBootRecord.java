package com.felhr.usbmassstorageforandroid.filesystems;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/2/15.
 */
public class MasterBootRecord
{
    private byte[] codeArea;
    private int diskSignature;
    private Partition[] partitions;

    private MasterBootRecord()
    {

    }

    public static MasterBootRecord parseMbr(byte[] data)
    {
        if(data.length == 512)
        {
            MasterBootRecord mbr = new MasterBootRecord();
            System.arraycopy(data, 0, mbr.codeArea, 0, 440);
            byte[] signatureArea = new byte[4];
            System.arraycopy(data, 440, signatureArea, 0 ,4);
            mbr.diskSignature = signatureArea[3] << 24 + signatureArea[2] << 16 + signatureArea[1] << 8 + signatureArea[0];
            byte[] partitionsTable = new byte[64];
            System.arraycopy(data, 446, partitionsTable, 0, 64);
            mbr.partitions = parsePartitionTable(partitionsTable);
            return mbr;
        }else
        {
            return null; // MBR must be 512 length
        }
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
