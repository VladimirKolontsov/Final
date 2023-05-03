package com.javarush.jira.bugtracking.service;

import com.javarush.jira.bugtracking.internal.StatusChangeEvent;
import com.javarush.jira.bugtracking.internal.mapper.TaskMapper;
import com.javarush.jira.bugtracking.internal.model.Task;
import com.javarush.jira.bugtracking.internal.repository.TaskRepository;
import com.javarush.jira.bugtracking.to.TaskTo;
import com.javarush.jira.common.error.NotFoundException;
import com.javarush.jira.ref.RefTo;
import com.javarush.jira.ref.RefType;
import com.javarush.jira.ref.internal.ReferenceMapper;
import com.javarush.jira.ref.internal.ReferenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

@Service
@Transactional(readOnly = true)
public class TaskService extends BugtrackingService<Task, TaskTo, TaskRepository> {
    private final TaskRepository taskRepository;
    private final ReferenceRepository referenceRepository;
    private final ReferenceMapper referenceMapper;
    private final ApplicationEventPublisher eventPublisher;

    public TaskService(TaskRepository repository, TaskMapper mapper,
                       TaskRepository taskRepository, ReferenceRepository referenceRepository, ReferenceMapper referenceMapper, ApplicationEventPublisher eventPublisher) {
        super(repository, mapper);
        this.taskRepository = taskRepository;
        this.referenceRepository = referenceRepository;
        this.referenceMapper = referenceMapper;
        this.eventPublisher = eventPublisher;
    }

    public List<TaskTo> getAllSprints() {
        return mapper.toToList(repository.findAllSprintIdIsNotNull());
    }

    @Transactional
    public void update(TaskTo taskTo) {
        Task fromTo = mapper.toEntity(taskTo);
        Task fromDb = repository.getExisted(fromTo.getId());
        checkChangeStatusTask(fromDb, taskTo);
        if (isNull(fromTo.getSprint())) {
            fromTo.setSprint(fromDb.getSprint());
        }
        if (isNull(fromTo.getProject())) {
            fromTo.setProject(fromDb.getProject());
        }
        repository.save(fromTo);
    }

    @Transactional
    public void addTags(Long id, TaskTo taskTo) {
        Task taskById = taskRepository.getExisted(id);
        Set<String> tags = taskById.getTags();
        if (!taskTo.getTags().isEmpty()) {
            tags = Stream.concat(
                            taskById.getTags().stream(),
                            taskTo.getTags().stream())
                    .collect(Collectors.toSet());
        }
        taskById.setTags(tags);
    }

    public TaskTo getTaskById(Long id) {
        return mapper.toTo(repository.getExisted(id));
    }

    public List<TaskTo> getAllTasksFree() {
        return mapper.toToList(repository.findBySprintIdByNull());
    }

    @Transactional
    public void deleteTask(@NonNull Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public TaskTo create(TaskTo taskTo) {
        Task taskDb = repository.save(mapper.toEntity(taskTo));
        saveCreateStateTask(taskDb);
        return mapper.toTo(taskDb);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Transactional
    public void enable(long id, boolean enabled) {
        Stream.of(getTaskById(id))
                .peek(taskTo -> taskTo.enable(enabled))
                .map(mapper::toEntity)
                .map(repository::save)
                .findAny().orElseThrow(() -> new NotFoundException("Task not found"));
    }

    public Map<String, List<RefTo>> getReferences(List<Integer> refTypes) {
        return refTypes.stream()
                .map(integer -> referenceRepository.getByType(RefType.values()[integer]))
                .filter(references -> !references.isEmpty())
                .collect(Collectors
                        .toMap(r -> r.get(0).getRefType().name(),
                                referenceMapper::toToList));
    }

    private void checkChangeStatusTask(Task taskDb, TaskTo toTo) {
        String statusCodeDb = taskDb.getStatusCode();
        String statusCodeTo = toTo.getStatusCode();
        if (statusCodeDb != null && !statusCodeDb.equals(statusCodeTo)) {
            executeEvent(taskDb);
        }
    }

    private void saveCreateStateTask(Task taskDb) {
        if (taskDb != null) {
            executeEvent(taskDb);
        }
    }

    private void executeEvent(Task taskDb) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        eventPublisher.publishEvent(new StatusChangeEvent(username, taskDb));
    }
}
