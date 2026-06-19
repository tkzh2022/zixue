import request from './request'

export const getRules = () => request.get('/rules')
export const updateRule = (id: number, data: any) => request.put(`/rules/${id}`, data)
