import request from './request'

export const getBorrows = (params?: any) => request.get('/borrows', { params })
export const borrowBook = (data: any) => request.post('/borrows', data)
export const returnBook = (barcode: string) => request.put(`/borrows/return/${barcode}`)

export const getMyBorrows = (params?: any) => request.get('/my/borrows', { params })
export const renewBook = (id: number) => request.put(`/my/borrows/${id}/renew`)
