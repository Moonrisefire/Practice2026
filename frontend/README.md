# Practice2026 — Тестовый фронтенд

Минимальный одностраничный интерфейс для ручной проверки всех эндпоинтов бэкенда.

## Запуск

1. Запустите бэкенд (например, через Docker):

```bash
docker-compose up
```

Бэкенд будет доступен на `http://localhost:8080`.

2. Запустите статический сервер из папки `frontend/`:

```bash
cd frontend
npx serve .
```

или:

```bash
python -m http.server 3000
```

3. Откройте в браузере `http://localhost:3000` (или порт, который покажет `serve`).

> ES modules не работают при открытии `index.html` напрямую через `file://` — нужен HTTP-сервер.

## Настройка

- **API URL** — поле в шапке, по умолчанию `http://localhost:8080`. Сохраняется в `localStorage`.
- Токены (`accessToken`, `refreshToken`) хранятся в `localStorage`. При 401 выполняется автоматический refresh.

## Тестовые учётки

| Логин | Пароль | Роль |
|-------|--------|------|
| `admin` | `1234` | ADMIN |
| `teacher_ivanov` | `secure_pass_1` | TEACHER |
| `student_petrov` | `secure_pass_2` | STUDENT |

## Покрытие API

### Auth (все роли)

- `POST /api/auth/login` — форма входа
- `POST /api/auth/refresh` — автоматически при истечении access token
- `GET /api/auth/confirm?token=` — страница подтверждения регистрации (`?token=...` в URL или ручной ввод)

### ADMIN

- `GET/POST/PUT/DELETE /api/admin/students` — вкладка «Студенты»
- `GET/POST/PUT/DELETE /api/admin/teachers` — вкладка «Преподаватели»
- `POST /api/admin/teachers/{id}/groups` — кнопка «+ Группа»
- `GET/POST/PUT/DELETE /api/admin/admins` — вкладка «Админы»
- `GET /api/admin/admins/{id}` — кнопка «Просмотр»
- `POST /api/admin/registration-requests` — вкладка «CSV-регистрация»

### TEACHER

- `GET /api/teachers/me/students` — список студентов
- `PUT /api/teachers/me/students/{id}` — редактирование ФИО и группы

### STUDENT

- `GET /api/students/me/classmates` — таблица одногруппников
- `PUT /api/students/me` — форма смены ФИО

## Чеклист проверки

- [ ] **Вход** — войти как `admin`, `teacher_ivanov`, `student_petrov`
- [ ] **Admin: студенты** — создать, изменить, удалить студента
- [ ] **Admin: преподаватели** — создать, изменить, удалить; назначить группу
- [ ] **Admin: админы** — создать, просмотреть по ID, изменить, удалить
- [ ] **CSV-регистрация** — загрузить CSV с заголовком `ФИО,роль,email,группа`
- [ ] **MailHog** — открыть `http://localhost:8025`, найти письмо с ссылкой подтверждения
- [ ] **Подтверждение** — перейти по ссылке или вставить token на странице подтверждения; записать временный пароль
- [ ] **Вход новым пользователем** — войти с email и временным паролем
- [ ] **Teacher** — просмотреть студентов, изменить ФИО/группу
- [ ] **Student** — посмотреть одногруппников, изменить своё ФИО
- [ ] **Refresh token** — подождать 15+ мин или очистить accessToken в DevTools → убедиться, что запросы обновляются автоматически

## Пример CSV

```csv
ФИО,роль,email,группа
Иванов Иван Иванович,студент,ivanov@test.ru,VM
Петрова Мария,преподаватель,petrova@test.ru,
```
