package me.marvin.proxy.commands.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A simple command.
 */
public abstract class CommandEntry {
    protected final Map<String, ExecutableCommandEntry> children;

    protected CommandEntry() {
        children = new HashMap<>();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CommandEntry.class.getSimpleName() + "[", "]")
            .add("children=" + children)
            .toString();
    }
}
