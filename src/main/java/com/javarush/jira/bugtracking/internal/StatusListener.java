package com.javarush.jira.bugtracking.internal;

import com.javarush.jira.bugtracking.internal.model.Task;
import com.javarush.jira.bugtracking.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StatusListener {

    private final ActivityService activityService;

    @EventListener
    public void statusTaskChange(StatusChangeEvent event) {
        Task task = event.task();
        String username = event.username();
        activityService.create(task, username);

    }
}
