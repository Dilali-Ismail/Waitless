package com.waitless.user.dto;

import com.waitless.user.enums.UserStatus;

import java.time.LocalDateTime;

public class UserDTO {

    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private Double score;
    private UserStatus status;
    private LocalDateTime suspensionEndDate;
    private Integer ticketsCreated;
    private Integer ticketsServed;
    private Integer ticketsCancelled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserDTO() {
    }

    public UserDTO(String userId, String name, String email, String phoneNumber, Double score, UserStatus status, LocalDateTime suspensionEndDate, Integer ticketsCreated, Integer ticketsServed, Integer ticketsCancelled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.score = score;
        this.status = status;
        this.suspensionEndDate = suspensionEndDate;
        this.ticketsCreated = ticketsCreated;
        this.ticketsServed = ticketsServed;
        this.ticketsCancelled = ticketsCancelled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getSuspensionEndDate() {
        return suspensionEndDate;
    }

    public void setSuspensionEndDate(LocalDateTime suspensionEndDate) {
        this.suspensionEndDate = suspensionEndDate;
    }

    public Integer getTicketsCreated() {
        return ticketsCreated;
    }

    public void setTicketsCreated(Integer ticketsCreated) {
        this.ticketsCreated = ticketsCreated;
    }

    public Integer getTicketsServed() {
        return ticketsServed;
    }

    public void setTicketsServed(Integer ticketsServed) {
        this.ticketsServed = ticketsServed;
    }

    public Integer getTicketsCancelled() {
        return ticketsCancelled;
    }

    public void setTicketsCancelled(Integer ticketsCancelled) {
        this.ticketsCancelled = ticketsCancelled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static builder builder() {
        return new builder();
    }

    public static class builder {
        private String userId;
        private String name;
        private String email;
        private String phoneNumber;
        private Double score;
        private UserStatus status;
        private LocalDateTime suspensionEndDate;
        private Integer ticketsCreated;
        private Integer ticketsServed;
        private Integer ticketsCancelled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public builder name(String name) {
            this.name = name;
            return this;
        }

        public builder email(String email) {
            this.email = email;
            return this;
        }

        public builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public builder score(Double score) {
            this.score = score;
            return this;
        }

        public builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public builder suspensionEndDate(LocalDateTime suspensionEndDate) {
            this.suspensionEndDate = suspensionEndDate;
            return this;
        }

        public builder ticketsCreated(Integer ticketsCreated) {
            this.ticketsCreated = ticketsCreated;
            return this;
        }

        public builder ticketsServed(Integer ticketsServed) {
            this.ticketsServed = ticketsServed;
            return this;
        }

        public builder ticketsCancelled(Integer ticketsCancelled) {
            this.ticketsCancelled = ticketsCancelled;
            return this;
        }

        public builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserDTO build() {
            return new UserDTO(userId, name, email, phoneNumber, score, status, suspensionEndDate, ticketsCreated, ticketsServed, ticketsCancelled, createdAt, updatedAt);
        }
    }
}
