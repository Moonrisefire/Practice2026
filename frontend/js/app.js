import { api, apiPublic, ApiError, getApiBase, setApiBase } from './api.js';
import { login, logout, isAuthenticated, getRole, getUsername } from './auth.js';

// --- UI helpers ---

function showStatus(message, type = 'info', details) {
  const el = document.getElementById('status');
  let text = message;
  if (details && typeof details === 'object') {
    const parts = Object.entries(details).map(([k, v]) => `${k}: ${v}`);
    text += ' — ' + parts.join('; ');
  }
  el.textContent = text;
  el.className = `show ${type}`;
}

function clearStatus() {
  const el = document.getElementById('status');
  el.className = '';
  el.textContent = '';
}

function formData(form) {
  const data = {};
  new FormData(form).forEach((v, k) => { data[k] = v; });
  return data;
}

function omitEmptyPassword(data) {
  if ('password' in data) {
    const password = data.password?.trim();
    if (!password) {
      delete data.password;
    } else {
      data.password = password;
    }
  }
  return data;
}

function esc(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function renderTable(headers, rows) {
  if (!rows.length) return '<p>Нет данных</p>';
  const ths = headers.map(h => `<th>${esc(h.label)}</th>`).join('');
  const trs = rows.map(row => {
    const tds = headers.map(h => `<td>${h.render ? h.render(row) : esc(row[h.key])}</td>`).join('');
    return `<tr>${tds}</tr>`;
  }).join('');
  return `<table><thead><tr>${ths}</tr></thead><tbody>${trs}</tbody></table>`;
}

async function handleError(err) {
  if (err instanceof ApiError) {
    showStatus(err.message, 'error', err.details);
  } else {
    showStatus(err.message || 'Неизвестная ошибка', 'error');
  }
}

// --- Routing ---

const SECTIONS = ['login', 'confirm', 'admin', 'teacher', 'student'];

function showSection(name) {
  SECTIONS.forEach(s => {
    document.getElementById(`section-${s}`).classList.toggle('active', s === name);
  });
}

function updateHeader() {
  const userInfo = document.getElementById('user-info');
  const logoutBtn = document.getElementById('logout-btn');
  if (isAuthenticated()) {
    userInfo.hidden = false;
    logoutBtn.hidden = false;
    userInfo.textContent = `${getUsername()} (${getRole()})`;
  } else {
    userInfo.hidden = true;
    logoutBtn.hidden = true;
  }
}

function route() {
  const params = new URLSearchParams(window.location.search);
  const token = params.get('token');

  if (token) {
    document.getElementById('confirm-token').value = token;
    showSection('confirm');
    updateHeader();
    return;
  }

  const hash = window.location.hash.replace('#', '') || 'login';

  if (hash === 'confirm') {
    showSection('confirm');
    updateHeader();
    return;
  }

  if (!isAuthenticated()) {
    showSection('login');
    updateHeader();
    return;
  }

  const role = getRole();
  const roleSection = { ADMIN: 'admin', TEACHER: 'teacher', STUDENT: 'student' }[role];
  showSection(roleSection || 'login');
  updateHeader();

  if (roleSection === 'admin') loadAdminData();
  if (roleSection === 'teacher') loadTeacherStudents();
  if (roleSection === 'student') loadStudentData();
}

// --- Auth ---

document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearStatus();
  try {
    await login(
      document.getElementById('login-username').value,
      document.getElementById('login-password').value
    );
    showStatus('Вход выполнен', 'success');
    window.location.hash = getRole().toLowerCase();
    route();
  } catch (err) {
    await handleError(err);
  }
});

document.getElementById('logout-btn').addEventListener('click', () => logout());

document.getElementById('api-base-input').addEventListener('change', (e) => {
  setApiBase(e.target.value);
  showStatus(`API URL: ${getApiBase()}`, 'info');
});

// --- Confirm registration ---

