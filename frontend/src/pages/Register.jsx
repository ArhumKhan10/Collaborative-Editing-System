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
  Avatar,
  Stack,
  Divider,
} from '@mui/material'
import { PersonAddOutlined, EmailOutlined, LockOutlined, PersonOutlined } from '@mui/icons-material'
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
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%), radial-gradient(circle at 80% 80%, rgba(255,255,255,0.1) 0%, transparent 50%)',
          pointerEvents: 'none',
        },
      }}
    >
      <Container component="main" maxWidth="sm">
        <Paper
          elevation={24}
          sx={{
            p: 5,
            borderRadius: 4,
            background: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(10px)',
            boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
            position: 'relative',
            zIndex: 1,
          }}
        >
          <Stack spacing={3} alignItems="center">
            <Avatar
              sx={{
                width: 80,
                height: 80,
                background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                boxShadow: '0 8px 24px rgba(240, 147, 251, 0.4)',
              }}
            >
              <PersonAddOutlined sx={{ fontSize: 40 }} />
            </Avatar>

            <Box textAlign="center">
              <Typography
                variant="h3"
                sx={{
                  background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                  backgroundClip: 'text',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  fontWeight: 800,
                  mb: 1,
                }}
              >
                Create Account
              </Typography>
              <Typography variant="body1" color="text.secondary">
                Join CollabEdit and start collaborating
              </Typography>
            </Box>

            {error && (
              <Alert 
                severity="error" 
                sx={{ 
                  width: '100%',
                  borderRadius: 2,
                  animation: 'slideIn 0.3s ease-out',
                  '@keyframes slideIn': {
                    from: { opacity: 0, transform: 'translateY(-10px)' },
                    to: { opacity: 1, transform: 'translateY(0)' },
                  },
                }}
              >
                {error}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
              <Stack spacing={2.5}>
                <TextField
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
                  InputProps={{
                    startAdornment: <PersonOutlined sx={{ mr: 1, color: 'action.active' }} />,
                  }}
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      '&:hover fieldset': {
                        borderColor: '#f093fb',
                      },
                      '&.Mui-focused fieldset': {
                        borderColor: '#f093fb',
                        borderWidth: 2,
                      },
                    },
                  }}
                />

                <TextField
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
                  InputProps={{
                    startAdornment: <EmailOutlined sx={{ mr: 1, color: 'action.active' }} />,
                  }}
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      '&:hover fieldset': {
                        borderColor: '#f093fb',
                      },
                      '&.Mui-focused fieldset': {
                        borderColor: '#f093fb',
                        borderWidth: 2,
                      },
                    },
                  }}
                />

                <TextField
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
                  InputProps={{
                    startAdornment: <LockOutlined sx={{ mr: 1, color: 'action.active' }} />,
                  }}
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      '&:hover fieldset': {
                        borderColor: '#f093fb',
                      },
                      '&.Mui-focused fieldset': {
                        borderColor: '#f093fb',
                        borderWidth: 2,
                      },
                    },
                  }}
                />

                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  disabled={loading}
                  startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <PersonAddOutlined />}
                  sx={{
                    py: 1.5,
                    fontSize: '1.1rem',
                    mt: 1,
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                    '&:hover': {
                      background: 'linear-gradient(135deg, #e082ea 0%, #e4465b 100%)',
                      transform: 'translateY(-2px)',
                      boxShadow: '0 8px 24px rgba(240, 147, 251, 0.4)',
                    },
                    transition: 'all 0.3s ease',
                  }}
                >
                  {loading ? 'Creating Account...' : 'Sign Up'}
                </Button>
              </Stack>
            </Box>

            <Divider sx={{ width: '100%', my: 2 }} />

            <Typography variant="body2" color="text.secondary">
              Already have an account?{' '}
              <Link
                to="/login"
                style={{
                  textDecoration: 'none',
                  color: '#f093fb',
                  fontWeight: 600,
                  transition: 'color 0.2s',
                }}
                onMouseEnter={(e) => e.target.style.color = '#f5576c'}
                onMouseLeave={(e) => e.target.style.color = '#f093fb'}
              >
                Sign In
              </Link>
            </Typography>
          </Stack>
        </Paper>
      </Container>
    </Box>
  )
}

export default Register
