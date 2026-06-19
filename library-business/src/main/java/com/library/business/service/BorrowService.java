package com.library.business.service;

public interface BorrowService {

    Long borrowBook(String readerNo, String barcode);
}
