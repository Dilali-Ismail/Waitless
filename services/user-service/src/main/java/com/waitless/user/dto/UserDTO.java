package com.waitless.user.dto;


import com.waitless.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String userId;
    private String name;
    private String email;
    private Double score;
    private UserStatus status;
    private LocalDateTime suspensionEndDate;
    private Integer ticketsCreated;
    private Integer ticketsServed;
    private Integer ticketsCancelled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
