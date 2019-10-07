package com.example.ipcapplication.IPC

import android.os.IInterface
import com.example.ipcapplication.bean.Book

/**
 * 实现IInterface接口
 */
interface BookManager : IInterface {

    fun getAllBook(): List<Book>
    fun addBook(book: Book?)

    companion object {
        const val DESCRIPTOR = "com.example.ipcapplication.IPC.BookManager"
    }
}