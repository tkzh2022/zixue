import request from './request'

export const getAdminDashboard = () => request.get('/dashboard/admin')
export const getReaderDashboard = () => request.get('/dashboard/reader')