document.getElementById('confirm-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearStatus();
  const token = document.getElementById('confirm-token').value.trim();
  const resultEl = document.getElementById('confirm-result');
  resultEl.innerHTML = '';
  try {
    const data = await apiPublic('GET', `/api/auth/confirm?token=${encodeURIComponent(token)}`);
    resultEl.innerHTML = `
      <div class="confirm-result">
        <p>${esc(data.message)}</p>
        <strong>Логин: ${esc(data.username)}</strong>
        <strong>Временный пароль: ${esc(data.temporaryPassword)}</strong>
      </div>`;
    showStatus('Регистрация подтверждена', 'success');
  } catch (err) {
    await handleError(err);
  }
});

// --- Admin tabs ---

document.getElementById('admin-tabs').addEventListener('click', (e) => {
  if (!e.target.classList.contains('tab')) return;
  const tab = e.target.dataset.tab;
  document.querySelectorAll('#admin-tabs .tab').forEach(t => t.classList.toggle('active', t === e.target));
  document.querySelectorAll('#section-admin .tab-panel').forEach(p => {
    p.classList.toggle('active', p.id === `admin-${tab}`);
  });
});

async function loadAdminData() {
  await Promise.all([loadStudents(), loadTeachers(), loadAdmins()]);
}

// --- Admin: Students ---

async function loadStudents() {
  try {
    const students = await api('GET', '/api/admin/students');
    const container = document.getElementById('students-table');
    container.innerHTML = renderTable(
      [
        { key: 'id', label: 'ID' },
        { key: 'username', label: 'Логин' },
        { key: 'fio', label: 'ФИО' },
        { key: 'groupName', label: 'Группа' },
        {
          key: 'actions',
          label: 'Действия',
          render: (s) => `
            <div class="actions">
              <button class="small" data-edit-student="${s.id}">Изменить</button>
              <button class="small danger" data-delete-student="${s.id}">Удалить</button>
            </div>
            <div id="edit-student-${s.id}"></div>`
        }
      ],
      students
    );
  } catch (err) {
    await handleError(err);
  }
}

document.getElementById('student-create-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearStatus();
  try {
    await api('POST', '/api/admin/students', formData(e.target));
    e.target.reset();
    showStatus('Студент создан', 'success');
    await loadStudents();
  } catch (err) {
    await handleError(err);
  }
});

document.getElementById('students-table').addEventListener('click', async (e) => {
  const deleteId = e.target.dataset.deleteStudent;
  if (deleteId) {
    if (!confirm('Удалить студента?')) return;
    clearStatus();
    try {
      await api('DELETE', `/api/admin/students/${deleteId}`);
      showStatus('Студент удалён', 'success');
      await loadStudents();
    } catch (err) {
      await handleError(err);
    }
    return;
  }

  const editId = e.target.dataset.editStudent;
  if (editId) {
    const container = document.getElementById(`edit-student-${editId}`);
    if (container.innerHTML) {
      container.innerHTML = '';
      return;
    }
    try {
      const students = await api('GET', '/api/admin/students');
      const s = students.find(x => String(x.id) === editId);
      if (!s) return;
      container.innerHTML = `
        <form class="inline-form" data-update-student="${s.id}">
          <label class="field">Логин <input name="username" value="${esc(s.username)}" required></label>
          <label class="field">ФИО <input name="fio" value="${esc(s.fio)}" required></label>
          <label class="field">Группа <input name="groupName" value="${esc(s.groupName)}" required></label>
          <label class="field">Новый пароль <input name="password" type="password" placeholder="не менять"></label>
          <button type="submit">Сохранить</button>
        </form>`;
    } catch (err) {
      await handleError(err);
    }
  }
});

document.getElementById('students-table').addEventListener('submit', async (e) => {
  const form = e.target.closest('[data-update-student]');
  if (!form) return;
  e.preventDefault();
  clearStatus();
  const id = form.dataset.updateStudent;
  const data = omitEmptyPassword(formData(form));
  data.id = Number(id);
  data.role = 'STUDENT';
  try {
    await api('PUT', `/api/admin/students/${id}`, data);
    showStatus('Студент обновлён', 'success');
    await loadStudents();
  } catch (err) {
    await handleError(err);
  }
});

