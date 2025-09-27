package org.example.Lab1;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
public class ProcessInfo {
    private static final List<String> RENAME = Arrays.asList("polkitd", "systemd-timesync","dbus");

    private long pid;
    private String user;
    private String name;
    private float memory;
    private int priority;
    private int threads;


    public static ProcessInfo from(long pid, String user, String name, float memory, int priority, int threads) {
        if (RENAME.contains(user)) {
            user = "root";
        }
        return new ProcessInfo(pid, user, name, memory, priority, threads);
    }
}
