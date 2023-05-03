package com.javarush.jira.bugtracking;

import com.fasterxml.jackson.annotation.JsonView;
import com.javarush.jira.bugtracking.service.TaskService;
import com.javarush.jira.bugtracking.to.TaskTo;
import com.javarush.jira.common.util.validation.View;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping(TaskRestController.TASK_URL)
public class TaskRestController {
    public static final String TASK_URL = "/api/task";
    private final TaskService taskService;

    @PostMapping
    public TaskTo create(@Validated(View.OnCreate.class) TaskTo taskTo) {
        return taskService.create(taskTo);
    }

    @GetMapping
    public List<TaskTo> getTasksAllFree() {
        return taskService.getAllTasksFree();
    }

    @GetMapping("{id}")
    public TaskTo getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @JsonView(View.OnUpdate.class)
    public void update(@RequestBody TaskTo task) {
        taskService.update(task);
    }

    @PutMapping(value = "/{id}/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @JsonView(View.OnUpdate.class)
    public void updateTags(@PathVariable Long id, @RequestBody TaskTo task) {
        taskService.addTags(id, task);
    }

    @PostMapping("/form")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Hidden
    public void createOrUpdate(@Validated(View.OnUpdate.class) TaskTo taskTo) {
        if (taskTo.isNew()) {
            create(taskTo);
        } else {
            update(taskTo);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable long id, @RequestParam boolean enabled) {
        taskService.enable(id, enabled);
    }
}
