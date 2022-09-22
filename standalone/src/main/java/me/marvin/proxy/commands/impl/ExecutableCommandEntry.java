package me.marvin.proxy.commands.impl;

import me.marvin.proxy.commands.Command;

public class ExecutableCommandEntry extends CommandEntry {
    private CommandEntry parent;
    private Command command;

    public ExecutableCommandEntry(CommandEntry parent, Command command) {
        super();
        this.parent = parent;
        this.command = command;
    }

    public ExecutableCommandEntry(CommandEntry parent) {
        this(parent, null);
    }

    public CommandEntry parent() {
        return parent;
    }

    public ExecutableCommandEntry parent(CommandEntry parent) {
        this.parent = parent;
        return this;
    }

    public Command command() {
        return command;
    }

    public ExecutableCommandEntry command(Command command) {
        this.command = command;
        return this;
    }
}
