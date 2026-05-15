<template>
  <div class="shifts-layout">

    <!-- Панель назначения -->
    <div class="card form-card">
      <h2 class="card-title">Назначить дежурство</h2>

      <div class="form-group">
        <label>Сотрудник</label>
        <select v-model="form.employeeId">
          <option value="" disabled>Выберите сотрудника</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">
            {{ emp.fullName }} (допуск {{ emp.clearanceLevel }})
          </option>
        </select>
      </div>

      <div class="form-group">
        <label>Тип смены</label>
        <select v-model="form.shiftTypeId">
          <option value="" disabled>Выберите тип смены</option>
          <option v-for="st in shiftTypes" :key="st.id" :value="st.id">
            {{ st.name }} ({{ st.startTime.slice(0,5) }}, {{ st.durationHours }}ч)
          </option>
        </select>
      </div>

      <div class="form-group">
        <label>Дата дежурства</label>
        <input type="date" v-model="form.shiftDate" />
      </div>

      <div class="form-group">
        <label>Примечание (необязательно)</label>
        <input type="text" v-model="form.notes" placeholder="Доп. информация..." />
      </div>

      <div v-if="errorMsg" class="alert alert-error">❌ {{ errorMsg }}</div>
      <div v-if="successMsg" class="alert alert-success">✅ {{ successMsg }}</div>

      <button class="btn-submit" @click="assignShift" :disabled="submitting">
        {{ submitting ? 'Назначение...' : 'Назначить дежурство' }}
      </button>
    </div>

    <!-- Список смен -->
    <div class="card shifts-card">
      <h2 class="card-title">Назначенные дежурства</h2>

      <div v-if="loadingShifts" class="state-msg">Загрузка...</div>

      <table v-else class="table">
        <thead>
          <tr>
            <th>Сотрудник</th>
            <th>Тип смены</th>
            <th>Дата</th>
            <th>Статус</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="shift in shifts" :key="shift.id" :class="rowClass(shift.status)">
            <td>{{ shift.employeeName }}</td>
            <td>{{ shift.shiftTypeName }}</td>
            <td>{{ formatDate(shift.shiftDate) }}</td>
            <td><span :class="statusClass(shift.status)">{{ statusLabel(shift.status) }}</span></td>
            <td>
              <div class="action-btns">
                <template v-if="shift.status === 'SCHEDULED'">
                  <button class="btn-complete" @click="changeStatus(shift, 'COMPLETED')" title="Завершить">✔ Завершить</button>
                  <button class="btn-cancel" @click="changeStatus(shift, 'CANCELLED')" title="Отменить">✖ Отменить</button>
                </template>
                <button class="btn-del" @click="confirmDeleteShift(shift)" title="Удалить">🗑</button>
              </div>
            </td>
          </tr>
          <tr v-if="shifts.length === 0">
            <td colspan="5" class="empty">Смены не назначены</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Подтверждение удаления смены -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal">
        <h2 class="modal-title">Удалить смену?</h2>
        <p class="delete-text">Смена сотрудника <strong>{{ deletingShift?.employeeName }}</strong> на {{ formatDate(deletingShift?.shiftDate) }} будет удалена безвозвратно.</p>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showDeleteModal = false">Отмена</button>
          <button class="btn-delete-confirm" @click="deleteShift" :disabled="actionLoading">
            {{ actionLoading ? 'Удаление...' : 'Удалить' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { employeeApi, shiftApi } from '../api/index.js'

const employees  = ref([])
const shiftTypes = ref([])
const shifts     = ref([])
const loadingShifts = ref(true)
const submitting = ref(false)
const actionLoading = ref(false)
const errorMsg   = ref('')
const successMsg = ref('')
const showDeleteModal = ref(false)
const deletingShift = ref(null)

const form = ref({ employeeId: '', shiftTypeId: '', shiftDate: '', notes: '' })

onMounted(async () => {
  const [empRes, shiftsRes, stRes] = await Promise.all([
    employeeApi.getAll(),
    shiftApi.getAll(),
    shiftApi.getTypes()
  ])
  employees.value = empRes.data
  shifts.value = shiftsRes.data
  shiftTypes.value = stRes.data
  loadingShifts.value = false
})

async function assignShift() {
  errorMsg.value = ''
  successMsg.value = ''
  if (!form.value.employeeId || !form.value.shiftTypeId || !form.value.shiftDate) {
    errorMsg.value = 'Заполните все обязательные поля.'
    return
  }
  submitting.value = true
  try {
    await shiftApi.create({ employeeId: form.value.employeeId, shiftTypeId: form.value.shiftTypeId, shiftDate: form.value.shiftDate, notes: form.value.notes })
    successMsg.value = 'Дежурство успешно назначено!'
    form.value = { employeeId: '', shiftTypeId: '', shiftDate: '', notes: '' }
    const res = await shiftApi.getAll()
    shifts.value = res.data
  } catch (e) {
    errorMsg.value = e.response?.data?.error || 'Произошла ошибка при назначении.'
  } finally {
    submitting.value = false
  }
}

async function changeStatus(shift, status) {
  try {
    const res = await shiftApi.updateStatus(shift.id, status)
    const idx = shifts.value.findIndex(s => s.id === shift.id)
    if (idx !== -1) shifts.value[idx] = res.data
  } catch (e) {
    alert('Ошибка при изменении статуса')
  }
}

function confirmDeleteShift(shift) {
  deletingShift.value = shift
  showDeleteModal.value = true
}

async function deleteShift() {
  actionLoading.value = true
  try {
    await shiftApi.remove(deletingShift.value.id)
    shifts.value = shifts.value.filter(s => s.id !== deletingShift.value.id)
    showDeleteModal.value = false
  } catch (e) {
    alert('Ошибка при удалении смены')
  } finally {
    actionLoading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const [y, m, d] = dateStr.split('-')
  return `${d}.${m}.${y}`
}

function statusLabel(status) {
  if (status === 'SCHEDULED') return 'Запланирована'
  if (status === 'COMPLETED') return 'Завершена'
  if (status === 'CANCELLED') return 'Отменена'
  return status
}

function statusClass(status) {
  if (status === 'SCHEDULED') return 'status-scheduled'
  if (status === 'COMPLETED') return 'status-completed'
  return 'status-cancelled'
}

function rowClass(status) {
  if (status === 'COMPLETED') return 'row-completed'
  if (status === 'CANCELLED') return 'row-cancelled'
  return ''
}
</script>

<style scoped>
.shifts-layout { display: grid; grid-template-columns: 360px 1fr; gap: 24px; align-items: start; }
.card { background: white; border-radius: 10px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.card-title { font-size: 18px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.form-group { margin-bottom: 16px; display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #555; }
.form-group select, .form-group input { padding: 9px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; outline: none; }
.form-group select:focus, .form-group input:focus { border-color: #4fc3f7; }
.btn-submit { width: 100%; padding: 11px; background: #1a1a2e; color: white; border: none; border-radius: 6px; font-size: 15px; font-weight: 600; cursor: pointer; margin-top: 8px; }
.btn-submit:hover:not(:disabled) { background: #2d2d4e; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.alert { padding: 10px 14px; border-radius: 6px; font-size: 13px; font-weight: 500; margin-bottom: 12px; }
.alert-error { background: #ffebee; color: #c62828; border: 1px solid #ffcdd2; }
.alert-success { background: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; }
.table { width: 100%; border-collapse: collapse; }
.table th { background: #f5f5f5; padding: 10px 12px; text-align: left; font-size: 12px; font-weight: 700; text-transform: uppercase; color: #666; border-bottom: 2px solid #eee; }
.table td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; font-size: 13px; }
.row-completed { opacity: 0.7; }
.row-cancelled { opacity: 0.5; }
.status-scheduled { background: #e3f2fd; color: #1565c0; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.status-completed { background: #e8f5e9; color: #2e7d32; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.status-cancelled { background: #ffebee; color: #c62828; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.action-btns { display: flex; gap: 6px; flex-wrap: wrap; }
.btn-complete { background: #e8f5e9; color: #2e7d32; border: none; padding: 4px 10px; border-radius: 5px; font-size: 12px; cursor: pointer; font-weight: 600; }
.btn-complete:hover { background: #c8e6c9; }
.btn-cancel { background: #fff8e1; color: #e65100; border: none; padding: 4px 10px; border-radius: 5px; font-size: 12px; cursor: pointer; font-weight: 600; }
.btn-cancel:hover { background: #ffecb3; }
.btn-del { background: #ffebee; color: #c62828; border: none; padding: 4px 8px; border-radius: 5px; font-size: 13px; cursor: pointer; }
.btn-del:hover { background: #ffcdd2; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: white; border-radius: 12px; padding: 32px; width: 420px; max-width: 95vw; box-shadow: 0 8px 32px rgba(0,0,0,0.2); }
.modal-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin-bottom: 16px; }
.delete-text { font-size: 14px; color: #333; line-height: 1.6; margin-bottom: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.btn-secondary { background: #f0f0f0; color: #333; border: none; padding: 9px 20px; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-delete-confirm { background: #c62828; color: white; border: none; padding: 9px 20px; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-delete-confirm:disabled { opacity: 0.6; cursor: not-allowed; }
.state-msg { padding: 20px; text-align: center; color: #999; }
.empty { text-align: center; color: #999; padding: 28px; }
</style>
