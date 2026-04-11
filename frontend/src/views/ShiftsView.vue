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

      <!-- Уведомление об ошибке -->
      <div v-if="errorMsg" class="alert alert-error">
        ❌ {{ errorMsg }}
      </div>

      <!-- Уведомление об успехе -->
      <div v-if="successMsg" class="alert alert-success">
        ✅ {{ successMsg }}
      </div>

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
          </tr>
        </thead>
        <tbody>
          <tr v-for="shift in shifts" :key="shift.id">
            <td>{{ shift.employeeName }}</td>
            <td>{{ shift.shiftTypeName }}</td>
            <td>{{ formatDate(shift.shiftDate) }}</td>
            <td>
              <span :class="statusClass(shift.status)">{{ shift.status }}</span>
            </td>
          </tr>
          <tr v-if="shifts.length === 0">
            <td colspan="4" class="empty">Смены не назначены</td>
          </tr>
        </tbody>
      </table>
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
const errorMsg   = ref('')
const successMsg = ref('')

const form = ref({
  employeeId:  '',
  shiftTypeId: '',
  shiftDate:   '',
  notes:       ''
})

onMounted(async () => {
  const [empRes, shiftsRes] = await Promise.all([
    employeeApi.getAll(),
    shiftApi.getAll()
  ])
  employees.value = empRes.data
  shifts.value    = shiftsRes.data
  loadingShifts.value = false

  // Загружаем типы смен
  try {
    const stRes = await shiftApi.getTypes()
    shiftTypes.value = stRes.data
  } catch {
    // если эндпоинт ещё не добавлен — не критично
  }
})

async function assignShift() {
  errorMsg.value   = ''
  successMsg.value = ''

  if (!form.value.employeeId || !form.value.shiftTypeId || !form.value.shiftDate) {
    errorMsg.value = 'Заполните все обязательные поля.'
    return
  }

  submitting.value = true
  try {
    await shiftApi.create({
      employeeId:  form.value.employeeId,
      shiftTypeId: form.value.shiftTypeId,
      shiftDate:   form.value.shiftDate,
      notes:       form.value.notes
    })
    successMsg.value = 'Дежурство успешно назначено!'
    form.value = { employeeId: '', shiftTypeId: '', shiftDate: '', notes: '' }

    // Обновляем список смен
    const res = await shiftApi.getAll()
    shifts.value = res.data
  } catch (e) {
    errorMsg.value = e.response?.data?.error || 'Произошла ошибка при назначении.'
  } finally {
    submitting.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const [y, m, d] = dateStr.split('-')
  return `${d}.${m}.${y}`
}

function statusClass(status) {
  if (status === 'SCHEDULED') return 'status-scheduled'
  if (status === 'COMPLETED') return 'status-completed'
  return 'status-cancelled'
}
</script>

<style scoped>
.shifts-layout {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
  align-items: start;
}

.card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #1a1a2e;
}

.form-group {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #555;
}

.form-group select,
.form-group input {
  padding: 9px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border 0.2s;
}

.form-group select:focus,
.form-group input:focus { border-color: #4fc3f7; }

.btn-submit {
  width: 100%;
  padding: 11px;
  background: #1a1a2e;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled) { background: #2d2d4e; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.alert {
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 12px;
}

.alert-error   { background: #ffebee; color: #c62828; border: 1px solid #ffcdd2; }
.alert-success { background: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; }

.table { width: 100%; border-collapse: collapse; }

.table th {
  background: #f5f5f5;
  padding: 10px 14px;
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  color: #666;
  border-bottom: 2px solid #eee;
}

.table td {
  padding: 11px 14px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.status-scheduled { background: #e3f2fd; color: #1565c0; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.status-completed  { background: #e8f5e9; color: #2e7d32; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.status-cancelled  { background: #ffebee; color: #c62828; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }

.state-msg { padding: 20px; text-align: center; color: #999; }
.empty { text-align: center; color: #999; padding: 28px; }
</style>
