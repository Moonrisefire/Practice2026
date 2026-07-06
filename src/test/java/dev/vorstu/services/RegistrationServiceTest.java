package dev.vorstu.services;

import dev.vorstu.dto.CsvUploadResultDto;
import dev.vorstu.exceptions.BadRequestException;
import dev.vorstu.repositories.RegistrationRequestRepository;
import dev.vorstu.repositories.StudentRepository;
import dev.vorstu.repositories.TeacherRepository;
import dev.vorstu.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRequestRepository registrationRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(registrationService, "tokenTtlHours", 24);
    }

    @Test
    void processCsvUpload_emptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);

        assertThrows(BadRequestException.class, () -> registrationService.processCsvUpload(file));
    }

    @Test
    void processCsvUpload_invalidHeader() {
        String content = "name,role,mail,group\nПетров,студент,a@b.com,VM";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));

        assertThrows(BadRequestException.class, () -> registrationService.processCsvUpload(file));
    }

    @Test
    void processCsvUpload_successWithPartialErrors() {
        String content = """
                ФИО,роль,email,группа
                Петров Петр,студент,petrov@test.com,VM
                ,студент,bad@test.com,VM
                """;
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));

        when(userRepository.findByUsername(anyString())).thenReturn(java.util.Optional.empty());
        when(registrationRequestRepository.existsByEmailAndStatus(anyString(), any())).thenReturn(false);
        when(registrationRequestRepository.save(any())).thenAnswer(inv -> {
            var req = inv.getArgument(0, dev.vorstu.models.RegistrationRequest.class);
            req.setId(1L);
            return req;
        });
        doNothing().when(emailService).sendRegistrationLink(anyString(), anyString());

        CsvUploadResultDto result = registrationService.processCsvUpload(file);

        assertEquals(1, result.getCreated().size());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getReason().contains("ФИО"));
    }
}
