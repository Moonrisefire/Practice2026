package dev.vorstu.services;

import dev.vorstu.dto.ConfirmRegistrationResponse;
import dev.vorstu.dto.CsvUploadResultDto;
import dev.vorstu.dto.RegistrationRequestDto;
import dev.vorstu.exceptions.BadRequestException;
import dev.vorstu.exceptions.ConflictException;
import dev.vorstu.exceptions.GoneException;
import dev.vorstu.exceptions.ResourceNotFoundException;
import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.models.Role;
import dev.vorstu.models.Student;
import dev.vorstu.models.Teacher;
import dev.vorstu.repositories.RegistrationRequestRepository;
import dev.vorstu.repositories.StudentRepository;
import dev.vorstu.repositories.TeacherRepository;
import dev.vorstu.repositories.UserRepository;
import dev.vorstu.services.email.RegistrationEmailDispatcher;
import dev.vorstu.validation.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final RegistrationEmailDispatcher registrationEmailDispatcher;
    private final PasswordEncoder passwordEncoder;

    @Value("${registration.token.ttl-hours:24}")
    private int tokenTtlHours;

    @Transactional
    public CsvUploadResultDto processCsvUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV файл пуст или не передан");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BadRequestException("Файл должен иметь расширение .csv");
        }

        List<RegistrationRequestDto> created = new ArrayList<>();
        List<CsvUploadResultDto.CsvRowError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new BadRequestException("CSV файл не содержит данных");
            }

            if (!isValidHeader(headerLine)) {
                throw new BadRequestException("Неверный формат заголовка CSV. Ожидается: ФИО,роль,email,группа");
            }

            String line;
            int rowNumber = 1;
            Set<String> emailsInFile = new HashSet<>();

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 4) {
                    errors.add(CsvUploadResultDto.CsvRowError.builder()
                            .row(rowNumber)
                            .reason("Недостаточно колонок в строке")
                            .build());
                    continue;
                }

                String fio = parts[0].trim();
                String roleStr = parts[1].trim().toLowerCase(Locale.ROOT);
                String email = parts[2].trim();
                String groupName = parts[3].trim();

                if (fio.isBlank()) {
                    errors.add(error(rowNumber, "ФИО не может быть пустым"));
                    continue;
                }
                if (email.isBlank()) {
                    errors.add(error(rowNumber, "Email не может быть пустым"));
                    continue;
                }
                if (!new EmailValidator().isValid(email, null)) {
                    errors.add(error(rowNumber, "Некорректный формат email"));
                    continue;
                }
                if (groupName.isBlank()) {
                    errors.add(error(rowNumber, "Название группы не может быть пустым"));
                    continue;
                }

                Role role = parseRole(roleStr);
                if (role == null) {
                    errors.add(error(rowNumber, "Неверная роль. Допустимо: студент, преподаватель"));
                    continue;
                }

                if (!emailsInFile.add(email.toLowerCase(Locale.ROOT))) {
                    errors.add(error(rowNumber, "Дубликат email в файле"));
                    continue;
                }

                if (userRepository.findByUsername(email).isPresent()) {
                    errors.add(error(rowNumber, "Пользователь с таким email уже зарегистрирован"));
                    continue;
                }

                if (role == Role.TEACHER && teacherRepository.existsByEmail(email)) {
                    errors.add(error(rowNumber, "Преподаватель с таким email уже существует"));
                    continue;
                }

                if (registrationRequestRepository.existsByEmailAndStatus(email, RegistrationStatus.PENDING)) {
                    errors.add(error(rowNumber, "Заявка с таким email уже ожидает подтверждения"));
                    continue;
                }

                try {
                    RegistrationRequest request = createPendingRequest(fio, email, groupName, role);
                    registrationEmailDispatcher.dispatch(request.getId(), email, request.getToken());
                    created.add(toDto(request));
                } catch (Exception e) {
                    errors.add(error(rowNumber, e.getMessage() != null ? e.getMessage() : "Ошибка создания заявки"));
                }
            }

            if (created.isEmpty() && errors.isEmpty()) {
                throw new BadRequestException("CSV файл не содержит строк с данными");
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Ошибка чтения CSV файла: " + e.getMessage());
        }

        return CsvUploadResultDto.builder()
                .created(created)
                .errors(errors)
                .build();
    }

    @Transactional
    public ConfirmRegistrationResponse confirmRegistration(String token) {
        RegistrationRequest request = registrationRequestRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена"));

        if (request.getStatus() == RegistrationStatus.CONFIRMED) {
            throw new ConflictException("Регистрация уже подтверждена");
        }

        if (request.getStatus() == RegistrationStatus.REVOKED
                || request.getStatus() == RegistrationStatus.EXPIRED
                || request.getExpiresAt().isBefore(LocalDateTime.now())) {
            request.setStatus(RegistrationStatus.EXPIRED);
            registrationRequestRepository.save(request);
            throw new GoneException("Срок действия ссылки истёк, заявка отозвана");
        }

        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
        String username = request.getEmail();

        if (request.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setUsername(username);
            student.setPassword(passwordEncoder.encode(temporaryPassword));
            student.setRole(Role.STUDENT);
            student.setFio(request.getFio());
            student.setGroupName(request.getGroupName());
            studentRepository.save(student);
        } else if (request.getRole() == Role.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setUsername(username);
            teacher.setPassword(passwordEncoder.encode(temporaryPassword));
            teacher.setRole(Role.TEACHER);
            teacher.setFio(request.getFio());
            teacher.setEmail(request.getEmail());
            teacher.setAssignedGroups(new HashSet<>(Set.of(request.getGroupName())));
            teacherRepository.save(teacher);
        }

        request.setStatus(RegistrationStatus.CONFIRMED);
        registrationRequestRepository.save(request);

        return ConfirmRegistrationResponse.builder()
                .message("Регистрация успешно подтверждена")
                .username(username)
                .temporaryPassword(temporaryPassword)
                .build();
    }

    @Transactional
    public int revokeExpiredRequests() {
        List<RegistrationRequest> expired = registrationRequestRepository
                .findByStatusAndExpiresAtBefore(RegistrationStatus.PENDING, LocalDateTime.now());

        expired.forEach(request -> request.setStatus(RegistrationStatus.REVOKED));
        registrationRequestRepository.saveAll(expired);
        return expired.size();
    }

    @Transactional(readOnly = true)
    public List<RegistrationRequestDto> getRegistrationRequests(RegistrationStatus status) {
        List<RegistrationRequest> requests = status != null
                ? registrationRequestRepository.findByStatus(status)
                : registrationRequestRepository.findAll();
        return requests.stream().map(this::toDto).toList();
    }

    @Transactional
    public RegistrationRequestDto resendEmail(Long requestId) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена"));

        if (request.getStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("Повторная отправка доступна только для ожидающих заявок");
        }

        if (request.getEmailStatus() == EmailDeliveryStatus.SENT) {
            throw new BadRequestException("Письмо уже успешно отправлено");
        }

        registrationEmailDispatcher.dispatch(request.getId(), request.getEmail(), request.getToken());

        return registrationRequestRepository.findById(requestId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена"));
    }

    private RegistrationRequest createPendingRequest(String fio, String email, String groupName, Role role) {
        RegistrationRequest request = new RegistrationRequest();
        request.setFio(fio);
        request.setEmail(email);
        request.setGroupName(groupName);
        request.setRole(role);
        request.setToken(UUID.randomUUID().toString());
        request.setExpiresAt(LocalDateTime.now().plusHours(tokenTtlHours));
        request.setStatus(RegistrationStatus.PENDING);
        return registrationRequestRepository.save(request);
    }

    private boolean isValidHeader(String headerLine) {
        String normalized = headerLine.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("фио") && normalized.contains("роль")
                && normalized.contains("email") && normalized.contains("группа");
    }

    private Role parseRole(String roleStr) {
        if (roleStr.equals("студент") || roleStr.equals("student")) {
            return Role.STUDENT;
        }
        if (roleStr.equals("преподаватель") || roleStr.equals("teacher")) {
            return Role.TEACHER;
        }
        return null;
    }

    private CsvUploadResultDto.CsvRowError error(int row, String reason) {
        return CsvUploadResultDto.CsvRowError.builder().row(row).reason(reason).build();
    }

    private RegistrationRequestDto toDto(RegistrationRequest request) {
        return RegistrationRequestDto.builder()
                .id(request.getId())
                .fio(request.getFio())
                .email(request.getEmail())
                .groupName(request.getGroupName())
                .role(request.getRole())
                .status(request.getStatus())
                .expiresAt(request.getExpiresAt())
                .emailStatus(request.getEmailStatus())
                .emailAttempts(request.getEmailAttempts())
                .lastEmailError(request.getLastEmailError())
                .build();
    }
}
