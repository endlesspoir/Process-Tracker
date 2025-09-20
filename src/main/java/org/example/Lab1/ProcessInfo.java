package org.example.Lab1;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessInfo {

    private long pid;
    private String user;
    private String name;
    private long memory;
    private  int priority;
    private int threads;
}
