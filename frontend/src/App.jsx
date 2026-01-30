/**
 * App Component
 * 
 * Root application component that configures routing and theme.
 * Defines all application routes with authentication protection.
 * 
 * @component
 */

import { Routes, Route, Navigate } from 'react-router-dom'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import DocumentEditor from './pages/DocumentEditor'
import PrivateRoute from './components/PrivateRoute'

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
})

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={
          <PrivateRoute>
            <Dashboard />
          </PrivateRoute>
        } />
        <Route path="/document/:documentId" element={
          <PrivateRoute>
            <DocumentEditor />
          </PrivateRoute>
        } />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </ThemeProvider>
  )
}

export default App