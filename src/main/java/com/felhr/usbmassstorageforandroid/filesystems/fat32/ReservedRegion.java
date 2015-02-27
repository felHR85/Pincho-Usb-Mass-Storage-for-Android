package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import android.os.Bundle;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;
import com.felhr.usbmassstorageforandroid.utilities.UnsignedUtil;

import java.util.Arrays;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 18/2/15.
 */
public class ReservedRegion
{
    /*
        Using Long as a standard to avoid negative numbers in some cases
     */

    private long jumpInstruction; // 3-bytes
    private String oemVersion; // 8-bytes
    private long bytesPerSector; // 2-bytes
    private long sectorsPerCluster; // 1-bytes
    private long numberReservedSectors; // 2-bytes
    private long fatCopies; // 1-byte
    private long mediaDescriptor; // 1-byte
    private long sectorsPerTrack;
    private long numberHeads;
    private long hiddenSectors;
    private long numberSectors;
    private long numberSectorsPerFat;
    private long flags;
    private long volumeVersion; // 2-bytes
    private long rootFirstCluster; // 4-bytes
    private long fsfInfoSectorNumber; // 2-bytes
    private long bootRecordBackupCopy; // 2-bytes
    private long logicalDriveNumberPartitions; // 1-byte
    private long extendedBootSignature; // 1-byte
    private long volumeSerialNumber; // 4-bytes
    private String volumeLabel; // 11-bytes
    private String fileSystemType; // 8-bytes

    private ReservedRegion()
    {

    }


    public static ReservedRegion getReservedRegion(byte[] data)
    {
        ReservedRegion reservedRegion = new ReservedRegion();

        byte[] buffer = new byte[11];

        System.arraycopy(data, 0, buffer, 0, 3);
        reservedRegion.jumpInstruction = UnsignedUtil.convertBytes2Long(buffer[2], buffer[1], buffer[0]);

        System.arraycopy(data, 3, buffer, 0, 8);
        reservedRegion.oemVersion = new String(Arrays.copyOf(buffer, 8));

        System.arraycopy(data, 11, buffer, 0, 2);
        reservedRegion.bytesPerSector = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 13, buffer, 0, 1);
        reservedRegion.sectorsPerCluster = UnsignedUtil.convertBytes2Long(buffer[0]);

        System.arraycopy(data, 14, buffer, 0, 2);
        reservedRegion.numberReservedSectors = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 16, buffer, 0, 1);
        reservedRegion.fatCopies = UnsignedUtil.convertBytes2Long(buffer[0]);

        System.arraycopy(data, 21, buffer, 0, 1);
        reservedRegion.mediaDescriptor = UnsignedUtil.convertBytes2Long(buffer[0]);

        System.arraycopy(data, 24, buffer, 0, 2);
        reservedRegion.sectorsPerTrack = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 26, buffer, 0, 2);
        reservedRegion.numberHeads = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 28, buffer, 0, 4);
        reservedRegion.hiddenSectors = UnsignedUtil.convertBytes2Long(buffer[3], buffer[2], buffer[1], buffer[0]);

        System.arraycopy(data, 32, buffer, 0, 4);
        reservedRegion.numberSectors = UnsignedUtil.convertBytes2Long(buffer[3], buffer[2], buffer[1], buffer[0]);

        System.arraycopy(data, 36, buffer, 0, 4);
        reservedRegion.numberSectorsPerFat = UnsignedUtil.convertBytes2Long(buffer[3], buffer[2], buffer[1], buffer[0]);

        System.arraycopy(data, 40, buffer, 0, 2);
        reservedRegion.flags = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 42, buffer, 0, 2);
        reservedRegion.volumeVersion = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 44, buffer, 0, 4);
        reservedRegion.rootFirstCluster = UnsignedUtil.convertBytes2Long(buffer[3], buffer[2], buffer[1], buffer[0]);

        System.arraycopy(data, 48, buffer, 0, 2);
        reservedRegion.fsfInfoSectorNumber = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 50, buffer, 0, 2);
        reservedRegion.bootRecordBackupCopy = UnsignedUtil.convertBytes2Long(buffer[1], buffer[0]);

        System.arraycopy(data, 64, buffer, 0, 1);
        reservedRegion.logicalDriveNumberPartitions = UnsignedUtil.convertBytes2Long(buffer[0]);

        System.arraycopy(data, 66, buffer, 0, 1);
        reservedRegion.extendedBootSignature = UnsignedUtil.convertBytes2Long(buffer[0]);

        System.arraycopy(data, 67, buffer, 0, 4);
        reservedRegion.volumeSerialNumber = UnsignedUtil.convertBytes2Long(buffer[3], buffer[2], buffer[1], buffer[0]);

        System.arraycopy(data, 71, buffer, 0, 11);
        reservedRegion.volumeLabel = new String(buffer);

        System.arraycopy(data, 82, buffer, 0, 8);
        reservedRegion.fileSystemType = new String(Arrays.copyOf(buffer, 8));

        return reservedRegion;
    }

    public Bundle getReadableReservedRegion()
    {
        Bundle bundle = new Bundle();
        bundle.putString("bytesPerSector", String.valueOf(bytesPerSector));
        bundle.putString("sectorsPerCluster", String.valueOf(sectorsPerCluster));
        bundle.putString("numberReservedSectors", String.valueOf(numberReservedSectors));
        bundle.putString("fatCopies", String.valueOf(fatCopies));
        bundle.putString("numberSectorsPerFat", String.valueOf(numberSectorsPerFat));
        bundle.putString("rootFirstCluster", String.valueOf(rootFirstCluster));
        return bundle;
    }

    public long getJumpInstruction()
    {
        return jumpInstruction;
    }

    public String getOemVersion()
    {
        return oemVersion;
    }

    public long getBytesPerSector()
    {
        return bytesPerSector;
    }

    public long getSectorsPerCluster()
    {
        return sectorsPerCluster;
    }

    public long getNumberReservedSectors()
    {
        return numberReservedSectors;
    }

    public long getFatCopies()
    {
        return fatCopies;
    }

    public long getMediaDescriptor()
    {
        return mediaDescriptor;
    }

    public long getSectorsPerTrack()
    {
        return sectorsPerTrack;
    }

    public long getNumberHeads()
    {
        return numberHeads;
    }

    public long getHiddenSectors()
    {
        return hiddenSectors;
    }

    public long getNumberSectors()
    {
        return numberSectors;
    }

    public long getNumberSectorsPerFat()
    {
        return numberSectorsPerFat;
    }

    public long getFlags()
    {
        return flags;
    }

    public long getVolumeVersion()
    {
        return volumeVersion;
    }

    public long getRootFirstCluster()
    {
        return rootFirstCluster;
    }

    public long getFsfInfoSectorNumber()
    {
        return fsfInfoSectorNumber;
    }

    public long getBootRecordBackupCopy()
    {
        return bootRecordBackupCopy;
    }

    public long getLogicalDriveNumberPartitions()
    {
        return logicalDriveNumberPartitions;
    }

    public long getExtendedBootSignature()
    {
        return extendedBootSignature;
    }

    public long getVolumeSerialNumber()
    {
        return volumeSerialNumber;
    }

    public String getVolumeLabel()
    {
        return volumeLabel;
    }

    public String getFileSystemType()
    {
        return fileSystemType;
    }
}
