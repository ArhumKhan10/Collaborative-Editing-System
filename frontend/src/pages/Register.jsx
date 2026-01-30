/**
 * Register Component
 * 
 * User registration page with username, email, and password fields.
 * Validates input and displays field-specific error messages.
 * Redirects to login page upon successful registration.
 * 
 * @component
 */

import { useState, useMemo } from 'react'
import { Link, Navigate } from 'react-router-dom'
import {
  Container,
  Box,
  TextField,
  Button,
  Typography,
  Paper,
  CircularProgress,
  Alert,
} from '@mui/material'
import { useAuth } from '../context/AuthContext'

const Register = () => {
  const { register, isAuthenticated } = useAuth()
  const [formData, setFormData] = useState({ username: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  
  const hasUsernameError = useMemo(() => 
    error && error.toLowerCase().includes('username'), 
    [error]
  )
  
  const hasEmailError = useMemo(() => 
    error && error.toLowerCase().includes('email'), 
    [error]
  )
  
  const hasPasswordError = useMemo(() => 
    error && error.toLowerCase().includes('password'), 
    [error]
  )

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    
    try {
      await register(formData.username, formData.email, formData.password)
      setError('')
    } catch (err) {
      const errorMessage = err.response?.data?.message 
        || err.message 
        || 'Registration failed. Please try again.'
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography component="h1" variant="h4" align="center" gutterBottom>
            Register
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" gutterBottom>
            Create a new account
          </Typography>
          
          {error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={formData.username}
              onChange={handleChange}
              error={!!hasUsernameError}
              helperText={hasUsernameError ? error : ''}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email Address"
              name="email"
              autoComplete="email"
              value={formData.email}
              onChange={handleChange}
              error={!!hasEmailError}
              helperText={hasEmailError ? error : ''}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="new-password"
              value={formData.password}
              onChange={handleChange}
              error={!!hasPasswordError}
              helperText={hasPasswordError ? error : 'Minimum 6 characters'}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Sign Up'}
            </Button>
            <Box sx={{ textAlign: 'center' }}>
              <Link to="/login" style={{ textDecoration: 'none' }}>
                <Typography variant="body2" color="primary">
                  Already have an account? Sign In
                </Typography>
              </Link>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  )
}

export default Register
