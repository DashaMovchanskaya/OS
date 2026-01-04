package org.example;import org.example.model.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testSettersAndGetters() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Title");
        task.setDescription("Test Description");
        task.setStatus("todo");

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        assertEquals(1L, task.getId());
        assertEquals("Test Title", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals("todo", task.getStatus());
        assertEquals(now, task.getCreatedAt());
        assertEquals(now, task.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Task t1 = new Task("Title", "Desc", "todo");
        t1.setId(1L);

        Task t2 = new Task("Title", "Desc", "todo");
        t2.setId(1L);

        Task t3 = new Task("Other", "Other", "done");
        t3.setId(2L);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1, t3);
    }

    @Test
    void testToStringContainsFields() {
        Task task = new Task("Title", "Desc", "todo");
        task.setId(1L);

        String str = task.toString();
        assertTrue(str.contains("Title"));
        assertTrue(str.contains("todo"));
        assertTrue(str.contains("1"));
    }

    @Test
    void testDefaultConstructorAndStatus() {
        Task task = new Task();
        assertEquals("todo", task.getStatus()); // статус по умолчанию
    }

    @Test
    void testParameterizedConstructor() {
        Task task = new Task("Title", "Desc", "done");
        assertEquals("Title", task.getTitle());
        assertEquals("Desc", task.getDescription());
        assertEquals("done", task.getStatus());
    }
}
