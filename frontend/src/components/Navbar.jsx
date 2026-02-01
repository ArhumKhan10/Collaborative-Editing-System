/**
 * Navbar Component
 * 
 * Application-wide navigation bar with user menu and logout functionality.
 * Displays current user information, invitation badge, and provides quick navigation.
 * Supports real-time dashboard updates when invitations are accepted.
 * 
 * @component
 * @param {Object} props - Component props
 * @param {Function} [props.onDocumentUpdate] - Optional callback to refresh documents when invitation accepted
 */

import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Box,
  ListItemIcon,
  ListItemText,
  Divider,
  alpha,
  Badge,
} from '@mui/material'
import { 
  AccountCircle, 
  Logout, 
  Dashboard as DashboardIcon,
  Description,
  Mail,
} from '@mui/icons-material'
import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import InvitationCenter from './InvitationCenter'
import documentService from '../services/documentService'

const Navbar = ({ onDocumentUpdate }) => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [anchorEl, setAnchorEl] = useState(null)
  const [invitationOpen, setInvitationOpen] = useState(false)
  const [invitationCount, setInvitationCount] = useState(0)

  useEffect(() => {
    if (user?.email) {
      loadInvitationCount()
      // Refresh count every 10 seconds for more responsive notifications
      const interval = setInterval(loadInvitationCount, 10000)
      return () => clearInterval(interval)
    }
  }, [user])

  const loadInvitationCount = async () => {
    try {
      const response = await documentService.getInvitationCount(user.email)
      if (response.success) {
        setInvitationCount(response.data || 0)
      }
    } catch (error) {
      console.error('Failed to load invitation count:', error)
    }
  }

  const handleMenu = (event) => {
    setAnchorEl(event.currentTarget)
  }

  const handleInvitationClick = () => {
    setInvitationOpen(true)
    // Refresh count immediately when opening
    loadInvitationCount()
  }

  const handleInvitationClose = () => {
    setInvitationOpen(false)
    loadInvitationCount() // Refresh count when closing
  }

  const handleInvitationAccepted = () => {
    loadInvitationCount()
    // Trigger dashboard refresh in real-time
    if (onDocumentUpdate) {
      onDocumentUpdate()
    }
  }

  const handleClose = () => {
    setAnchorEl(null)
  }

  const handleDashboard = () => {
    navigate('/dashboard')
    handleClose()
  }

  const handleLogout = () => {
    logout()
    handleClose()
  }

  const getInitials = (name) => {
    return name
      ? name
          .split(' ')
          .map((n) => n[0])
          .join('')
          .toUpperCase()
          .slice(0, 2)
      : 'U'
  }

  return (
    <AppBar
      position="sticky"
      elevation={0}
      sx={{
        background: 'rgba(255, 255, 255, 0.8)',
        backdropFilter: 'blur(10px)',
        borderBottom: '1px solid',
        borderColor: alpha('#6366f1', 0.1),
        boxShadow: '0 2px 12px rgba(99, 102, 241, 0.08)',
      }}
    >
      <Toolbar sx={{ justifyContent: 'space-between', py: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 2,
              background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 12px rgba(99, 102, 241, 0.3)',
            }}
          >
            <Description sx={{ color: 'white', fontSize: 24 }} />
          </Box>
          <Typography
            variant="h5"
            sx={{
              fontWeight: 800,
              background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              letterSpacing: '-0.5px',
            }}
          >
            CollabEdit
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button
            startIcon={<DashboardIcon />}
            onClick={handleDashboard}
            sx={{
              color: 'text.primary',
              fontWeight: 600,
              px: 2,
              '&:hover': {
                background: alpha('#6366f1', 0.1),
              },
            }}
          >
            Dashboard
          </Button>

          <IconButton
            size="large"
            onClick={handleInvitationClick}
            sx={{
              color: 'text.primary',
              '&:hover': {
                background: alpha('#6366f1', 0.1),
              },
            }}
          >
            <Badge 
              badgeContent={invitationCount} 
              color="error"
              sx={{
                '& .MuiBadge-badge': {
                  background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
                  boxShadow: '0 2px 8px rgba(239, 68, 68, 0.4)',
                },
              }}
            >
              <Mail />
            </Badge>
          </IconButton>

          <IconButton
            size="large"
            onClick={handleMenu}
            sx={{
              border: '2px solid',
              borderColor: alpha('#6366f1', 0.2),
              '&:hover': {
                borderColor: alpha('#6366f1', 0.4),
                background: alpha('#6366f1', 0.05),
              },
            }}
          >
            <Avatar
              sx={{
                width: 36,
                height: 36,
                background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                fontSize: '1rem',
                fontWeight: 700,
              }}
            >
              {getInitials(user?.username)}
            </Avatar>
          </IconButton>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleClose}
            PaperProps={{
              elevation: 0,
              sx: {
                mt: 1.5,
                minWidth: 240,
                borderRadius: 2,
                border: '1px solid',
                borderColor: alpha('#6366f1', 0.1),
                boxShadow: '0 8px 32px rgba(99, 102, 241, 0.15)',
                '& .MuiMenuItem-root': {
                  px: 2,
                  py: 1.5,
                  borderRadius: 1,
                  mx: 1,
                  my: 0.5,
                  '&:hover': {
                    background: alpha('#6366f1', 0.08),
                  },
                },
              },
            }}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            <Box sx={{ px: 2, py: 2 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                {user?.username}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                {user?.email}
              </Typography>
            </Box>
            <Divider sx={{ my: 1 }} />
            <MenuItem onClick={handleDashboard}>
              <ListItemIcon>
                <DashboardIcon fontSize="small" sx={{ color: '#6366f1' }} />
              </ListItemIcon>
              <ListItemText>Dashboard</ListItemText>
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <Logout fontSize="small" sx={{ color: '#ef4444' }} />
              </ListItemIcon>
              <ListItemText sx={{ color: '#ef4444' }}>Logout</ListItemText>
            </MenuItem>
          </Menu>

          <InvitationCenter
            open={invitationOpen}
            onClose={handleInvitationClose}
            userEmail={user?.email}
            userId={user?.userId}
            onInvitationAccepted={handleInvitationAccepted}
          />
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default Navbar
