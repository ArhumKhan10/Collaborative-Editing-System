import { useState, useEffect, useCallback, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Container,
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  Chip,
  CircularProgress,
  Drawer,
  List,
  ListItem,
  ListItemText,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material'
import { ArrowBack, Save, History, People, Share } from '@mui/icons-material'
import ReactQuill from 'react-quill'
import Navbar from '../components/Navbar'
import { useAuth } from '../context/AuthContext'
import { useWebSocket } from '../hooks/useWebSocket'
import documentService from '../services/documentService'
import versionService from '../services/versionService'
import { toast } from 'react-toastify'

const DocumentEditor = () => {
  const { documentId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [document, setDocument] = useState(null)
  const [content, setContent] = useState('')
  const [title, setTitle] = useState('')
  const [loading, setLoading] = useState(true)
  const [activeEditors, setActiveEditors] = useState(new Set())
  const [activeViewers, setActiveViewers] = useState(new Set())
  const [userPermission, setUserPermission] = useState('edit') // 'edit' or 'view'
  const [versionDrawerOpen, setVersionDrawerOpen] = useState(false)
  const [versions, setVersions] = useState([])
  const [saving, setSaving] = useState(false)
  const [shareDialogOpen, setShareDialogOpen] = useState(false)
  const [shareEmail, setShareEmail] = useState('')
  const [sharePermission, setSharePermission] = useState('edit')
  const [canShare, setCanShare] = useState(false)
  const [lastSaved, setLastSaved] = useState(null)

  const isTypingRef = useRef(false)
  const saveTimeoutRef = useRef(null)
  const autoSaveTimeoutRef = useRef(null)

  // WebSocket message handler
  const handleWebSocketMessage = useCallback((message) => {
    console.log('Received WebSocket message:', message)

    if (message.type === 'content-change') {
      // Only update if not currently typing
      if (!isTypingRef.current && message.userId !== user.userId) {
        setContent(message.content)
        
        // Auto-add user to activeEditors if not already there (ensures sync)
        setActiveEditors(prev => {
          if (!prev.has(message.username)) {
            return new Set(prev).add(message.username)
          }
          return prev
        })
        
        toast.info(`${message.username} updated the document`, { autoClose: 2000 })
      }
    } else if (message.type === 'user-joined') {
      // Don't add yourself or show notification for yourself
      if (message.userId !== user.userId) {
        const permission = message.permission || 'edit'
        if (permission === 'edit') {
          setActiveEditors(prev => new Set(prev).add(message.username))
          toast.success(`${message.username} is editing`, { autoClose: 2000 })
        } else {
          setActiveViewers(prev => new Set(prev).add(message.username))
          toast.info(`${message.username} is viewing`, { autoClose: 2000 })
        }
      }
    } else if (message.type === 'user-left') {
      // Don't show notification for yourself
      if (message.userId !== user.userId) {
        setActiveEditors(prev => {
          const newSet = new Set(prev)
          newSet.delete(message.username)
          return newSet
        })
        setActiveViewers(prev => {
          const newSet = new Set(prev)
          newSet.delete(message.username)
          return newSet
        })
        toast.info(`${message.username} left the document`, { autoClose: 2000 })
      }
    }
  }, [user.userId, user.username])

  // WebSocket connection
  const { connected, sendMessage } = useWebSocket(
    documentId,
    user.userId,
    user.username,
    handleWebSocketMessage,
    userPermission
  )

  // Load document
  useEffect(() => {
    loadDocument()
  }, [documentId])

  const loadDocument = async () => {
    try {
      setLoading(true)
      const response = await documentService.getDocument(documentId, user.userId)
      if (response.success && response.data) {
        const doc = response.data
        setDocument(doc)
        setContent(doc.content || '')
        setTitle(doc.title)
        
        // Determine user's permission level
        const isOwner = doc.ownerId === user.userId
        const collaborator = doc.collaborators?.find(
          collab => collab.userId === user.userId
        )
        
        const hasEditPermission = isOwner || collaborator?.permission === 'edit'
        const hasViewPermission = collaborator?.permission === 'view'
        
        setUserPermission(hasEditPermission ? 'edit' : 'view')
        setCanShare(hasEditPermission)
      }
    } catch (error) {
      console.error('Failed to load document:', error)
      toast.error('Failed to load document')
      navigate('/dashboard')
    } finally {
      setLoading(false)
    }
  }

  // Handle content change
  const handleContentChange = (value) => {
    // Prevent editing if user only has view permission
    if (userPermission === 'view') {
      toast.warning('You have view-only access to this document')
      return
    }

    setContent(value)
    isTypingRef.current = true

    // Debounce WebSocket send (500ms for real-time updates)
    if (saveTimeoutRef.current) {
      clearTimeout(saveTimeoutRef.current)
    }

    saveTimeoutRef.current = setTimeout(() => {
      sendMessage(value)
      isTypingRef.current = false
    }, 500)

    // Auto-save to database (3 seconds after user stops typing)
    if (autoSaveTimeoutRef.current) {
      clearTimeout(autoSaveTimeoutRef.current)
    }

    autoSaveTimeoutRef.current = setTimeout(() => {
      handleAutoSave(value)
    }, 3000)
  }

  // Auto-save function
  const handleAutoSave = async (contentToSave) => {
    try {
      await documentService.updateDocument(
        documentId,
        { title, content: contentToSave, lastModifiedBy: user.userId },
        user.userId
      )
      setLastSaved(new Date())
      console.log('Auto-saved at', new Date().toLocaleTimeString())
    } catch (error) {
      console.error('Auto-save failed:', error)
      // Only show error if it's critical
      if (error.response?.status === 403 || error.response?.status === 404) {
        toast.error('Failed to save document')
      }
    }
  }

  // Cleanup timeouts on unmount
  useEffect(() => {
    return () => {
      if (saveTimeoutRef.current) {
        clearTimeout(saveTimeoutRef.current)
      }
      if (autoSaveTimeoutRef.current) {
        clearTimeout(autoSaveTimeoutRef.current)
      }
    }
  }, [])

  // Save document (manual save)
  const handleSave = async () => {
    try {
      setSaving(true)
      const response = await documentService.updateDocument(
        documentId,
        { title, content, lastModifiedBy: user.userId },
        user.userId
      )
      if (response.success) {
        setLastSaved(new Date())
        toast.success('Document saved successfully')
      }
    } catch (error) {
      console.error('Failed to save document:', error)
      toast.error('Failed to save document')
    } finally {
      setSaving(false)
    }
  }

  // Create version
  const handleCreateVersion = async () => {
    try {
      const description = prompt('Enter version description (optional):')
      const response = await versionService.createVersion(
        documentId,
        content,
        user.userId,
        description || 'Manual save'
      )
      if (response.success) {
        toast.success('Version created successfully')
      }
    } catch (error) {
      console.error('Failed to create version:', error)
      toast.error('Failed to create version')
    }
  }

  // Load version history
  const loadVersionHistory = async () => {
    try {
      const response = await versionService.getVersionHistory(documentId)
      if (response.success) {
        setVersions(response.data)
      }
    } catch (error) {
      console.error('Failed to load versions:', error)
      toast.error('Failed to load version history')
    }
  }

  // Revert to version
  const handleRevertToVersion = async (versionId) => {
    if (window.confirm('Are you sure you want to revert to this version?')) {
      try {
        // First, get the version content
        const versionResponse = await versionService.getVersion(versionId)
        if (!versionResponse.success) {
          toast.error('Failed to load version')
          return
        }

        const oldContent = versionResponse.data.content

        // Update the document with the old content
        await documentService.updateDocument(
          documentId,
          { content: oldContent, lastModifiedBy: user.userId },
          user.userId
        )

        // Create a version record
        await versionService.revertToVersion(
          documentId,
          versionId,
          user.userId
        )

        toast.success('Document reverted successfully')
        
        // Reload and update UI
        setContent(oldContent)
        loadDocument()
        setVersionDrawerOpen(false)
        
        // Broadcast the change to other users
        sendMessage(oldContent)
      } catch (error) {
        console.error('Failed to revert:', error)
        toast.error('Failed to revert to version')
      }
    }
  }

  // Open version drawer
  const handleOpenVersionHistory = () => {
    loadVersionHistory()
    setVersionDrawerOpen(true)
  }

  // Share document
  const handleShareDocument = async () => {
    if (!shareEmail.trim()) {
      toast.error('Please enter an email address')
      return
    }

    try {
      // First, get the user by email
      const userResponse = await fetch(`http://localhost:8080/api/users/exists/${encodeURIComponent(shareEmail)}`)
      if (!userResponse.ok) {
        toast.error('User not found with that email')
        return
      }
      const userData = await userResponse.json()
      
      if (!userData.success) {
        toast.error('User not found with that email')
        return
      }

      // Share the document
      const response = await documentService.shareDocument(
        documentId,
        userData.data.id,  // UserDTO has 'id', not 'userId'
        sharePermission,
        user.userId  // Current user's ID (backend checks if they have permission)
      )
      
      if (response.success) {
        toast.success(`Document shared with ${shareEmail}`)
        setShareDialogOpen(false)
        setShareEmail('')
        setSharePermission('edit')
      }
    } catch (error) {
      console.error('Failed to share document:', error)
      toast.error(error.response?.data?.message || 'Failed to share document')
    }
  }

  if (loading) {
    return (
      <>
        <Navbar />
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
          <CircularProgress />
        </Box>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <Container maxWidth="lg" sx={{ mt: 2, mb: 4 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Button
            startIcon={<ArrowBack />}
            onClick={() => navigate('/dashboard')}
          >
            Back
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            <Chip
              icon={<People />}
              label={`${activeEditors.size + activeViewers.size + 1} online`}
              color={connected ? 'success' : 'default'}
              size="small"
              title={
                activeEditors.size + activeViewers.size > 0 
                  ? `Editing: ${activeEditors.size + (userPermission === 'edit' ? 1 : 0)} | Viewing: ${activeViewers.size + (userPermission === 'view' ? 1 : 0)}`
                  : 'Just you'
              }
            />
            <Button
              startIcon={<Share />}
              onClick={() => setShareDialogOpen(true)}
              disabled={!canShare}
              title={canShare ? 'Share this document' : 'Only editors can share'}
            >
              Share
            </Button>
            <Button
              startIcon={<History />}
              onClick={handleOpenVersionHistory}
            >
              Versions
            </Button>
            <Button
              variant="outlined"
              onClick={handleCreateVersion}
              disabled={userPermission === 'view'}
              title={userPermission === 'view' ? 'View-only users cannot save versions' : 'Save a version snapshot'}
            >
              Save Version
            </Button>
            <Button
              variant="contained"
              startIcon={<Save />}
              onClick={handleSave}
              disabled={saving || userPermission === 'view'}
              title={userPermission === 'view' ? 'View-only access' : 'Force save now (auto-save is enabled)'}
            >
              {saving ? 'Saving...' : 'Save Now'}
            </Button>
          </Box>
        </Box>

        {/* Title */}
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Untitled Document"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          disabled={userPermission === 'view'}
          sx={{ mb: 2 }}
          InputProps={{
            style: { fontSize: '1.5rem', fontWeight: 'bold' }
          }}
        />

        {/* Editor */}
        <Paper elevation={2} sx={{ p: 2, position: 'relative' }}>
          {userPermission === 'view' && (
            <Box
              sx={{
                position: 'absolute',
                top: 8,
                right: 8,
                bgcolor: 'warning.main',
                color: 'white',
                px: 2,
                py: 0.5,
                borderRadius: 1,
                fontSize: '0.75rem',
                fontWeight: 'bold',
                zIndex: 1
              }}
            >
              VIEW ONLY
            </Box>
          )}
          <ReactQuill
            theme="snow"
            value={content}
            onChange={handleContentChange}
            readOnly={userPermission === 'view'}
            modules={{
              toolbar: userPermission === 'view' ? false : [
                [{ 'header': [1, 2, 3, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                [{ 'align': [] }],
                ['link', 'image'],
                ['clean']
              ],
            }}
            style={{ minHeight: '500px' }}
          />
        </Paper>

        {/* Status */}
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Typography variant="caption" color="text.secondary">
              {connected ? '🟢 Connected' : '🔴 Disconnected'}
            </Typography>
            {lastSaved && (
              <Typography variant="caption" color="text.secondary">
                💾 Auto-saved at {lastSaved.toLocaleTimeString()}
              </Typography>
            )}
          </Box>
          <Box sx={{ display: 'flex', gap: 2 }}>
            {activeEditors.size > 0 && (
              <Typography variant="caption" color="success.main" fontWeight="bold">
                ✏️ Editing: {Array.from(activeEditors).join(', ')}
              </Typography>
            )}
            {activeViewers.size > 0 && (
              <Typography variant="caption" color="text.secondary">
                👁️ Viewing: {Array.from(activeViewers).join(', ')}
              </Typography>
            )}
            {activeEditors.size === 0 && activeViewers.size === 0 && (
              <Typography variant="caption" color="text.secondary">
                {userPermission === 'edit' ? 'No other users' : 'No other users'}
              </Typography>
            )}
          </Box>
        </Box>
      </Container>

      {/* Version History Drawer */}
      <Drawer
        anchor="right"
        open={versionDrawerOpen}
        onClose={() => setVersionDrawerOpen(false)}
      >
        <Box sx={{ width: 350, p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Version History
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <List>
            {versions.map((version) => (
              <ListItem
                key={version.id}
                button
                onClick={() => handleRevertToVersion(version.id)}
              >
                <ListItemText
                  primary={version.description || 'No description'}
                  secondary={
                    <>
                      {new Date(version.timestamp).toLocaleString()}
                      <br />
                      Changes: +{version.changeStats.charsAdded} -{version.changeStats.charsDeleted}
                    </>
                  }
                />
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>

      {/* Share Document Dialog */}
      <Dialog open={shareDialogOpen} onClose={() => setShareDialogOpen(false)}>
        <DialogTitle>Share Document</DialogTitle>
        <DialogContent sx={{ minWidth: 400, pt: 2 }}>
          <TextField
            autoFocus
            margin="dense"
            label="User Email"
            type="email"
            fullWidth
            variant="outlined"
            value={shareEmail}
            onChange={(e) => setShareEmail(e.target.value)}
            placeholder="Enter email address"
            sx={{ mb: 2 }}
          />
          <FormControl fullWidth>
            <InputLabel>Permission</InputLabel>
            <Select
              value={sharePermission}
              label="Permission"
              onChange={(e) => setSharePermission(e.target.value)}
            >
              <MenuItem value="view">View Only</MenuItem>
              <MenuItem value="edit">Can Edit</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShareDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleShareDocument} variant="contained">
            Share
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default DocumentEditor
