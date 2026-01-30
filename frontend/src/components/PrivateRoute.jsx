/**
 * PrivateRoute Component
 * 
 * Route wrapper that protects pages requiring authentication.
 * Redirects unauthenticated users to login page.
 * Displays loading indicator during authentication check.
 * 
 * @component
 */

import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { CircularProgress, Box } from '@mui/material'

const PrivateRoute = ({ children }) => {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
      >
        <CircularProgress />
      </Box>
    )
  }

  return user ? children : <Navigate to="/login" replace />
}

export default PrivateRoute
