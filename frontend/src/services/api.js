import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only handle 401 for authenticated requests (when we have a token)
    // Don't redirect if we're already on login/register page or during login attempt
    const isLoginAttempt = error.config?.url?.includes('/login') || error.config?.url?.includes('/register')
    const currentPath = window.location.pathname
    const isOnAuthPage = currentPath === '/login' || currentPath === '/register'
    
    if (error.response?.status === 401 && !isLoginAttempt && !isOnAuthPage) {
      // Unauthorized - token expired or invalid, clear auth and redirect to login
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
