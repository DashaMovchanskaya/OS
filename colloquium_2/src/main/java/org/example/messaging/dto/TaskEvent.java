package org.example.messaging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class TaskEvent {
    private Long taskId;
    private String title;
    private String action;
    private String oldStatus;
    private String newStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String performedBy;

    public TaskEvent() {
    }

    public TaskEvent(Long taskId, String title, String action, String oldStatus,
                     String newStatus, LocalDateTime timestamp, String performedBy) {
        this.taskId = taskId;
        this.title = title;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = timestamp;
        this.performedBy = performedBy;
    }

    public static TaskEvent created(Long taskId, String title, String status, String performedBy) {
        return new TaskEvent(
                taskId,
                title,
                "CREATED",
                null,
                status,
                LocalDateTime.now(),
                performedBy
        );
    }

    public static TaskEvent updated(Long taskId, String title, String oldStatus,
                                    String newStatus, String performedBy) {
        return new TaskEvent(
                taskId,
                title,
                "UPDATED",
                oldStatus,
                newStatus,
                LocalDateTime.now(),
                performedBy
        );
    }

    public static TaskEvent deleted(Long taskId, String title, String status, String performedBy) {
        return new TaskEvent(
                taskId,
                title,
                "DELETED",
                status,
                null,
                LocalDateTime.now(),
                performedBy
        );
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    @Override
    public String toString() {
        return "TaskEvent{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", action='" + action + '\'' +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", timestamp=" + timestamp +
                ", performedBy='" + performedBy + '\'' +
                '}';
    }
}