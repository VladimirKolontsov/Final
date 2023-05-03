package com.javarush.jira.bugtracking.to;

import com.javarush.jira.common.util.validation.Code;
import com.javarush.jira.common.util.validation.NoHtml;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
public class TaskTo extends NodeTo<TaskTo> {
    @Code
    String typeCode;

    @Code
    String statusCode;

    @NotBlank
    @NoHtml
    String description;

    @Nullable
    SprintTo sprint;

    @Nullable
    ProjectTo project;

    @Nullable
    LocalDateTime updated;

    @Code
    String priorityCode;

    @Positive
    @Nullable
    int estimate;

    @Nullable
    int storyPoints;

    Set<String> tags;

    List<ActivityTo> activities;

    public TaskTo(Long id, String title, boolean enabled, String typeCode, String statusCode, String description, SprintTo sprint,
                  ProjectTo project, LocalDateTime updated,
                  String priorityCode, int estimate, int storyPoints, Set<String> tags, List<ActivityTo> activities, TaskTo parent) {
        super(id, title, enabled, parent);
        this.typeCode = typeCode;
        this.statusCode = statusCode;
        this.description = description;
        this.sprint = sprint;
        this.project = project;
        this.updated = updated;
        this.priorityCode = priorityCode;
        this.estimate = estimate;
        this.storyPoints = storyPoints;
        this.tags = tags;
        this.activities = activities;
    }
}
