package com.felhr.usbmassstorageforandroid.scsi;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import commandwrappers.CommandBlockWrapper;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 4/1/15.
 */
public class CommandBlockBuffer
{
    private LinkedList<CommandBlockWrapper> commands;
    private AtomicBoolean ready;

    public CommandBlockBuffer()
    {
        this.commands = new LinkedList<CommandBlockWrapper>();
        this.ready = new AtomicBoolean(true);
    }

    public synchronized void putCommand(CommandBlockWrapper command)
    {
        commands.push(command);
        notify();
    }

    public synchronized CommandBlockWrapper getCommand()
    {
        if(commands.size() == 0 || !ready.get())
        {
            waitingForCommands();
        }
        ready.set(false);
        return commands.pop();
    }

    public synchronized void goAhead()
    {
        ready.set(true);
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
