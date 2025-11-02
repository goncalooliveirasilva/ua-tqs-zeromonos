import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api/v1/bookings'

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    console.log('token missing')
    return {}
  }
  return { Authorization: `Bearer ${token}` }
}

export const getBookingByToken = async (token) => {
  const res = await axios.get(`${BASE_URL}/public/${token}`)
  return res.data
}

export const createBooking = async (bookingRequest) => {
  const res = await axios.post(BASE_URL, bookingRequest, {
    headers: getAuthHeaders(),
  })
  return res.data
}

export const getMyBookings = async () => {
  const res = await axios.get(`${BASE_URL}/me`, {
    headers: getAuthHeaders(),
  })
  return res.data
}

export const cancelBooking = async (id) => {
  const res = await axios.delete(`${BASE_URL}/${id}`, {
    headers: getAuthHeaders(),
  })
  return res.data
}

export const getAvailableTimes = async (municipality, date) => {
  const res = await axios.get(`${BASE_URL}/available-times`, {
    headers: getAuthHeaders(),
    params: { municipality, date },
  })
  return res.data
}

export const getAllBookings = async () => {
  const res = await axios.get(BASE_URL, {
    headers: getAuthHeaders(),
  })
  return res.data
}

export const getBookingDetails = async (id) => {
  const res = await axios.get(`${BASE_URL}/${id}`, {
    headers: getAuthHeaders(),
  })
  return res.data
}

export const updateBookingState = async (id, newState) => {
  const res = await axios.put(
    `${BASE_URL}/${id}/state`,
    { state: newState },
    { headers: getAuthHeaders() },
  )
  return res.data
}

export const getBookingHistory = async (id) => {
  const res = await axios.get(`${BASE_URL}/${id}/history`, {
    headers: getAuthHeaders(),
  })
  return res.data
}

export const getBookingsByMunicipality = async (municipality) => {
  const res = await axios.get(`${BASE_URL}/municipality/${municipality}`, {
    headers: getAuthHeaders(),
  })
  return res.data
}

export const getBookingsByDistrict = async (district) => {
  const res = await axios.get(`${BASE_URL}/district/${district}`, {
    headers: getAuthHeaders(),
  })
  return res.data
}
