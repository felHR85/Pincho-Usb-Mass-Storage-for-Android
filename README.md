Pincho. Usb Mass Storage for Android
============================
Pincho allows to read and write USB Mass storage devices from Android devices without worrying about rooting the device.
[Blog entry with more detailed information](http://felhr85.net/2015/07/30/pincho-a-usb-mass-storage-for-android-implementation-without-root-v0-1/)

Add the dependency
--------------------------------------
~~~
dependencies {
// Other dependencies...
compile project(':usbmassstorageforandroid-release')
}
~~~

Virtual FileSystem methods
--------------------------------------
~~~
UsbDevice mDevice;
UsbDeviceConnection mConnection;
 
// choose the device and initialize mConnection through UsbManager.openDevice(UsbDevice mDevice);
// http://developer.android.com/guide/topics/connectivity/usb/host.html
 
/*
* Constructor definition
*/
public VirtualFileSystem(UsbDevice mDevice, UsbDeviceConnection mConnection);
 
/*
* Mount operation, return true if device was successfully mounted. BLOCKING OPERATION
*/
public boolean mount(int index);
 
/*
* List only file names, return a List of Strings. NON BLOCKING OPERATION
*/
public List<String> list();
 
/*
* List complete information of files, return a List of VFSFile objects. NON BLOCKING OPERATION
*/
public List<VFSFile> listFiles();
 
/*
* Get current path, return a string linux-like formatted with the current path. NON BLOCKING OPERATION
*/
public String getPath();
 
/*
* Change dir specified by a name, folder must be inside the current folder, return true if operation was * ok. BLOCKING OPERATION.
*/
public boolean changeDir(String dirName);
 
/*
* Change dir specified by a VFSFile, folder must be inside the current folder, return true if operation was ok. BLOCKING OPERATION.
*/
public boolean changeDir(VFSFile file);
 
/*
* Change dir back, return true if operation was ok. BLOCKING OPERATION
*/   
public boolean changeDirBack();
 
/*
* Write file, return true if file was written correctly. BLOCKING OPERATION
*/
public boolean writeFile(File file); //java.io.File 
 
/*
* Read file specified by a string, return an array of bytes. BLOCKING OPERATION
*/
public byte[] readFile(String fileName);
 
/*
* Read file specified by a VFSFile, return an array of bytes. BLOCKING OPERATION
*/
public byte[] readFile(VFSFile file);
 
/*
* Delete file specified by a string, return true if file was deleted. BLOCKING OPERATION
*/
public boolean deleteFile(String fileName);
 
/*
* UnMount the USB mass storage device, return true if was unmounted correctly. BLOCKING OPERATION
*/
public boolean unMount();
~~~

SCSI interface
--------------------------------------

Define the callback
~~~
private SCSIInterface scsiInterface = new SCSIInterface()
{
    @Override
    public void onSCSIOperationCompleted(int status, int dataResidue)
    {
      // status 0: completed successfully
      // status 1: Some error occurred
    }
 
    @Override
    public void onSCSIDataReceived(SCSIResponse response)
    {
       // Data received in the data-phase.
       // Possible responses: SCSIInquiryResponse, SCSIModeSense10Response, SCSIRead10Response,
       //     SCSIReadCapacity10, SCSIReportLuns, SCSIRequestSense
    }
 
    @Override
    public void onSCSIOperationStarted(boolean status)
    {
      // SCSI operation started
    }
};
~~~

Create the SCSICommunicator object
~~~
SCSICommunicator comm;
comm = new SCSICommunicator(mDevice, mConnection);
comm.openSCSICommunicator(scsiInterface);
//..
//..
//..
comm.closeSCSICommunicator();
~~~

SCSI calls
~~~
public void read10(int rdProtect, boolean dpo, boolean fua,
                   boolean fuaNv, int logicalBlockAddress,
                   int groupNumber, int transferLength)
 
 
public void requestSense(boolean desc, int allocationLength)
 
 
public void testUnitReady()
 
 
public void write10(int wrProtect, boolean dpo, boolean fua,
                    boolean fuaNv, int logicalBlockAddress, int groupNumber,
                    int transferLength, byte[] data)
 
 
public void modeSense10(boolean llbaa, boolean dbd, int pc,
                        int pageCode, int subPageCode, int allocationLength)
 
 
public void modeSelect10(boolean pageFormat, boolean savePages, int parameterListLength)
 
 
public void formatUnit(boolean fmtpinfo, boolean rtoReq, boolean longList,
                       boolean fmtData, boolean cmplst, int defectListFormat)
 
 
public void preventAllowRemoval(int lun, boolean prevent)

~~~

License
--------------------------------------
The MIT License (MIT)

Copyright (c) 2015 Felipe Herranz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.