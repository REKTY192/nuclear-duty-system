import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

export const employeeApi = {
  getAll:   () => api.get('/employees'),
  create:   (data) => api.post('/employees', data),
  update:   (id, data) => api.put(`/employees/${id}`, data),
  remove:   (id) => api.delete(`/employees/${id}`)
}

export const shiftApi = {
  getAll:      () => api.get('/shifts'),
  create:      (data) => api.post('/shifts', data),
  getTypes:    () => api.get('/shift-types'),
  getSchedule: (from, to) => api.get(`/shifts/schedule?from=${from}&to=${to}`)
}

export default api
