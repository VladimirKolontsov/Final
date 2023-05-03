package com.javarush.jira.bugtracking;

import com.javarush.jira.bugtracking.service.TaskService;
import com.javarush.jira.bugtracking.to.SprintTo;
import com.javarush.jira.bugtracking.to.TaskTo;
import com.javarush.jira.ref.RefTo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping
public class DashboardUIController {
    public static final String ROOT_URL = "/";
    public static final String BACKLOG_URL ="/backlog";
    private static final List<Integer> REF_TYPES_FOR_TASKS = List.of(2, 3, 7);
    private TaskService taskService;

    @GetMapping(DashboardUIController.ROOT_URL) // index page
    public String getAllSprint(Model model) {
        List<TaskTo> tasks = taskService.getAllSprints();
        Map<SprintTo, List<TaskTo>> taskMap = tasks.stream()
                .collect(Collectors.groupingBy(TaskTo::getSprint));
        model.addAttribute("taskMap", taskMap);
        model.addAttribute("refs", taskService.getReferences(REF_TYPES_FOR_TASKS));
        return "index";
    }

    @GetMapping(DashboardUIController.BACKLOG_URL)
    public String getBacklog(Model model) {
        Map<String, List<RefTo>> references = taskService.getReferences(REF_TYPES_FOR_TASKS);
        model.addAttribute("refs", references);
        return "backlog";
    }
}
