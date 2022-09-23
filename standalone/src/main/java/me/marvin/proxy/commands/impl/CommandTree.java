package me.marvin.proxy.commands.impl;

import me.marvin.proxy.commands.Command;
import me.marvin.proxy.utils.Tristate;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

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

    public void unregister(String... literals) {
        for (String literal : literals) {
            AtomicBoolean first = new AtomicBoolean(true);
            traverseTree(literal, (key, entry) -> {
                if (entry instanceof ExecutableCommandEntry parent) {
                    ExecutableCommandEntry realEntry = parent.children.get(key);

                    if (realEntry != null) {
                        if (first.get()) {
                            realEntry.command(null);
                            first.set(false);
                        }

                        if (realEntry.children.isEmpty()) {
                            parent.children.remove(key);
                        }
                    }
                }
            });
        }
    }

    public Tristate execute(String literal) {
        String[] parts = literal.split(" ");
        CommandEntry current = this;
        int start = -1;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            ExecutableCommandEntry next = current.children.get(part);

            if (next == null) {
                start = i;
                break;
            } else {
                current = next;
            }
        }

        if (current instanceof ExecutableCommandEntry executable && executable.command() != null) {
            parts = Arrays.copyOfRange(parts, start == -1 ? parts.length : start, parts.length);
            return Tristate.fromBoolean(executable.command().execute(parts));
        }

        return Tristate.NOT_SET;
    }

    private void traverseTree(String literal, BiConsumer<String, CommandEntry> consumer) {
        String[] parts = literal.split(" ");
        CommandEntry current = this;
        int start = -1;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            ExecutableCommandEntry next = current.children.get(part);

            if (next == null) {
                start = i - 1;
                break;
            } else if (i == parts.length - 1) {
                start = i;
                break;
            } else {
                current = next;
            }
        }

        if (current instanceof ExecutableCommandEntry executable) {
            CommandEntry parent = current;
            for (int i = start; i >= 0; i--) {
                String part = parts[i];
                consumer.accept(part, parent);
                parent = executable.parent();
            }
        }
    }
}
