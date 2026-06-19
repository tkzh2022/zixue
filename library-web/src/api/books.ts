import request from './request'

export const getBooks = (params?: any) => request.get('/books', { params })
export const getBook = (id: number) => request.get(`/books/${id}`)
export const createBook = (data: any) => request.post('/books', data)
export const updateBook = (id: number, data: any) => request.put(`/books/${id}`, data)
export const deleteBook = (id: number) => request.delete(`/books/${id}`)

export const addCopy = (bookId: number, data: any) => request.post(`/books/${bookId}/copies`, data)
export const deleteCopy = (copyId: number) => request.delete(`/books/copies/${copyId}`)
