<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Список персонала</h1>
      <button class="btn-primary" @click="openAddModal">+ Добавить сотрудника</button>
    </div>

    <div v-if="loading" class="state-msg">Загрузка...</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>

    <table v-else class="table">
      <thead>
        <tr>
          <th>ФИО</th>
          <th>Должность</th>
          <th>Уровень допуска</th>
          <th>Медосмотр до</th>
          <th>Статус</th>
          <th>Действия</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="emp in employees" :key="emp.id"
            :class="{ 'row-expired': emp.medicalExpired, 'row-warning': emp.medicalExpiringSoon && !emp.medicalExpired }">
          <td>{{ emp.fullName }}</td>
          <td>{{ emp.position || '—' }}</td>
          <td><span class="badge">{{ emp.clearanceLevel }}</span></td>
          <td>
            {{ formatDate(emp.medicalExpiry) }}
            <span v-if="emp.medicalExpired" class="expired-label">❌ Просрочен</span>
            <span v-else-if="emp.medicalExpiringSoon" class="warn-label">⚠ Скоро истекает</span>
          </td>
          <td>
            <span :class="emp.isActive ? 'status-active' : 'status-inactive'">
              {{ emp.isActive ? 'Активен' : 'Отстранён' }}
            </span>
          </td>
          <td>
            <div class="action-btns">
              <button class="btn-edit" @click="openEditModal(emp)">✏️ Изменить</button>
              <button class="btn-delete" @click="confirmDelete(emp)">🗑 Удалить</button>
            </div>
          </td>
        </tr>
        <tr v-if="employees.length === 0">
          <td colspan="6" class="empty">Нет данных</td>
        </tr>
      </tbody>
    </table>

    <!-- Модальное окно добавления/редактирования -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h2 class="modal-title">{{ editingEmployee ? 'Редактировать сотрудника' : 'Добавить сотрудника' }}</h2>

        <div class="form-group">
          <label>ФИО *</label>
          <input v-model="form.fullName" type="text" placeholder="Иванов Иван Иванович" />
          <span v-if="formErrors.fullName" class="field-error">{{ formErrors.fullName }}</span>
        </div>
        <div class="form-group">
          <label>Должность</label>
          <input v-model="form.position" type="text" placeholder="Оператор реакторного отделения" />
        </div>
        <div class="form-group">
          <label>Уровень допуска * (1–3)</label>
          <select v-model="form.clearanceLevel">
            <option value="" disabled>Выберите уровень</option>
            <option :value="1">1 — Базовый</option>
            <option :value="2">2 — Повышенный</option>
            <option :value="3">3 — Высший</option>
          </select>
          <span v-if="formErrors.clearanceLevel" class="field-error">{{ formErrors.clearanceLevel }}</span>
        </div>
        <div class="form-group">
          <label>Медосмотр действителен до *</label>
          <input v-model="form.medicalExpiry" type="date" />
          <span v-if="formErrors.medicalExpiry" class="field-error">{{ formErrors.medicalExpiry }}</span>
        </div>

        <div v-if="modalError" class="alert alert-error">❌ {{ modalError }}</div>

        <div class="modal-actions">
          <button class="btn-secondary" @click="closeModal">Отмена</button>
          <button class="btn-primary" @click="saveEmployee" :disabled="saving">
            {{ saving ? 'Сохранение...' : (editingEmployee ? 'Сохранить' : 'Добавить') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Модальное окно подтверждения удаления -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal modal-sm">
        <h2 class="modal-title">Подтверждение</h2>
        <p class="delete-text">Вы уверены что хотите деактивировать сотрудника<br><strong>{{ deletingEmployee?.fullName }}</strong>?</p>
        <p class="delete-hint">Сотрудник будет отстранён и не сможет быть назначен на дежурства.</p>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showDeleteModal = false">Отмена</button>
          <button class="btn-delete-confirm" @click="deleteEmployee" :disabled="saving">
            {{ saving ? 'Удаление...' : 'Деактивировать' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { employeeApi } from '../api/index.js'

const employees = ref([])
const loading = ref(true)
const error = ref(null)
const showModal = ref(false)
const showDeleteModal = ref(false)
const editingEmployee = ref(null)
const deletingEmployee = ref(null)
const saving = ref(false)
const modalError = ref('')
const formErrors = ref({})

const form = ref({ fullName: '', position: '', clearanceLevel: '', medicalExpiry: '' })

onMounted(loadEmployees)

async function loadEmployees() {
  try {
    const res = await employeeApi.getAll()
    employees.value = res.data
  } catch (e) {
    error.value = 'Не удалось загрузить список сотрудников.'
  } finally {
    loading.value = false
  }
}

function openAddModal() {
  editingEmployee.value = null
  form.value = { fullName: '', position: '', clearanceLevel: '', medicalExpiry: '' }
  formErrors.value = {}
  modalError.value = ''
  showModal.value = true
}

function openEditModal(emp) {
  editingEmployee.value = emp
  form.value = { fullName: emp.fullName, position: emp.position || '', clearanceLevel: emp.clearanceLevel, medicalExpiry: emp.medicalExpiry }
  formErrors.value = {}
  modalError.value = ''
  showModal.value = true
}

function closeModal() { showModal.value = false; editingEmployee.value = null }

function validateForm() {
  const errors = {}
  if (!form.value.fullName.trim()) errors.fullName = 'ФИО обязательно'
  if (!form.value.clearanceLevel) errors.clearanceLevel = 'Выберите уровень допуска'
  if (!form.value.medicalExpiry) errors.medicalExpiry = 'Укажите дату медосмотра'
  formErrors.value = errors
  return Object.keys(errors).length === 0
}

async function saveEmployee() {
  if (!validateForm()) return
  saving.value = true
  modalError.value = ''
  try {
    const payload = { fullName: form.value.fullName, position: form.value.position, clearanceLevel: Number(form.value.clearanceLevel), medicalExpiry: form.value.medicalExpiry }
    if (editingEmployee.value) {
      await employeeApi.update(editingEmployee.value.id, payload)
    } else {
      await employeeApi.create(payload)
    }
    closeModal()
    await loadEmployees()
  } catch (e) {
    modalError.value = e.response?.data?.error || 'Ошибка при сохранении.'
  } finally {
    saving.value = false
  }
}

function confirmDelete(emp) { deletingEmployee.value = emp; showDeleteModal.value = true }

async function deleteEmployee() {
  saving.value = true
  try {
    await employeeApi.remove(deletingEmployee.value.id)
    showDeleteModal.value = false
    await loadEmployees()
  } finally {
    saving.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const [y, m, d] = dateStr.split('-')
  return `${d}.${m}.${y}`
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 22px; font-weight: 700; color: #1a1a2e; }
.btn-primary { background: #1a1a2e; color: white; border: none; padding: 9px 20px; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-primary:hover:not(:disabled) { background: #2d2d4e; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary { background: #f0f0f0; color: #333; border: none; padding: 9px 20px; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-secondary:hover { background: #e0e0e0; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.table th { background: #1a1a2e; color: white; padding: 12px 16px; text-align: left; font-size: 13px; font-weight: 600; text-transform: uppercase; }
.table td { padding: 12px 16px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.table tbody tr:hover { background: #fafafa; }
.row-warning { background: #fff8e1 !important; }
.row-warning:hover { background: #fff3cd !important; }
.row-expired { background: #ffebee !important; }
.row-expired:hover { background: #ffcdd2 !important; }
.warn-label { color: #e65100; font-size: 12px; font-weight: 600; margin-left: 6px; }
.expired-label { color: #c62828; font-size: 12px; font-weight: 600; margin-left: 6px; }
.badge { background: #1a1a2e; color: #4fc3f7; padding: 2px 10px; border-radius: 12px; font-size: 13px; font-weight: 700; }
.status-active { background: #e8f5e9; color: #2e7d32; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.status-inactive { background: #ffebee; color: #c62828; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.action-btns { display: flex; gap: 8px; }
.btn-edit { background: #e3f2fd; color: #1565c0; border: none; padding: 5px 12px; border-radius: 5px; font-size: 13px; cursor: pointer; }
.btn-edit:hover { background: #bbdefb; }
.btn-delete { background: #ffebee; color: #c62828; border: none; padding: 5px 12px; border-radius: 5px; font-size: 13px; cursor: pointer; }
.btn-delete:hover { background: #ffcdd2; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: white; border-radius: 12px; padding: 32px; width: 480px; max-width: 95vw; box-shadow: 0 8px 32px rgba(0,0,0,0.2); }
.modal-sm { width: 380px; }
.modal-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin-bottom: 24px; }
.form-group { margin-bottom: 16px; display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #555; }
.form-group input, .form-group select { padding: 9px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; outline: none; }
.form-group input:focus, .form-group select:focus { border-color: #4fc3f7; }
.field-error { color: #c62828; font-size: 12px; }
.alert { padding: 10px 14px; border-radius: 6px; font-size: 13px; margin-bottom: 12px; }
.alert-error { background: #ffebee; color: #c62828; border: 1px solid #ffcdd2; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.delete-text { font-size: 15px; color: #333; margin-bottom: 8px; line-height: 1.5; }
.delete-hint { font-size: 13px; color: #888; }
.btn-delete-confirm { background: #c62828; color: white; border: none; padding: 9px 20px; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-delete-confirm:hover:not(:disabled) { background: #b71c1c; }
.btn-delete-confirm:disabled { opacity: 0.6; cursor: not-allowed; }
.state-msg { padding: 24px; text-align: center; color: #666; }
.state-msg.error { color: #c62828; }
.empty { text-align: center; color: #999; padding: 32px; }
</style>
