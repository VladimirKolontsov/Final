package com.javarush.jira.bugtracking.internal.repository;

import com.javarush.jira.bugtracking.internal.model.Task;
import com.javarush.jira.common.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;

@Transactional(readOnly = true)
public interface TaskRepository extends BaseRepository<Task> {
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.sprint LEFT JOIN FETCH t.activities WHERE t.sprint IS NOT NULL")
    List<Task> findAllSprintIdIsNotNull();

    @Query("select t from Task t where t.sprint.id is null")
    List<Task> findBySprintIdByNull();

    default Task saveOrUpdate(Task entity) {
        Task fromDB = getExisted(entity.getId());
        if (isNull(entity.getSprint())) {
            entity.setSprint(fromDB.getSprint());
        }
        if (isNull(entity.getProject())) {
            entity.setProject(fromDB.getProject());
        }
        return save(entity);
    }
}
