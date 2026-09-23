package com.mastercomputeracademy.dto.request;

import com.mastercomputeracademy.entity.Student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Request to change a student's status")
public class UpdateStudentStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(allowableValues = {"ACTIVE", "INACTIVE", "COMPLETED", "DROPPED"})
    private StudentStatus status;
}
