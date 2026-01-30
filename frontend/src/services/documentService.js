/**
 * Document Service
 * 
 * Manages all document-related API operations including CRUD operations,
 * sharing functionality, and document access management.
 * 
 * @module documentService
 */

import api from './api'

const documentService = {
  async createDocument(title, content, ownerId) {
    const response = await api.post('/api/documents', {
      title,
      content,
      ownerId,
    })
    return response.data
  },

  async getDocument(documentId, userId) {
    const response = await api.get(`/api/documents/${documentId}`, {
      params: { userId },
    })
    return response.data
  },

  async updateDocument(documentId, data, userId) {
    const response = await api.put(`/api/documents/${documentId}`, data, {
      params: { userId },
    })
    return response.data
  },

  async shareDocument(documentId, userId, permission, ownerId) {
    const response = await api.post(
      `/api/documents/${documentId}/share`,
      { userId, permission },
      { params: { ownerId } }
    )
    return response.data
  },

  async deleteDocument(documentId, userId) {
    const response = await api.delete(`/api/documents/${documentId}`, {
      params: { userId },
    })
    return response.data
  },

  async getAllDocuments(userId) {
    const response = await api.get('/api/documents', {
      params: { userId },
    })
    return response.data
  },
}

export default documentService
