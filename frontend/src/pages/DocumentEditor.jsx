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
} from '@mui/material'
import { ArrowBack, Save, History, People } from '@mui/icons-material'
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
  const [activeUsers, setActiveUsers] = useState(new Set())
  const [versionDrawerOpen, setVersionDrawerOpen] = useState(false)
  const [versions, setVersions] = useState([])
  const [saving, setSaving] = useState(false)

  const isTypingRef = useRef(false)
  const saveTimeoutRef = useRef(null)

  // WebSocket message handler
  const handleWebSocketMessage = useCallback((message) => {
    console.log('Received WebSocket message:', message)

    if (message.type === 'content-change') {
      // Only update if not currently typing
      if (!isTypingRef.current && message.userId !== user.userId) {
        setContent(message.content)
        toast.info(`${message.username} updated the document`, { autoClose: 2000 })
      }
    } else if (message.type === 'user-joined') {
      setActiveUsers(prev => new Set(prev).add(message.username))
      toast.success(`${message.username} joined the document`, { autoClose: 2000 })
    } else if (message.type === 'user-left') {
      setActiveUsers(prev => {
        const newSet = new Set(prev)
        newSet.delete(message.username)
        return newSet
      })
      toast.info(`${message.username} left the document`, { autoClose: 2000 })
    }
  }, [user.userId])

  // WebSocket connection
  const { connected, sendMessage } = useWebSocket(
    documentId,
    user.userId,
    user.username,
    handleWebSocketMessage
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
        setDocument(response.data)
        setContent(response.data.content || '')
        setTitle(response.data.title)
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
    setContent(value)
    isTypingRef.current = true

    // Debounce WebSocket send
    if (saveTimeoutRef.current) {
      clearTimeout(saveTimeoutRef.current)
    }

    saveTimeoutRef.current = setTimeout(() => {
      sendMessage(value)
      isTypingRef.current = false
    }, 500)
  }

  // Save document
  const handleSave = async () => {
    try {
      setSaving(true)
      const response = await documentService.updateDocument(
        documentId,
        { title, content, lastModifiedBy: user.userId },
        user.userId
      )
      if (response.success) {
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
        const response = await versionService.revertToVersion(
          documentId,
          versionId,
          user.userId
        )
        if (response.success) {
          toast.success('Document reverted successfully')
          loadDocument()
          setVersionDrawerOpen(false)
        }
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
              label={`${activeUsers.size + 1} online`}
              color={connected ? 'success' : 'default'}
              size="small"
            />
            <Button
              startIcon={<History />}
              onClick={handleOpenVersionHistory}
            >
              Versions
            </Button>
            <Button
              variant="outlined"
              onClick={handleCreateVersion}
            >
              Save Version
            </Button>
            <Button
              variant="contained"
              startIcon={<Save />}
              onClick={handleSave}
              disabled={saving}
            >
              {saving ? 'Saving...' : 'Save'}
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
          sx={{ mb: 2 }}
          InputProps={{
            style: { fontSize: '1.5rem', fontWeight: 'bold' }
          }}
        />

        {/* Editor */}
        <Paper elevation={2} sx={{ p: 2 }}>
          <ReactQuill
            theme="snow"
            value={content}
            onChange={handleContentChange}
            modules={{
              toolbar: [
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
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
          <Typography variant="caption" color="text.secondary">
            {connected ? '🟢 Connected' : '🔴 Disconnected'}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Active users: {Array.from(activeUsers).join(', ') || 'Just you'}
          </Typography>
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
    </>
  )
}

export default DocumentEditor