// --- Admin: Teachers ---

async function loadTeachers() {
  try {
    const teachers = await api('GET', '/api/admin/teachers');
    const container = document.getElementById('teachers-table');
    container.innerHTML = renderTable(
      [
        { key: 'id', label: 'ID' },
        { key: 'username', label: 'Логин' },
        { key: 'fio', label: 'ФИО' },
        { key: 'email', label: 'Email' },
        { key: 'phone', label: 'Телефон' },
        {
          key: 'assignedGroups',
          label: 'Группы',
          render: (t) => esc((t.assignedGroups || []).join(', '))
        },
        {
          key: 'actions',
          label: 'Действия',
          render: (t) => `
            <div class="actions">
              <button class="small" data-edit-teacher="${t.id}">Изменить</button>
              <button class="small" data-assign-group="${t.id}">+ Группа</button>
              <button class="small danger" data-delete-teacher="${t.id}">Удалить</button>
            </div>
            <div id="edit-teacher-${t.id}"></div>
            <div id="assign-group-${t.id}"></div>`
        }
      ],
      teachers
    );
  } catch (err) {
    await handleError(err);
  }
}

document.getElementById('teacher-create-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearStatus();
  const data = formData(e.target);
  if (!data.phone) delete data.phone;
  try {
    await api('POST', '/api/admin/teachers', data);
    e.target.reset();
    showStatus('Преподаватель создан', 'success');
    await loadTeachers();
  } catch (err) {
    await handleError(err);
  }
});

document.getElementById('teachers-table').addEventListener('click', async (e) => {
  const deleteId = e.target.dataset.deleteTeacher;
  if (deleteId) {
    if (!confirm('Удалить преподавателя?')) return;
    clearStatus();
    try {
      await api('DELETE', `/api/admin/teachers/${deleteId}`);
      showStatus('Преподаватель удалён', 'success');
      await loadTeachers();
    } catch (err) {
      await handleError(err);
    }
    return;
  }

  const assignId = e.target.dataset.assignGroup;
  if (assignId) {
    const container = document.getElementById(`assign-group-${assignId}`);
    container.innerHTML = `
      <form class="inline-form" data-assign-form="${assignId}">
        <label class="field">Группа <input name="groupName" required></label>
        <button type="submit">Назначить</button>
      </form>`;
    return;
  }

  const editId = e.target.dataset.editTeacher;
  if (editId) {
    const container = document.getElementById(`edit-teacher-${editId}`);
    if (container.innerHTML) {
      container.innerHTML = '';
      return;
    }
    try {
      const teachers = await api('GET', '/api/admin/teachers');
      const t = teachers.find(x => String(x.id) === editId);
      if (!t) return;
      container.innerHTML = `
        <form class="inline-form" data-update-teacher="${t.id}">
          <label class="field">Логин <input name="username" value="${esc(t.username)}" required></label>
          <label class="field">ФИО <input name="fio" value="${esc(t.fio)}" required></label>
          <label class="field">Email <input name="email" type="email" value="${esc(t.email)}" required></label>
          <label class="field">Телефон <input name="phone" value="${esc(t.phone || '')}"></label>
          <label class="field">Новый пароль <input name="password" type="password" placeholder="не менять"></label>
          <button type="submit">Сохранить</button>
        </form>`;
    } catch (err) {
      await handleError(err);
    }
  }
});

