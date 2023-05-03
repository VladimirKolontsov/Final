package com.javarush.jira.bugtracking;

import com.javarush.jira.bugtracking.service.TaskService;
import com.javarush.jira.bugtracking.to.TaskTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.javarush.jira.login.internal.web.UserTestData.ADMIN_MAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    private final Long TEST_TASK_ID = 2L;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return mockMvc.perform(builder);
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void updateTaskTags() throws Exception {
        perform(MockMvcRequestBuilders.put(TaskRestController.TASK_URL + "/" + TEST_TASK_ID + "/tags")
                .content("""
                        {
                            "id":2,
                            "tags": ["ThreeTags", "FourTag", "FiveTags"]
                        }
                        """).contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
        TaskTo taskById = taskService.getTaskById(TEST_TASK_ID);
        assertEquals(5, taskById.getTags().size());
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void updateTaskTagsWithDuplicate() throws Exception {
        TaskTo taskBeforeAdd = taskService.getTaskById(2L);

        perform(MockMvcRequestBuilders.put(TaskRestController.TASK_URL + "/" + TEST_TASK_ID + "/tags")
                .content("""
                                {
                                    "id":2,
                                    "tags": ["OneTag", "TwoTag"]
                                }
                                """).contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
        TaskTo taskAfterAdd = taskService.getTaskById(TEST_TASK_ID);
        assertEquals(taskBeforeAdd.getTags().size(), taskAfterAdd.getTags().size());
    }

}
