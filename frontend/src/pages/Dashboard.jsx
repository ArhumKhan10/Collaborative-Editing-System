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
} from '@mui/material'
import { Add, Description, Delete } from '@mui/icons-material'
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

  return (
    <>
      <Navbar />
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
          <Typography variant="h4">My Documents</Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setOpenDialog(true)}
          >
            New Document
          </Button>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <CircularProgress />
          </Box>
        ) : documents.length === 0 ? (
          <Box sx={{ textAlign: 'center', mt: 4 }}>
            <Typography variant="h6" color="text.secondary">
              No documents yet. Create your first document!
            </Typography>
          </Box>
        ) : (
          <Grid container spacing={3}>
            {documents.map((doc) => (
              <Grid item xs={12} sm={6} md={4} key={doc.id}>
                <Card 
                  sx={{ 
                    cursor: 'pointer',
                    '&:hover': { boxShadow: 6 }
                  }}
                  onClick={() => handleOpenDocument(doc.id)}
                >
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Description sx={{ mr: 1, color: 'primary.main' }} />
                      <Typography variant="h6" noWrap>
                        {doc.title}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary" noWrap>
                      {doc.content || 'Empty document'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
                      Last modified: {new Date(doc.updatedAt).toLocaleDateString()}
                    </Typography>
                  </CardContent>
                  <CardActions>
                    <Button size="small" onClick={(e) => {
                      e.stopPropagation()
                      handleOpenDocument(doc.id)
                    }}>
                      Open
                    </Button>
                    {doc.ownerId === user.userId && (
                      <IconButton 
                        size="small" 
                        color="error"
                        onClick={(e) => handleDeleteDocument(doc.id, e)}
                      >
                        <Delete />
                      </IconButton>
                    )}
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}

        {/* Create Document Dialog */}
        <Dialog open={openDialog} onClose={() => setOpenDialog(false)}>
          <DialogTitle>Create New Document</DialogTitle>
          <DialogContent>
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
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
            <Button onClick={handleCreateDocument} variant="contained">
              Create
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </>
  )
}

export default Dashboard
