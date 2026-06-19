import request from './request'

export const getFines = (params?: any) => request.get('/fines', { params })
export const payFine = (id: number, data: any) => request.put(`/fines/${id}/pay`, data)

export const getMyFines = (params?: any) => request.get('/my/fines', { params })
