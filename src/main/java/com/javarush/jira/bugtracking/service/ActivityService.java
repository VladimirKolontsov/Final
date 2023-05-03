package com.javarush.jira.bugtracking.service;

import com.javarush.jira.bugtracking.internal.model.Activity;
import com.javarush.jira.bugtracking.internal.model.Task;
import com.javarush.jira.bugtracking.internal.repository.ActivityRepository;
import com.javarush.jira.login.User;
import com.javarush.jira.login.internal.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(Task task, String username) {
        User user = userRepository.getExistedByEmail(username);
        Activity activity = new Activity();
        activity.setAuthor(user);
        activity.setDescription(task.getDescription());
        activity.setTask(task);
        activity.setStatusCode(task.getStatusCode());
        activity.setPriorityCode(task.getPriorityCode());
        activity.setTypeCode(task.getTypeCode());
        activity.setTitle(task.getTitle());
        activity.setEstimate(task.getEstimate());
        activityRepository.save(activity);
    }

}
