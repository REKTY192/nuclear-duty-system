import { createRouter, createWebHistory } from 'vue-router'
import EmployeesView from '../views/EmployeesView.vue'
import ShiftsView from '../views/ShiftsView.vue'
import ScheduleView from '../views/ScheduleView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/employees' },
    { path: '/employees', name: 'employees', component: EmployeesView },
    { path: '/shifts', name: 'shifts', component: ShiftsView },
    { path: '/schedule', name: 'schedule', component: ScheduleView }
  ]
})

export default router
