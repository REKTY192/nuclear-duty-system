<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">График дежурств</h1>
      <div class="period-controls">
        <button class="btn-nav" @click="prevMonth">◀</button>
        <span class="period-label">{{ monthLabel }}</span>
        <button class="btn-nav" @click="nextMonth">▶</button>
      </div>
    </div>

    <div v-if="loading" class="state-msg">Загрузка...</div>

    <div v-else class="schedule-wrapper">
      <table class="schedule-table">
        <thead>
          <tr>
            <th class="emp-col">Сотрудник</th>
            <th
              v-for="day in daysInMonth"
              :key="day"
              :class="{ 'today-col': isToday(day) }"
            >
              <div class="day-num">{{ day }}</div>
              <div class="day-name">{{ getDayName(day) }}</div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="emp in employees" :key="emp.id">
            <td class="emp-name">
              <div class="emp-info">
                <span class="emp-full-name">{{ emp.fullName }}</span>
                <span class="emp-position">{{ emp.position || '' }}</span>
              </div>
            </td>
            <td
              v-for="day in daysInMonth"
              :key="day"
              :class="getCellClass(emp.id, day)"
              :title="getCellTitle(emp.id, day)"
            >
              <span v-if="getShift(emp.id, day)" class="shift-badge">
                {{ getShift(emp.id, day).shiftTypeName.slice(0, 3) }}
              </span>
            </td>
          </tr>
          <tr v-if="employees.length === 0">
            <td colspan="32" class="empty">Нет данных</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Легенда -->
    <div class="legend">
      <span class="legend-item">
        <span class="legend-box shift-day"></span> Дневная
      </span>
      <span class="legend-item">
        <span class="legend-box shift-night"></span> Ночная
      </span>
      <span class="legend-item">
        <span class="legend-box shift-full"></span> Суточная
      </span>
      <span class="legend-item">
        <span class="legend-box today-box"></span> Сегодня
      </span>
    </div>

    <!-- Сводка -->
    <div class="summary-grid">
      <div class="summary-card" v-for="emp in employees" :key="'s' + emp.id">
        <div class="summary-name">{{ emp.fullName }}</div>
        <div class="summary-stats">
          <span class="stat">
            <span class="stat-num">{{ countShifts(emp.id) }}</span>
            <span class="stat-label">смен</span>
          </span>
          <span class="stat">
            <span class="stat-num">{{ countHours(emp.id) }}</span>
            <span class="stat-label">часов</span>
          </span>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { employeeApi, shiftApi } from '../api/index.js'

const employees = ref([])
const shifts = ref([])
const loading = ref(true)

const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth()) // 0-based

const monthNames = [
  'Январь','Февраль','Март','Апрель','Май','Июнь',
  'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'
]
const dayNames = ['Вс','Пн','Вт','Ср','Чт','Пт','Сб']

const monthLabel = computed(() =>
  `${monthNames[currentMonth.value]} ${currentYear.value}`
)

const daysInMonth = computed(() => {
  const count = new Date(currentYear.value, currentMonth.value + 1, 0).getDate()
  return Array.from({ length: count }, (_, i) => i + 1)
})

function isToday(day) {
  return today.getFullYear() === currentYear.value &&
         today.getMonth() === currentMonth.value &&
         today.getDate() === day
}

function getDayName(day) {
  const d = new Date(currentYear.value, currentMonth.value, day)
  return dayNames[d.getDay()]
}

function getShift(employeeId, day) {
  const dateStr = `${currentYear.value}-${String(currentMonth.value + 1).padStart(2,'0')}-${String(day).padStart(2,'0')}`
  return shifts.value.find(s => s.employeeId === employeeId && s.shiftDate === dateStr && s.status !== 'CANCELLED')
}

function getCellClass(employeeId, day) {
  const shift = getShift(employeeId, day)
  if (!shift) return isToday(day) ? 'cell-today' : ''
  const name = shift.shiftTypeName.toLowerCase()
  if (name.includes('дн') || name.includes('day')) return 'cell-day' + (isToday(day) ? ' cell-today' : '')
  if (name.includes('ноч') || name.includes('night')) return 'cell-night' + (isToday(day) ? ' cell-today' : '')
  return 'cell-full' + (isToday(day) ? ' cell-today' : '')
}

