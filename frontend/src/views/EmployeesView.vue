<template>
  <div>
    <h1 class="page-title">Список персонала</h1>

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
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="emp in employees"
          :key="emp.id"
          :class="{ 'row-warning': emp.medicalExpiringSoon }"
        >
          <td>{{ emp.fullName }}</td>
          <td>{{ emp.position || '—' }}</td>
          <td>
            <span class="badge">{{ emp.clearanceLevel }}</span>
          </td>
          <td>
            {{ formatDate(emp.medicalExpiry) }}
            <span v-if="emp.medicalExpiringSoon" class="warn-label">⚠ Скоро истекает</span>
          </td>
          <td>
            <span :class="emp.isActive ? 'status-active' : 'status-inactive'">
              {{ emp.isActive ? 'Активен' : 'Отстранён' }}
            </span>
          </td>
        </tr>
        <tr v-if="employees.length === 0">
          <td colspan="5" class="empty">Нет данных</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { employeeApi } from '../api/index.js'

const employees = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    const res = await employeeApi.getAll()
    employees.value = res.data
  } catch (e) {
    error.value = 'Не удалось загрузить список сотрудников. Проверьте что бэкенд запущен.'
  } finally {
    loading.value = false
  }
})

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const [y, m, d] = dateStr.split('-')
  return `${d}.${m}.${y}`
}
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #1a1a2e;
}

.table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.table th {
  background: #1a1a2e;
  color: white;
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.table tbody tr:hover { background: #fafafa; }

.row-warning { background: #fff8e1 !important; }
.row-warning:hover { background: #fff3cd !important; }

.warn-label {
  color: #e65100;
  font-size: 12px;
  font-weight: 600;
  margin-left: 6px;
}

.badge {
  background: #1a1a2e;
  color: #4fc3f7;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
}

.status-active {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-inactive {
  background: #ffebee;
  color: #c62828;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.state-msg { padding: 24px; text-align: center; color: #666; }
.state-msg.error { color: #c62828; }
.empty { text-align: center; color: #999; padding: 32px; }
</style>
