package com.example.ipcapplication.IPC

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import com.example.ipcapplication.bean.Book
import com.example.ipcapplication.IPC.BookManager.Companion.DESCRIPTOR

abstract class Stub : Binder(), BookManager {
    init {
        this.attachInterface(this, DESCRIPTOR)
    }

    override fun asBinder(): IBinder {
        return this
    }

    @Throws(RemoteException::class)
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        reply?.apply {
            when (code) {
                IBinder.INTERFACE_TRANSACTION -> {
                    reply.writeString(DESCRIPTOR)
                    return true
                }
                TRANSACTION_getAllBook -> {
                    data.enforceInterface(DESCRIPTOR)
                    val result = getAllBook()
                    Log.d(TAG,"result -> $result")
                    reply.writeNoException()
                    reply.writeTypedList(result)
                    return true
                }
                TRANSACTION_addBook -> {
                    data.enforceInterface(DESCRIPTOR)
                    val book: Book?
                    if (0 != data.readInt()) {
                        book = Book.createFromParcel(data)
                    } else {
                        book = null
                    }
                    addBook(book)
                    reply.writeNoException()
                    return true
                }
            }
        }
        return super.onTransact(code, data, reply, flags)
    }

    companion object {
        private val TAG = Stub::class.java.name
        const val TRANSACTION_getAllBook = IBinder.FIRST_CALL_TRANSACTION
        const val TRANSACTION_addBook = IBinder.FIRST_CALL_TRANSACTION + 1


        fun asInterface(binder: IBinder?): BookManager? {
            if (binder == null) {
                return null
            }
            val iin = binder.queryLocalInterface(DESCRIPTOR)
            if (iin != null && iin is BookManager) {
                return iin
            }
            return Proxy(binder)
        }
    }
}