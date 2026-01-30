/**
 * Authentication Service
 * 
 * Handles all user authentication and profile management API calls.
 * Includes login, registration, and profile operations.
 * 
 * @module authService
 */

import api from './api'

const authService = {
  async register(username, email, password) {
    const response = await api.post('/api/users/register', {
      username,
      email,
      password,
    })
    return response.data
  },

  async login(email, password) {
    const response = await api.post('/api/users/login', {
      email,
      password,
    })
    return response.data
  },

  async getUserProfile(userId) {
    const response = await api.get(`/api/users/profile/${userId}`)
    return response.data
  },

  async updateProfile(userId, data) {
    const response = await api.put(`/api/users/profile/${userId}`, data)
    return response.data
  },
}

export default authService
