package com.waitless.user.service;


import com.waitless.user.entity.User;
import com.waitless.user.entity.UserRestriction;
import com.waitless.user.enums.RestrictionType;
import com.waitless.user.enums.UserStatus;
import com.waitless.user.exception.UserNotFoundException;
import com.waitless.user.repository.UserRepository;
import com.waitless.user.repository.UserRestrictionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestrictionService {

    private final UserRepository userRepository;
    private final UserRestrictionRepository userRestrictionRepository;

    private static final int WARNING_THRESHOLD = 3;
    private static final int NO_SHOW_THRESHOLD = 2;
    private static final int SUSPENSION_THRESHOLD = 3;
    private static final int CANCELLATION_WARNING_MINUTES = 30;
    private static final int SUSPENSION_DURATION_HOURS = 24;


    public void applyWarning(String userId , Long ticketId , String reason){

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setWarningCount(user.getWarningCount() + 1);

        creationRestrictionAudit(userId,RestrictionType.WARNING,reason,ticketId);

        if(user.getWarningCount() >= WARNING_THRESHOLD){
            applySuspension(userId,"3 warnings accumulated");
        }else{
            userRepository.save(user);
        }


    }
    public void applyNoShow(String userId, long ticketId , String reason){

        User user =  userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setNoShowCount(user.getNoShowCount() + 1);

        creationRestrictionAudit(userId,RestrictionType.NO_SHOW,reason,ticketId);
        if(user.getNoShowCount() >= NO_SHOW_THRESHOLD){
            applySuspension(userId,"2 no-shows accumulated");
        }
        else{
            userRepository.save(user);
        }
    }
    public void applySuspension(String userId , String reason){

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setStatus(UserStatus.SUSPENDED);
        user.setSuspensionEndDate(LocalDateTime.now().plusHours(SUSPENSION_DURATION_HOURS));
        user.setSuspensionCount(user.getSuspensionCount() + 1);

        user.setWarningCount(0);
        user.setWarningCount(0);

        String detailedReason = String.format(
                "Suspension for %dh. Reason: %s. Total suspensions: %d/%d. Ends at: %s",
                SUSPENSION_DURATION_HOURS,
                reason,
                user.getSuspensionCount(),
                SUSPENSION_THRESHOLD,
                user.getSuspensionEndDate()
        );

        creationRestrictionAudit(userId,RestrictionType.SUSPENDED,detailedReason,null);

        if(user.getWarningCount() >= SUSPENSION_THRESHOLD){
            applyBan(userId,"3 suspensions accumulated");
        } else{
            userRepository.save(user);
        }


    }
    public void applyBan(String userId , String reason){

        User user =  userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setStatus(UserStatus.BANNED);
        user.setSuspensionEndDate(null);

        String detailedReason = String.format(
                "Permanent ban. Reason: %s. Total suspensions: %d. Manual admin intervention required.",
                reason,
                user.getSuspensionCount()
        );

        creationRestrictionAudit(userId,RestrictionType.BANNED,detailedReason,null);

        userRepository.save(user);

    }

    public boolean liftSuspension(String userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        if(!user.getStatus().equals(UserStatus.SUSPENDED)){
            return false;
        }

        if(user.getSuspensionEndDate() == null){
            return false;
        }

        if(LocalDateTime.now().isAfter(user.getSuspensionEndDate())){
            user.setStatus(UserStatus.ACTIVE);
            user.setSuspensionEndDate(null);
            String reason = String.format(
                    "Suspension automatically lifted after %dh",
                    SUSPENSION_DURATION_HOURS
            );

            creationRestrictionAudit(userId,RestrictionType.SUSPENSION_LIFTED,reason,null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    public void incrementTicketsCancelled(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setTicketsCancelled(user.getTicketsCancelled() + 1);
        userRepository.save(user);
    }

    private void creationRestrictionAudit(
            String userId ,
            RestrictionType restrictionType,
            String reason,
            Long ticketId
    ){
        UserRestriction restriction = UserRestriction.builder()
                .userId(userId)
                .restrictionType(restrictionType)
                .reason(reason)
                .ticketId(ticketId)
                .build();

        userRestrictionRepository.save(restriction);
    }
}
