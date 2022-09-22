package me.marvin.proxy.commands.impl;

import me.marvin.proxy.commands.Command;
import me.marvin.proxy.utils.Tristate;

import java.util.Arrays;

public class CommandTree extends CommandEntry {
    public void register(Command command, String... literals) {
        for (String literal : literals) {
            String[] parts = literal.split(" ");
            CommandEntry parent = this;

            int lastIndex = parts.length - 1;
            for (int i = 0; i < parts.length; i++) {
                final CommandEntry currentParent = parent;
                String part = parts[i];
                ExecutableCommandEntry newParent = parent.children.computeIfAbsent(part, __ -> new ExecutableCommandEntry(currentParent));
                parent = newParent;

                if (lastIndex == i) {
                    newParent.command(command);
                }
            }
        }
    }

    public Tristate execute(String literal) {
        String[] parts = literal.split(" ");
        ExecutableCommandEntry current = null;
        int start = parts.length;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            ExecutableCommandEntry next = children.get(part);

            if (next == null) {
                start = i;
                break;
            } else {
                current = next;
            }
        }

        if (current != null) {
            parts = Arrays.copyOfRange(parts, start, parts.length);
            return Tristate.fromBoolean(current.command().execute(parts));
        }

        return Tristate.NOT_SET;
    }
}
