package com.felhr.usbmassstorageforandroid.scsi;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/1/15.
 */
public class SCSICommandBuffer
{
    private AtomicBoolean waiting;
    private LinkedList<SCSICommand> commands;

    public SCSICommandBuffer()
    {
        this.waiting = new AtomicBoolean(true);
        this.commands = new LinkedList<SCSICommand>();
    }

    public synchronized void putCommand(SCSICommand command)
    {
        commands.push(command);
        waiting.set(false);
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
        waiting.set(false);
        notify();
    }

    private void waitingForCommands()
    {
        while(waiting.get())
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        waiting.set(true);
    }

}
