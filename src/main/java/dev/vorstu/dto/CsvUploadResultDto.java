package dev.vorstu.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CsvUploadResultDto {
    private List<RegistrationRequestDto> created;
    private List<CsvRowError> errors;

    @Data
    @Builder
    public static class CsvRowError {
        private int row;
        private String reason;
    }
}
