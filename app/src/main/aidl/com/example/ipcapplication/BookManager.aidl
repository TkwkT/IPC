// BookManager.aidl
package com.example.ipcapplication;

import com.example.ipcapplication.bean.Book;

interface BookManager {

    List<Book> getAllBook();

    void addBookIn(in Book book);
}
