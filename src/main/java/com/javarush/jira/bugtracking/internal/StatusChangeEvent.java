package com.javarush.jira.bugtracking.internal;

import com.javarush.jira.bugtracking.internal.model.Task;
import com.javarush.jira.common.AppEvent;

public record StatusChangeEvent(String username, Task task) implements AppEvent {
}
