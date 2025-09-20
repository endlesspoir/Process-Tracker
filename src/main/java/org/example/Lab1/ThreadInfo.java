package org.example.Lab1;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThreadInfo {
    private long tid;
    private long priority;
}
