package com.collab.userservice.dto;

import com.collab.userservice.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile data transfer object")
public class UserDTO {

    @Schema(description = "User ID")
    private String id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static UserDTO fromUser(User user) {
        return new UserDTO(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatar(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
