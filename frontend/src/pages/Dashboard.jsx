/**
 * Dashboard Component
 * 
 * Main landing page displaying user's accessible documents.
 * Provides functionality to create, view, and delete documents.
 * Shows documents owned by the user and documents shared with them.
 * 
 * @component
 */

import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Container,
  Grid,
  Card,
  CardContent,
  CardActions,
  Typography,
  Button,
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  IconButton,
  Chip,
  alpha,
  Fade,
  Grow,
} from '@mui/material'
import { 
  Add, 
  Description, 
  Delete, 
  FolderOpen,
  Schedule,
  Person,
  Edit,
} from '@mui/icons-material'
import Navbar from '../components/Navbar'
import { useAuth } from '../context/AuthContext'
import documentService from '../services/documentService'
import { toast } from 'react-toastify'

const Dashboard = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [documents, setDocuments] = useState([])
  const [loading, setLoading] = useState(true)
  const [openDialog, setOpenDialog] = useState(false)
  const [newDocTitle, setNewDocTitle] = useState('')

  useEffect(() => {
    loadDocuments()
  }, [])

  // Strip HTML tags from content for preview
  const stripHtml = (html) => {
    const tmp = document.createElement('DIV')
    tmp.innerHTML = html
    return tmp.textContent || tmp.innerText || ''
  }

  const loadDocuments = async () => {
    try {
      setLoading(true)
      const response = await documentService.getAllDocuments(user.userId)
      if (response.success) {
        setDocuments(response.data)
      }
    } catch (error) {
      console.error('Failed to load documents:', error)
      toast.error('Failed to load documents')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateDocument = async () => {
    if (!newDocTitle.trim()) {
      toast.error('Please enter a document title')
      return
    }

    try {
      const response = await documentService.createDocument(
        newDocTitle,
        '',
        user.userId
      )
      if (response.success) {
        toast.success('Document created successfully')
        setOpenDialog(false)
        setNewDocTitle('')
        loadDocuments()
      }
    } catch (error) {
      console.error('Failed to create document:', error)
      toast.error('Failed to create document')
    }
  }

  const handleOpenDocument = (documentId) => {
    navigate(`/document/${documentId}`)
  }

  const handleDeleteDocument = async (documentId, e) => {
    e.stopPropagation()
    if (window.confirm('Are you sure you want to delete this document?')) {
      try {
        const response = await documentService.deleteDocument(documentId, user.userId)
        if (response.success) {
          toast.success('Document deleted successfully')
          loadDocuments()
        }
      } catch (error) {
        console.error('Failed to delete document:', error)
        toast.error('Failed to delete document')
      }
    }
  }

  const EmptyState = () => (
    <Fade in timeout={800}>
      <Box
        sx={{
          textAlign: 'center',
          py: 12,
          px: 3,
        }}
      >
        <FolderOpen
          sx={{
            fontSize: 120,
            color: alpha('#6366f1', 0.2),
            mb: 3,
          }}
        />
        <Typography
          variant="h4"
          gutterBottom
          sx={{
            color: 'text.secondary',
            fontWeight: 600,
          }}
        >
          No documents yet
        </Typography>
        <Typography
          variant="body1"
          color="text.secondary"
          sx={{ mb: 4, maxWidth: 500, mx: 'auto' }}
        >
          Start your collaborative journey by creating your first document
        </Typography>
        <Button
          variant="contained"
          size="large"
          startIcon={<Add />}
          onClick={() => setOpenDialog(true)}
          sx={{
            px: 4,
            py: 1.5,
            fontSize: '1.1rem',
          }}
        >
          Create Document
        </Button>
      </Box>
    </Fade>
  )

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Navbar onDocumentUpdate={loadDocuments} />
      <Container maxWidth="xl" sx={{ py: 5 }}>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 5,
            flexWrap: 'wrap',
            gap: 2,
          }}
        >
          <Box>
            <Typography
              variant="h3"
              sx={{
                fontWeight: 800,
                background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                backgroundClip: 'text',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                mb: 1,
              }}
            >
              My Documents
            </Typography>
            <Typography variant="body1" color="text.secondary">
              {documents.length} {documents.length === 1 ? 'document' : 'documents'} available
            </Typography>
          </Box>

          <Button
            variant="contained"
            size="large"
            startIcon={<Add />}
            onClick={() => setOpenDialog(true)}
            sx={{
              px: 4,
              py: 1.5,
              fontSize: '1.05rem',
              boxShadow: '0 4px 14px rgba(99, 102, 241, 0.4)',
            }}
          >
            New Document
          </Button>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 12 }}>
            <CircularProgress size={60} thickness={4} />
          </Box>
        ) : documents.length === 0 ? (
          <EmptyState />
        ) : (
          <Grid container spacing={3}>
            {documents.map((doc, index) => (
              <Grow
                in
                key={doc.id}
                timeout={300 + index * 100}
                style={{ transformOrigin: '0 0 0' }}
              >
                <Grid item xs={12} sm={6} md={4} lg={3}>
                  <Card
                    sx={{
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      cursor: 'pointer',
                      position: 'relative',
                      overflow: 'visible',
                      background: 'linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)',
                      border: '1px solid',
                      borderColor: alpha('#6366f1', 0.1),
                      '&:hover': {
                        transform: 'translateY(-8px)',
                        boxShadow: `0 12px 40px ${alpha('#6366f1', 0.15)}`,
                        borderColor: alpha('#6366f1', 0.3),
                      },
                      '&:hover .action-buttons': {
                        opacity: 1,
                      },
                    }}
                    onClick={() => handleOpenDocument(doc.id)}
                  >
                    <Box
                      sx={{
                        height: 8,
                        background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                        borderRadius: '16px 16px 0 0',
                      }}
                    />

                    <CardContent sx={{ flexGrow: 1, pt: 3 }}>
                      <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                        <Box
                          sx={{
                            width: 48,
                            height: 48,
                            borderRadius: 2,
                            background: alpha('#6366f1', 0.1),
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            mr: 2,
                            flexShrink: 0,
                          }}
                        >
                          <Description sx={{ color: '#6366f1', fontSize: 28 }} />
                        </Box>
                        <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                          <Typography
                            variant="h6"
                            sx={{
                              fontWeight: 700,
                              mb: 0.5,
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            {doc.title}
                          </Typography>
                          <Chip
                            label={doc.ownerId === user.userId ? 'Owner' : 'Shared'}
                            size="small"
                            icon={doc.ownerId === user.userId ? <Person /> : null}
                            sx={{
                              height: 22,
                              fontSize: '0.75rem',
                              background: doc.ownerId === user.userId
                                ? alpha('#6366f1', 0.1)
                                : alpha('#10b981', 0.1),
                              color: doc.ownerId === user.userId ? '#6366f1' : '#10b981',
                              fontWeight: 600,
                            }}
                          />
                        </Box>
                      </Box>

                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                          mb: 2,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                          minHeight: '2.5em',
                        }}
                      >
                        {stripHtml(doc.content || 'No content yet')}
                      </Typography>

                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <Schedule sx={{ fontSize: 16, color: 'text.disabled' }} />
                        <Typography variant="caption" color="text.secondary">
                          {new Date(doc.updatedAt).toLocaleDateString('en-US', {
                            month: 'short',
                            day: 'numeric',
                            year: 'numeric',
                          })}
                        </Typography>
                      </Box>
                    </CardContent>

                    <CardActions
                      className="action-buttons"
                      sx={{
                        justifyContent: 'space-between',
                        px: 2,
                        pb: 2,
                        opacity: 0.7,
                        transition: 'opacity 0.2s',
                      }}
                    >
                      <Button
                        size="small"
                        startIcon={<Edit />}
                        onClick={(e) => {
                          e.stopPropagation()
                          handleOpenDocument(doc.id)
                        }}
                        sx={{
                          color: '#6366f1',
                          fontWeight: 600,
                          '&:hover': {
                            background: alpha('#6366f1', 0.1),
                          },
                        }}
                      >
                        Open
                      </Button>
                      {doc.ownerId === user.userId && (
                        <IconButton
                          size="small"
                          onClick={(e) => handleDeleteDocument(doc.id, e)}
                          sx={{
                            color: '#ef4444',
                            '&:hover': {
                              background: alpha('#ef4444', 0.1),
                            },
                          }}
                        >
                          <Delete fontSize="small" />
                        </IconButton>
                      )}
                    </CardActions>
                  </Card>
                </Grid>
              </Grow>
            ))}
          </Grid>
        )}

        {/* Create Document Dialog */}
        <Dialog
          open={openDialog}
          onClose={() => setOpenDialog(false)}
          maxWidth="sm"
          fullWidth
          PaperProps={{
            sx: {
              borderRadius: 3,
              boxShadow: '0 20px 60px rgba(0,0,0,0.2)',
            },
          }}
        >
          <DialogTitle
            sx={{
              fontSize: '1.5rem',
              fontWeight: 700,
              background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            Create New Document
          </DialogTitle>
          <DialogContent sx={{ pt: 3 }}>
            <TextField
              autoFocus
              margin="dense"
              label="Document Title"
              type="text"
              fullWidth
              variant="outlined"
              value={newDocTitle}
              onChange={(e) => setNewDocTitle(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  handleCreateDocument()
                }
              }}
              sx={{
                '& .MuiOutlinedInput-root': {
                  '&:hover fieldset': {
                    borderColor: '#6366f1',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#6366f1',
                    borderWidth: 2,
                  },
                },
              }}
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button
              onClick={() => setOpenDialog(false)}
              sx={{ color: 'text.secondary' }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleCreateDocument}
              variant="contained"
              sx={{
                px: 3,
              }}
            >
              Create
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  )
}

export default Dashboard
