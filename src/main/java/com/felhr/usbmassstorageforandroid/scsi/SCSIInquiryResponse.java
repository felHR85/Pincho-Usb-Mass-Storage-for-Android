package com.felhr.usbmassstorageforandroid.scsi;

import android.os.Bundle;
import android.util.Log;

import com.felhr.usbmassstorageforandroid.utilities.HexUtil;

import java.util.Arrays;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 12/12/14.
 */
public class SCSIInquiryResponse extends SCSIResponse
{
    /***
     *  Standard Inquiry Response
     *  http://www.seagate.com/staticfiles/support/disc/manuals/scsi/100293068a.pdf (page 72)
     */
    private int peripheralQualifier;
    private int peripheralDeviceType;
    private boolean removable;
    private int spcVersion;
    private boolean normaca;
    private boolean hisup;
    private int dataFormatResponse;
    private int aditionalLength;
    private boolean sccs;
    private boolean acc;
    private int tpgs;
    private boolean thirdPartyCommands;
    private boolean protect;
    private boolean bque;
    private boolean encserv;
    private boolean vs;
    private boolean multip;
    private boolean mchngr;
    private boolean addr16; // Not used with USB interface
    private boolean wbus16; // Not used with USB interface
    private boolean sync;   // Not used with USB interface
    private boolean linked;
    private boolean cmdque;
    private boolean vendorSpecific;
    private byte[] t10VendorIdentification; //8..15
    private byte[] productIdentification;   //16..31
    private byte[] productRevisionLevel;    //32..35

    private SCSIInquiryResponse()
    {

    }

    public static SCSIInquiryResponse getResponse(byte[] data)
    {
        Log.i("Buffer state", "Data to host: " + HexUtil.hexToString(data));

        SCSIInquiryResponse response = new SCSIInquiryResponse();
        response.peripheralQualifier = data[0] >> 5;
        data[0] &= ~(1 << 7);
        data[0] &= ~(1 << 6);
        data[0] &= ~(1 << 5);
        response.peripheralDeviceType = data[0];
        response.removable = data[1] == (byte) 0x80;
        response.spcVersion = data[2];
        response.normaca = (data[3] & (1 << 5)) == (1 << 5);
        response.hisup = (data[3] & (1 << 4)) == (1 << 4);
        data[3] &= ~(1 << 5);
        data[3] &= ~(1 << 4);
        response.dataFormatResponse = data[3];
        response.aditionalLength = data[4];
        response.sccs = (data[5] & (1 << 7)) == (1 << 7);
        response.acc = (data[5] & (1 << 6)) == (1 << 6);
        response.tpgs = (data[5] & (3 << 4)) >> 4;
        response.thirdPartyCommands = (data[5] & (1 << 3)) == (1 << 3);
        response.protect = (data[5] & 1) == 1;
        response.bque = (data[6] & (1 << 7)) == (1 << 7);
        response.encserv = (data[6] & (1 << 6)) == (1 << 6);
        response.vs = (data[6] & (1 << 5)) == (1 << 5);
        response.multip = (data[6] & (1 << 4)) == (1 << 4);
        response.mchngr = (data[6] & (1 << 3)) == (1 << 3);
        response.addr16 = (data[6] & 1) == 1;
        response.wbus16 = (data[7] & (1 << 5)) == (1 << 5);
        response.sync = (data[7] & (1 << 4)) == (1 << 4);
        response.linked = (data[7] & (1 << 3)) == (1 << 3);
        response.cmdque = (data[7] & (1 << 1)) == (1 << 1);
        response.vendorSpecific = (data[7] & 1) == 1;
        response.t10VendorIdentification = Arrays.copyOfRange(data, 8, 16);
        response.productIdentification = Arrays.copyOfRange(data, 16, 32);
        response.productRevisionLevel = Arrays.copyOfRange(data, 32, 35);
        return response;
    }

    @Override
    public Bundle getReadableResponse()
    {
        Bundle bundle = new Bundle();
        bundle.putString("peripheralQualifier", String.valueOf(peripheralQualifier));
        bundle.putString("peripheralDeviceType", String.valueOf(peripheralDeviceType));
        bundle.putString("removable", String.valueOf(removable));
        bundle.putString("spcVersion", String.valueOf(spcVersion));
        bundle.putString("normaca", String.valueOf(normaca));
        bundle.putString("hisup", String.valueOf(hisup));
        bundle.putString("dataFormatResponse", String.valueOf(dataFormatResponse));
        bundle.putString("aditionalLength", String.valueOf(aditionalLength));
        bundle.putString("sccs", String.valueOf(sccs));
        bundle.putString("acc", String.valueOf(acc));
        bundle.putString("tpgs", String.valueOf(tpgs));
        bundle.putString("thirdPartyCommands", String.valueOf(thirdPartyCommands));
        bundle.putString("protect", String.valueOf(protect));
        bundle.putString("bque", String.valueOf(bque));
        bundle.putString("encserv", String.valueOf(encserv));
        bundle.putString("vs", String.valueOf(vs));
        bundle.putString("multip", String.valueOf(multip));
        bundle.putString("mchngr", String.valueOf(mchngr));
        bundle.putString("addr16", String.valueOf(addr16));
        bundle.putString("wbus16", String.valueOf(wbus16));
        bundle.putString("sync", String.valueOf(sync));
        bundle.putString("linked", String.valueOf(linked));
        bundle.putString("cmdque", String.valueOf(cmdque));
        bundle.putString("vendorSpecific", String.valueOf(vendorSpecific));
        return bundle;
    }

