package com.example.ipcapplication.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.ipcapplication.bean.Book
import com.example.ipcapplication.IPC.Stub

class IPCBinderService : Service() {
    private val binder = IPCBinder()
    private val bookArray = ArrayList<Book>()

    private fun initBooks() {
        for (i: Int in 0 until 20) {
            bookArray.add(Book(('A' + i).toString(), i))
        }
    }

    override fun onCreate() {
        super.onCreate()
        initBooks()
    }

    private fun getAllBookS(): MutableList<Book> {
        Log.d(TAG, "getAllBookS 获取书籍成功 -> $bookArray")
        return bookArray
    }

    private fun addBookS(book: Book?) {
        if (book != null) {
            bookArray.add(book)
            Log.d(TAG, "addBookS 添加书籍成功 -> $book")
        } else {
            Log.d(TAG, "addBookS 书籍不能为空")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class IPCBinder : Stub() {
        override fun getAllBook(): MutableList<Book> {
            return getAllBookS()
        }

        override fun addBook(book: Book?) {
            addBookS(book)
        }
    }

    companion object {
        private val TAG = "IPCBinderService"
    }
}
