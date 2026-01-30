import { createContext, useState, useContext, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import authService from '../services/authService'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (token && storedUser) {
      setUser(JSON.parse(storedUser))
    }
    
    setLoading(false)
  }, [])

  const login = async (email, password) => {
    try {
      const response = await authService.login(email, password)
      
      if (response.success && response.data) {
        const { token, userId, username, email: userEmail } = response.data
        
        // Store token and user info
        localStorage.setItem('token', token)
        const userData = { userId, username, email: userEmail }
        localStorage.setItem('user', JSON.stringify(userData))
        
        setUser(userData)
        toast.success('Login successful!')
        navigate('/dashboard')
        return true
      } else {
        const errorMsg = response.message || 'Login failed'
        throw new Error(errorMsg)
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || 'Login failed'
      throw error
    }
  }

  const register = async (username, email, password) => {
    try {
      const response = await authService.register(username, email, password)
      
      if (response.success) {
        toast.success('Registration successful! Please login.')
        navigate('/login')
        return true
      } else {
        const errorMsg = response.message || 'Registration failed'
        throw new Error(errorMsg)
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || 'Registration failed'
      throw error
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
    toast.info('Logged out successfully')
    navigate('/login')
  }

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    isAuthenticated: !!user,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
