// services/auth.js
import axios from 'axios'

const API_URL = 'http://localhost:8080/api/v1/auth'

export const register = async (data, role) => {
  const body = { ...data, role }
  const response = await axios.post(`${API_URL}/register?role=${role}`, body)
  return response.data
}

export const login = async (data) => {
  const response = await axios.post(`${API_URL}/login`, data)
  return response.data
}