document.getElementById('teachers-table').addEventListener('submit', async (e) => {
  const assignForm = e.target.closest('[data-assign-form]');
  if (assignForm) {
    e.preventDefault();
    clearStatus();
    const id = assignForm.dataset.assignForm;
    try {
      await api('POST', `/api/admin/teachers/${id}/groups`, formData(assignForm));
      showStatus('Группа назначена', 'success');
      await loadTeachers();
    } catch (err) {
      await handleError(err);
    }
    return;
  }

  const form = e.target.closest('[data-update-teacher]');
  if (!form) return;
  e.preventDefault();
  clearStatus();
  const id = form.dataset.updateTeacher;
  const data = omitEmptyPassword(formData(form));
  if (!data.phone) delete data.phone;
  data.id = Number(id);
  try {
    const teachers = await api('GET', '/api/admin/teachers');
    const existing = teachers.find(x => String(x.id) === id);
    data.assignedGroups = existing?.assignedGroups || [];
    await api('PUT', `/api/admin/teachers/${id}`, data);
    showStatus('Преподаватель обновлён', 'success');
    await loadTeachers();
  } catch (err) {
    await handleError(err);
  }
});

// --- Admin: Admins ---

async function loadAdmins() {
  try {
    const admins = await api('GET', '/api/admin/admins');
    const container = document.getElementById('admins-table');
    container.innerHTML = renderTable(
      [
        { key: 'id', label: 'ID' },
        { key: 'username', label: 'Логин' },
        {
          key: 'actions',
          label: 'Действия',
          render: (a) => `
            <div class="actions">
              <button class="small" data-view-admin="${a.id}">Просмотр</button>
              <button class="small" data-edit-admin="${a.id}">Изменить</button>
              <button class="small danger" data-delete-admin="${a.id}">Удалить</button>
            </div>
            <div id="edit-admin-${a.id}"></div>
            <div id="view-admin-${a.id}"></div>`
        }
      ],
      admins
    );
  } catch (err) {
    await handleError(err);
  }
}

document.getElementById('admin-create-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearStatus();
  try {
    await api('POST', '/api/admin/admins', formData(e.target));
    e.target.reset();
    showStatus('Администратор создан', 'success');
    await loadAdmins();
  } catch (err) {
    await handleError(err);
  }
});

document.getElementById('admins-table').addEventListener('click', async (e) => {
  const deleteId = e.target.dataset.deleteAdmin;
  if (deleteId) {
    if (!confirm('Удалить администратора?')) return;
    clearStatus();
    try {
      await api('DELETE', `/api/admin/admins/${deleteId}`);
      showStatus('Администратор удалён', 'success');
      await loadAdmins();
    } catch (err) {
      await handleError(err);
    }
    return;
  }

  const viewId = e.target.dataset.viewAdmin;
  if (viewId) {
    clearStatus();
    try {
      const admin = await api('GET', `/api/admin/admins/${viewId}`);
      document.getElementById(`view-admin-${viewId}`).innerHTML =
        `<pre class="json">${esc(JSON.stringify(admin, null, 2))}</pre>`;
    } catch (err) {
      await handleError(err);
    }
    return;
  }

  const editId = e.target.dataset.editAdmin;
  if (editId) {
    const container = document.getElementById(`edit-admin-${editId}`);
    if (container.innerHTML) {
      container.innerHTML = '';
      return;
    }
    try {
      const admins = await api('GET', '/api/admin/admins');
      const a = admins.find(x => String(x.id) === editId);
      if (!a) return;
      container.innerHTML = `
        <form class="inline-form" data-update-admin="${a.id}">
          <label class="field">Логин <input name="username" value="${esc(a.username)}" required></label>
          <label class="field">Новый пароль <input name="password" type="password" placeholder="не менять"></label>
          <button type="submit">Сохранить</button>
        </form>`;
    } catch (err) {
      await handleError(err);
    }
  }
});

document.getElementById('admins-table').addEventListener('submit', async (e) => {
  const form = e.target.closest('[data-update-admin]');
  if (!form) return;
  e.preventDefault();
  clearStatus();
  const id = form.dataset.updateAdmin;
  const data = omitEmptyPassword(formData(form));
  data.id = Number(id);
  try {
    await api('PUT', `/api/admin/admins/${id}`, data);
    showStatus('Администратор обновлён', 'success');
    await loadAdmins();
  } catch (err) {
    await handleError(err);
  }
});

