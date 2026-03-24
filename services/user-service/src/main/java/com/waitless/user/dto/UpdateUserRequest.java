package com.waitless.user.dto;

import jakarta.validation.constraints.Email;

public class UpdateUserRequest {

    private String name;

    @Email(message = "Email must be valid")
    private String email;
    private String phoneNumber;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
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

    public static builder builder() {
        return new builder();
    }

    public static class builder {
        private String name;
        private String email;
        private String phoneNumber;

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

        public UpdateUserRequest build() {
            return new UpdateUserRequest(name, email, phoneNumber);
        }
    }
}
