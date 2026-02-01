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

  // ============ INVITATION METHODS ============

  async sendInvitation(documentId, ownerId, email, permission) {
    const response = await api.post(
      `/api/documents/${documentId}/invite`,
      { email, permission },
      { params: { ownerId } }
    )
    return response.data
  },

  async getPendingInvitations(userEmail) {
    const response = await api.get('/api/documents/invitations/pending', {
      params: { userEmail },
    })
    return response.data
  },

  async acceptInvitation(invitationId, userEmail, userId) {
    const response = await api.post(
      `/api/documents/invitations/${invitationId}/accept`,
      {},
      { params: { userEmail, userId } }
    )
    return response.data
  },

  async declineInvitation(invitationId, userEmail) {
    const response = await api.post(
      `/api/documents/invitations/${invitationId}/decline`,
      {},
      { params: { userEmail } }
    )
    return response.data
  },

  async cancelInvitation(invitationId, ownerId) {
    const response = await api.delete(
      `/api/documents/invitations/${invitationId}`,
      { params: { ownerId } }
    )
    return response.data
  },

  async getInvitationCount(userEmail) {
    const response = await api.get('/api/documents/invitations/count', {
      params: { userEmail },
    })
    return response.data
  },
}

export default documentService