// --- Admin: CSV ---

document.getElementById('csv-upload-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearStatus();
  const fileInput = e.target.querySelector('input[type="file"]');
  const fd = new FormData();
  fd.append('file', fileInput.files[0]);
  try {
    const result = await api('POST', '/api/admin/registration-requests', fd, { multipart: true });
    const el = document.getElementById('csv-result');
    el.innerHTML = `<pre class="json">${esc(JSON.stringify(result, null, 2))}</pre>`;
    showStatus(`Загружено: ${result.created?.length || 0}, ошибок: ${result.errors?.length || 0}`, 'success');
    e.target.reset();
  } catch (err) {
    await handleError(err);
  }
});

// --- Teacher ---

async function loadTeacherStudents() {
  try {
    const students = await api('GET', '/api/teachers/me/students');
    const container = document.getElementById('teacher-students-table');
    container.innerHTML = renderTable(
      [
        { key: 'id', label: 'ID' },
        { key: 'username', label: 'Логин' },
        { key: 'fio', label: 'ФИО' },
        { key: 'groupName', label: 'Группа' },
        {
          key: 'actions',
          label: 'Действия',
          render: (s) => `
            <button class="small" data-edit-ts="${s.id}">Изменить</button>
            <div id="edit-ts-${s.id}"></div>`
        }
      ],
      students
    );
  } catch (err) {
    await handleError(err);
  }
}

document.getElementById('teacher-students-table').addEventListener('click', async (e) => {
  const editId = e.target.dataset.editTs;
  if (!editId) return;
  const container = document.getElementById(`edit-ts-${editId}`);
  if (container.innerHTML) {
    container.innerHTML = '';
    return;
  }
  try {
    const students = await api('GET', '/api/teachers/me/students');
    const s = students.find(x => String(x.id) === editId);
    if (!s) return;
    container.innerHTML = `
      <form class="inline-form" data-update-ts="${s.id}">
        <label class="field">ФИО <input name="fio" value="${esc(s.fio)}"></label>
        <label class="field">Группа <input name="groupName" value="${esc(s.groupName)}"></label>
        <button type="submit">Сохранить</button>
      </form>`;
  } catch (err) {
    await handleError(err);
  }
});

document.getElementById('teacher-students-table').addEventListener('submit', async (e) => {
  const form = e.target.closest('[data-update-ts]');
  if (!form) return;
  e.preventDefault();
  clearStatus();
  const id = form.dataset.updateTs;
  const data = formData(form);
  if (!data.fio) delete data.fio;
  if (!data.groupName) delete data.groupName;
  try {
    await api('PUT', `/api/teachers/me/students/${id}`, data);
    showStatus('Студент обновлён', 'success');
    await loadTeacherStudents();
  } catch (err) {
    await handleError(err);
  }
});

// --- Student ---

async function loadStudentData() {
  try {
    const classmates = await api('GET', '/api/students/me/classmates');
    document.getElementById('classmates-table').innerHTML = renderTable(
      [
        { key: 'id', label: 'ID' },
        { key: 'username', label: 'Логин' },
        { key: 'fio', label: 'ФИО' },
        { key: 'groupName', label: 'Группа' }
      ],
      classmates
    );
    const me = classmates.find(c => c.username === getUsername());
    if (me) {
      document.getElementById('student-fio-input').value = me.fio;
    }
  } catch (err) {
    await handleError(err);
  }
}

document.getElementById('student-self-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearStatus();
  try {
    const data = await api('PUT', '/api/students/me', formData(e.target));
    document.getElementById('student-fio-input').value = data.fio;
    showStatus('Профиль обновлён', 'success');
    await loadStudentData();
  } catch (err) {
    await handleError(err);
  }
});

// --- Init ---

document.getElementById('api-base-input').value = getApiBase();
window.addEventListener('hashchange', route);
route();
