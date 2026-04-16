package com.carebridge.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private Instant startAt;
    private boolean showOnBoard;
    private Long createdById;
    private Long eventTypeId;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private boolean seenByCurrentUser;
    private List<Long> seenByUserIds;

    public EventDTO() {
    }

    public EventDTO(Long id, String title, String description, Instant startAt, boolean showOnBoard, Long createdById, Long eventTypeId, LocalDate eventDate, LocalTime eventTime, boolean seenByCurrentUser, List<Long> seenByUserIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.showOnBoard = showOnBoard;
        this.createdById = createdById;
        this.eventTypeId = eventTypeId;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.seenByCurrentUser = seenByCurrentUser;
        this.seenByUserIds = seenByUserIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public boolean isShowOnBoard() {
        return showOnBoard;
    }

    public void setShowOnBoard(boolean showOnBoard) {
        this.showOnBoard = showOnBoard;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalTime eventTime) {
        this.eventTime = eventTime;
    }

    public boolean isSeenByCurrentUser() {
        return seenByCurrentUser;
    }

    public void setSeenByCurrentUser(boolean seenByCurrentUser) {
        this.seenByCurrentUser = seenByCurrentUser;
    }

    public List<Long> getSeenByUserIds() {
        return seenByUserIds;
    }

    public void setSeenByUserIds(List<Long> seenByUserIds) {
        this.seenByUserIds = seenByUserIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDTO eventDTO = (EventDTO) o;
        return showOnBoard == eventDTO.showOnBoard && seenByCurrentUser == eventDTO.seenByCurrentUser && Objects.equals(id, eventDTO.id) && Objects.equals(title, eventDTO.title) && Objects.equals(description, eventDTO.description) && Objects.equals(startAt, eventDTO.startAt) && Objects.equals(createdById, eventDTO.createdById) && Objects.equals(eventTypeId, eventDTO.eventTypeId) && Objects.equals(eventDate, eventDTO.eventDate) && Objects.equals(eventTime, eventDTO.eventTime) && Objects.equals(seenByUserIds, eventDTO.seenByUserIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, startAt, showOnBoard, createdById, eventTypeId, eventDate, eventTime, seenByCurrentUser, seenByUserIds);
    }

    @Override
    public String toString() {
        return "EventDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startAt=" + startAt +
                ", showOnBoard=" + showOnBoard +
                ", createdById=" + createdById +
                ", eventTypeId=" + eventTypeId +
                ", eventDate=" + eventDate +
                ", eventTime=" + eventTime +
                ", seenByCurrentUser=" + seenByCurrentUser +
                ", seenByUserIds=" + seenByUserIds +
                '}';
    }
}
