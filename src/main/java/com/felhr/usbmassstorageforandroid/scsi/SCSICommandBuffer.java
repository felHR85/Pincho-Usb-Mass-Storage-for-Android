package com.felhr.usbmassstorageforandroid.scsi;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/1/15.
 */
public class SCSICommandBuffer
{
    private LinkedList<SCSICommand> commands;

    public SCSICommandBuffer()
    {
        this.commands = new LinkedList<SCSICommand>();
    }

    public synchronized void putCommand(SCSICommand command)
    {
        commands.push(command);
        notify();
    }

    public synchronized SCSICommand getCommand()
    {
        while(commands.size() == 0)
        {
            waitingForCommands();
        }
        return commands.pop();
    }

    public synchronized void goAhead()
    {
        notify();
    }

    private void waitingForCommands()
    {
        try
        {
            wait();
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
