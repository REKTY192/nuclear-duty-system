import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

export const employeeApi = {
  getAll: () => api.get('/employees')
}

export const shiftApi = {
  getAll:  () => api.get('/shifts'),
  create:  (data) => api.post('/shifts', data),
  getTypes: () => api.get('/shift-types')
}

export default api