function getCellTitle(employeeId, day) {
  const shift = getShift(employeeId, day)
  if (!shift) return ''
  return `${shift.shiftTypeName} — ${shift.status}`
}

function countShifts(employeeId) {
  return shifts.value.filter(s =>
    s.employeeId === employeeId &&
    s.status !== 'CANCELLED' &&
    new Date(s.shiftDate).getMonth() === currentMonth.value &&
    new Date(s.shiftDate).getFullYear() === currentYear.value
  ).length
}

function countHours(employeeId) {
  return shifts.value
    .filter(s =>
      s.employeeId === employeeId &&
      s.status !== 'CANCELLED' &&
      new Date(s.shiftDate).getMonth() === currentMonth.value &&
      new Date(s.shiftDate).getFullYear() === currentYear.value
    )
    .reduce((sum, s) => sum + (s.durationHours || 12), 0)
}

async function loadData() {
  loading.value = true
  try {
    const from = `${currentYear.value}-${String(currentMonth.value + 1).padStart(2,'0')}-01`
    const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0).getDate()
    const to = `${currentYear.value}-${String(currentMonth.value + 1).padStart(2,'0')}-${lastDay}`

    const [empRes, shiftRes] = await Promise.all([
      employeeApi.getAll(),
      shiftApi.getSchedule(from, to)
    ])
    employees.value = empRes.data
    shifts.value = shiftRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function prevMonth() {
  if (currentMonth.value === 0) { currentMonth.value = 11; currentYear.value-- }
  else currentMonth.value--
  loadData()
}

function nextMonth() {
  if (currentMonth.value === 11) { currentMonth.value = 0; currentYear.value++ }
  else currentMonth.value++
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title { font-size: 22px; font-weight: 700; color: #1a1a2e; }

.period-controls { display: flex; align-items: center; gap: 16px; }
.period-label { font-size: 18px; font-weight: 700; color: #1a1a2e; min-width: 180px; text-align: center; }
.btn-nav {
  background: #1a1a2e;
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-nav:hover { background: #2d2d4e; }

.schedule-wrapper {
  overflow-x: auto;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  margin-bottom: 20px;
}

.schedule-table {
  border-collapse: collapse;
  min-width: 100%;
  font-size: 13px;
}

.schedule-table th {
  background: #1a1a2e;
  color: white;
  padding: 8px 4px;
  text-align: center;
  min-width: 36px;
  white-space: nowrap;
}

.schedule-table th.today-col { background: #4fc3f7; color: #1a1a2e; }
.emp-col { min-width: 200px !important; text-align: left !important; padding: 8px 12px !important; }

.day-num { font-weight: 700; font-size: 14px; }
.day-name { font-size: 10px; opacity: 0.8; }

.schedule-table td {
  border: 1px solid #f0f0f0;
  padding: 4px;
  text-align: center;
  height: 44px;
  min-width: 36px;
}

.emp-name { text-align: left !important; padding: 8px 12px !important; border-right: 2px solid #e0e0e0; }
.emp-info { display: flex; flex-direction: column; }
.emp-full-name { font-weight: 600; font-size: 13px; color: #1a1a2e; }
.emp-position { font-size: 11px; color: #888; }

.cell-day { background: #e3f2fd; }
.cell-night { background: #ede7f6; }
.cell-full { background: #e8f5e9; }
.cell-today { outline: 2px solid #4fc3f7; outline-offset: -2px; }

.shift-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: #1a1a2e;
}

/* Легенда */
.legend {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #555; }
.legend-box { display: inline-block; width: 24px; height: 16px; border-radius: 3px; border: 1px solid #ddd; }
.shift-day { background: #e3f2fd; }
.shift-night { background: #ede7f6; }
.shift-full { background: #e8f5e9; }
.today-box { background: white; outline: 2px solid #4fc3f7; outline-offset: -2px; }

/* Сводка */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.summary-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
}
.summary-name { font-weight: 700; font-size: 14px; color: #1a1a2e; margin-bottom: 10px; }
.summary-stats { display: flex; gap: 20px; }
.stat { display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 24px; font-weight: 800; color: #1a1a2e; }
.stat-label { font-size: 11px; color: #888; }

.state-msg { padding: 40px; text-align: center; color: #999; }
.empty { text-align: center; color: #999; padding: 32px; }
</style>
