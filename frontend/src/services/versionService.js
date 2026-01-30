/**
 * Version Service
 * 
 * Manages document version control operations including creating snapshots,
 * retrieving version history, reverting to previous versions, and tracking contributions.
 * 
 * @module versionService
 */

import api from './api'

const versionService = {
  async createVersion(documentId, content, userId, description) {
    const response = await api.post(`/api/versions/${documentId}`, {
      content,
      userId,
      description,
    })
    return response.data
  },

  async getVersionHistory(documentId) {
    const response = await api.get(`/api/versions/${documentId}`)
    return response.data
  },

  async getVersion(versionId) {
    const response = await api.get(`/api/versions/version/${versionId}`)
    return response.data
  },

  async revertToVersion(documentId, versionId, userId) {
    const response = await api.post(
      `/api/versions/${documentId}/revert/${versionId}`,
      {},
      { params: { userId } }
    )
    return response.data
  },

  async getContributions(documentId) {
    const response = await api.get(`/api/versions/${documentId}/contributions`)
    return response.data
  },
}

export default versionService
