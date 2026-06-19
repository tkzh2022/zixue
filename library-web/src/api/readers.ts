import request from './request'

export const getReaders = (params?: any) => request.get('/readers', { params })
export const getReader = (id: number) => request.get(`/readers/${id}`)
export const createReader = (data: any) => request.post('/readers', data)
export const updateReader = (id: number, data: any) => request.put(`/readers/${id}`, data)
export const updateReaderStatus = (id: number, status: string) => request.put(`/readers/${id}/status`, { status })
