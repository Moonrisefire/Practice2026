package dev.vorstu.services;

import dev.vorstu.dto.CsvUploadResultDto;
import dev.vorstu.dto.RegistrationRequestDto;
import dev.vorstu.exceptions.BadRequestException;
import dev.vorstu.exceptions.ResourceNotFoundException;
import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.models.Role;
import dev.vorstu.repositories.RegistrationRequestRepository;
import dev.vorstu.repositories.StudentRepository;
import dev.vorstu.repositories.TeacherRepository;
import dev.vorstu.repositories.UserRepository;
import dev.vorstu.services.email.RegistrationEmailDispatcher;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
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
    private RegistrationEmailDispatcher registrationEmailDispatcher;
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
            var req = inv.getArgument(0, RegistrationRequest.class);
            req.setId(1L);
            return req;
        });
        doNothing().when(registrationEmailDispatcher).dispatch(anyLong(), anyString(), anyString());

        CsvUploadResultDto result = registrationService.processCsvUpload(file);

        assertEquals(1, result.getCreated().size());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getReason().contains("ФИО"));
    }

    @Test
    void resendEmail_success() {
        RegistrationRequest request = new RegistrationRequest();
        request.setId(1L);
        request.setEmail("user@test.com");
        request.setToken("token-123");
        request.setStatus(RegistrationStatus.PENDING);
        request.setEmailStatus(EmailDeliveryStatus.FAILED);
        request.setRole(Role.STUDENT);
        request.setExpiresAt(LocalDateTime.now().plusHours(24));

        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        doNothing().when(registrationEmailDispatcher).dispatch(1L, "user@test.com", "token-123");

        RegistrationRequestDto result = registrationService.resendEmail(1L);

        assertEquals(1L, result.getId());
        verify(registrationEmailDispatcher).dispatch(1L, "user@test.com", "token-123");
    }

    @Test
    void resendEmail_alreadySent() {
        RegistrationRequest request = new RegistrationRequest();
        request.setId(1L);
        request.setStatus(RegistrationStatus.PENDING);
        request.setEmailStatus(EmailDeliveryStatus.SENT);

        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(BadRequestException.class, () -> registrationService.resendEmail(1L));
    }

    @Test
    void resendEmail_notFound() {
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> registrationService.resendEmail(1L));
    }

    @Test
    void getRegistrationRequests_byStatus() {
        RegistrationRequest request = new RegistrationRequest();
        request.setId(1L);
        request.setStatus(RegistrationStatus.PENDING);
        request.setEmailStatus(EmailDeliveryStatus.PENDING);

        when(registrationRequestRepository.findByStatus(RegistrationStatus.PENDING))
                .thenReturn(List.of(request));

        List<RegistrationRequestDto> result = registrationService.getRegistrationRequests(RegistrationStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(RegistrationStatus.PENDING, result.get(0).getStatus());
    }
}
