/**
 * InvitationCenter Component
 * 
 * Displays pending document invitations for the user.
 * Allows users to accept or decline collaboration invitations.
 * Shows invitation details including sender, document, and permission level.
 * 
 * @component
 * @param {Object} props - Component props
 * @param {boolean} props.open - Whether the dialog is open
 * @param {Function} props.onClose - Callback when dialog closes
 * @param {string} props.userEmail - Current user's email address
 * @param {string} props.userId - Current user's ID
 * @param {Function} props.onInvitationAccepted - Callback when invitation is accepted
 */

import { useState, useEffect } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Button,
  Box,
  Typography,
  Chip,
  Divider,
  CircularProgress,
  alpha,
} from '@mui/material'
import { Mail, Check, Close, Person, Description } from '@mui/icons-material'
import documentService from '../services/documentService'
import { toast } from 'react-toastify'

const InvitationCenter = ({ open, onClose, userEmail, userId, onInvitationAccepted }) => {
  const [invitations, setInvitations] = useState([])
  const [loading, setLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(null)

  useEffect(() => {
    if (open && userEmail) {
      loadInvitations()
    }
  }, [open, userEmail])

  const loadInvitations = async () => {
    try {
      setLoading(true)
      const response = await documentService.getPendingInvitations(userEmail)
      if (response.success) {
        setInvitations(response.data || [])
      }
    } catch (error) {
      console.error('Failed to load invitations:', error)
      toast.error('Failed to load invitations')
    } finally {
      setLoading(false)
    }
  }

  const handleAccept = async (invitation) => {
    try {
      setActionLoading(invitation.id)
      const response = await documentService.acceptInvitation(invitation.id, userEmail, userId)
      if (response.success) {
        toast.success(`Document "${invitation.documentTitle}" added to your dashboard!`)
        setInvitations(prev => prev.filter(inv => inv.id !== invitation.id))
        if (onInvitationAccepted) {
          onInvitationAccepted()
        }
      }
    } catch (error) {
      console.error('Failed to accept invitation:', error)
      toast.error(error.response?.data?.message || 'Failed to accept invitation')
    } finally {
      setActionLoading(null)
    }
  }

  const handleDecline = async (invitation) => {
    try {
      setActionLoading(invitation.id)
      const response = await documentService.declineInvitation(invitation.id, userEmail)
      if (response.success) {
        toast.info(`Declined invitation to "${invitation.documentTitle}"`)
        setInvitations(prev => prev.filter(inv => inv.id !== invitation.id))
      }
    } catch (error) {
      console.error('Failed to decline invitation:', error)
      toast.error('Failed to decline invitation')
    } finally {
      setActionLoading(null)
    }
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffInHours = Math.floor((now - date) / (1000 * 60 * 60))
    
    if (diffInHours < 1) return 'Just now'
    if (diffInHours < 24) return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`
    if (diffInHours < 48) return 'Yesterday'
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
  }

  return (
    <Dialog 
      open={open} 
      onClose={onClose} 
      maxWidth="sm" 
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 3,
          boxShadow: '0 20px 60px rgba(0,0,0,0.2)',
        },
      }}
    >
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1.5}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 2,
              background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Mail sx={{ color: 'white' }} />
          </Box>
          <Box flexGrow={1}>
            <Typography variant="h6" fontWeight="700">
              Invitations
            </Typography>
            {invitations.length > 0 && (
              <Typography variant="caption" color="text.secondary">
                {invitations.length} pending
              </Typography>
            )}
          </Box>
        </Box>
      </DialogTitle>
      
      <DialogContent sx={{ px: 0 }}>
        {loading ? (
          <Box py={8} textAlign="center">
            <CircularProgress />
          </Box>
        ) : invitations.length === 0 ? (
          <Box py={8} px={3} textAlign="center">
            <Mail sx={{ fontSize: 80, color: alpha('#6366f1', 0.2), mb: 2 }} />
            <Typography variant="h6" color="text.secondary" gutterBottom>
              No pending invitations
            </Typography>
            <Typography variant="body2" color="text.secondary">
              You're all caught up!
            </Typography>
          </Box>
        ) : (
          <List sx={{ py: 0 }}>
            {invitations.map((invitation, index) => (
              <Box key={invitation.id}>
                <ListItem
                  alignItems="flex-start"
                  sx={{
                    px: 3,
                    py: 2.5,
                    '&:hover': {
                      bgcolor: alpha('#6366f1', 0.02),
                    },
                  }}
                >
                  <ListItemAvatar>
                    <Avatar
                      sx={{
                        bgcolor: alpha('#6366f1', 0.1),
                        width: 48,
                        height: 48,
                      }}
                    >
                      <Description sx={{ color: '#6366f1' }} />
                    </Avatar>
                  </ListItemAvatar>
                  
                  <ListItemText
                    primary={
                      <Typography variant="subtitle1" fontWeight="600" sx={{ mb: 0.5 }}>
                        {invitation.documentTitle}
                      </Typography>
                    }
                    secondary={
                      <Box>
                        <Box display="flex" alignItems="center" gap={1} mb={1}>
                          <Person sx={{ fontSize: 16, color: 'text.secondary' }} />
                          <Typography variant="body2" color="text.secondary">
                            {invitation.invitedBy?.username || invitation.invitedBy?.email} invited you
                          </Typography>
                        </Box>
                        
                        <Box display="flex" alignItems="center" gap={1} flexWrap="wrap">
                          <Chip
                            label={invitation.permission?.toUpperCase() || 'EDIT'}
                            size="small"
                            sx={{
                              height: 24,
                              fontSize: '0.75rem',
                              fontWeight: 600,
                              background: invitation.permission === 'edit' 
                                ? alpha('#10b981', 0.1) 
                                : alpha('#3b82f6', 0.1),
                              color: invitation.permission === 'edit' ? '#10b981' : '#3b82f6',
                            }}
                          />
                          <Typography variant="caption" color="text.secondary">
                            {formatDate(invitation.invitedAt)}
                          </Typography>
                        </Box>
                      </Box>
                    }
                  />
                </ListItem>
                
                <Box display="flex" gap={1.5} px={3} pb={2.5}>
                  <Button
                    variant="contained"
                    startIcon={actionLoading === invitation.id ? <CircularProgress size={16} color="inherit" /> : <Check />}
                    onClick={() => handleAccept(invitation)}
                    disabled={actionLoading === invitation.id}
                    size="small"
                    sx={{
                      px: 2.5,
                      background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                      '&:hover': {
                        background: 'linear-gradient(135deg, #059669 0%, #047857 100%)',
                      },
                    }}
                  >
                    Accept
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={actionLoading === invitation.id ? null : <Close />}
                    onClick={() => handleDecline(invitation)}
                    disabled={actionLoading === invitation.id}
                    size="small"
                    sx={{
                      px: 2.5,
                      borderColor: alpha('#ef4444', 0.3),
                      color: '#ef4444',
                      '&:hover': {
                        borderColor: '#ef4444',
                        background: alpha('#ef4444', 0.05),
                      },
                    }}
                  >
                    Decline
                  </Button>
                </Box>
                
                {index < invitations.length - 1 && <Divider />}
              </Box>
            ))}
          </List>
        )}
      </DialogContent>
    </Dialog>
  )
}

export default InvitationCenter