    public int getPeripheralQualifier()
    {
        return peripheralQualifier;
    }

    public void setPeripheralQualifier(int peripheralQualifier)
    {
        this.peripheralQualifier = peripheralQualifier;
    }

    public int getPeripheralDeviceType()
    {
        return peripheralDeviceType;
    }

    public void setPeripheralDeviceType(int peripheralDeviceType)
    {
        this.peripheralDeviceType = peripheralDeviceType;
    }

    public boolean isRemovable()
    {
        return removable;
    }

    public void setRemovable(boolean removable)
    {
        this.removable = removable;
    }

    public int getSpcVersion()
    {
        return spcVersion;
    }

    public void setSpcVersion(int spcVersion)
    {
        this.spcVersion = spcVersion;
    }

    public boolean isNormaca()
    {
        return normaca;
    }

    public void setNormaca(boolean normaca)
    {
        this.normaca = normaca;
    }

    public boolean isHisup() {
        return hisup;
    }

    public void setHisup(boolean hisup)
    {
        this.hisup = hisup;
    }

    public int getDataFormatResponse()
    {
        return dataFormatResponse;
    }

    public void setDataFormatResponse(int dataFormatResponse)
    {
        this.dataFormatResponse = dataFormatResponse;
    }

    public int getAditionalLength()
    {
        return aditionalLength;
    }

    public void setAditionalLength(int aditionalLength)
    {
        this.aditionalLength = aditionalLength;
    }

    public boolean isSccs()
    {
        return sccs;
    }

    public void setSccs(boolean sccs)
    {
        this.sccs = sccs;
    }

    public boolean isAcc()
    {
        return acc;
    }

    public void setAcc(boolean acc)
    {
        this.acc = acc;
    }

    public int getTpgs()
    {
        return tpgs;
    }

    public void setTpgs(int tpgs)
    {
        this.tpgs = tpgs;
    }

    public boolean isThirdPartyCommands()
    {
        return thirdPartyCommands;
    }

    public void setThirdPartyCommands(boolean thirdPartyCommands)
    {
        this.thirdPartyCommands = thirdPartyCommands;
    }

    public boolean isProtect()
    {
        return protect;
    }

    public void setProtect(boolean protect)
    {
        this.protect = protect;
    }

    public boolean isBque()
    {
        return bque;
    }

    public void setBque(boolean bque)
    {
        this.bque = bque;
    }

    public boolean isEncserv()
    {
        return encserv;
    }

    public void setEncserv(boolean encserv)
    {
        this.encserv = encserv;
    }

    public boolean isVs()
    {
        return vs;
    }

    public void setVs(boolean vs)
    {
        this.vs = vs;
    }

    public boolean isMultip()
    {
        return multip;
    }

    public void setMultip(boolean multip)
    {
        this.multip = multip;
    }

    public boolean isMchngr()
    {
        return mchngr;
    }

    public void setMchngr(boolean mchngr)
    {
        this.mchngr = mchngr;
    }

    public boolean isAddr16()
    {
        return addr16;
    }

    public void setAddr16(boolean addr16)
    {
        this.addr16 = addr16;
    }

    public boolean isWbus16()
    {
        return wbus16;
    }

    public void setWbus16(boolean wbus16)
    {
        this.wbus16 = wbus16;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public boolean isLinked()
    {
        return linked;
    }

    public void setLinked(boolean linked)
    {
        this.linked = linked;
    }

    public boolean isCmdque()
    {
        return cmdque;
    }

    public void setCmdque(boolean cmdque)
    {
        this.cmdque = cmdque;
    }

    public boolean isVendorSpecific() {
        return vendorSpecific;
    }

    public void setVendorSpecific(boolean vendorSpecific)
    {
        this.vendorSpecific = vendorSpecific;
    }

    public byte[] getT10VendorIdentification()
    {
        return t10VendorIdentification;
    }

    public void setT10VendorIdentification(byte[] t10VendorIdentification)
    {
        this.t10VendorIdentification = t10VendorIdentification;
    }

    public byte[] getProductIdentification()
    {
        return productIdentification;
    }

    public void setProductIdentification(byte[] productIdentification)
    {
        this.productIdentification = productIdentification;
    }

    public byte[] getProductRevisionLevel()
    {
        return productRevisionLevel;
    }

    public void setProductRevisionLevel(byte[] productRevisionLevel)
    {
        this.productRevisionLevel = productRevisionLevel;
    }
}
