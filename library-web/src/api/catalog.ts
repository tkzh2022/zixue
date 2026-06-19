import request from './request'

export const searchCatalog = (params?: any) => request.get('/catalog/books', { params })
export const getCatalogBook = (id: number) => request.get(`/catalog/books/${id}`)
