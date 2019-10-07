package com.example.ipcapplication.IPC

import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import com.example.ipcapplication.bean.Book
import com.example.ipcapplication.IPC.BookManager.Companion.DESCRIPTOR
import java.lang.Exception

/**
 * 另一进程的代理类，通过Parcelable的反序列化，将进程传输过来的数据还原为数据类对象
 */
class Proxy(private val remote: IBinder) : BookManager {

    fun getInterfaceDescriptor(): String {
        return DESCRIPTOR
    }

    @Throws(RemoteException::class)
    override fun addBook(book: Book?) {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DESCRIPTOR)
            if (book != null) {
                data.writeInt(1)
                book.writeToParcel(data, 0)
            } else {
                data.writeInt(0)
            }
            remote.transact(Stub.TRANSACTION_addBook, data, reply, 0)
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    override fun getAllBook(): List<Book> {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        var result =  ArrayList<Book>()
        try {
            data.writeInterfaceToken(DESCRIPTOR)
            remote.transact(Stub.TRANSACTION_getAllBook, data, reply, 0)
            reply.readException()

            result = reply.createTypedArrayList(Book.CREATOR)!!
//            Log.d(TAG,reply.createTypedArrayList(Book.CREATOR).toString())
            Log.d(TAG, "result -> $result")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            data.recycle()
            reply.recycle()
        }

        return result
    }

    override fun asBinder(): IBinder {
        return remote
    }

    companion object {
        private val TAG = Proxy::class.java.name
    }
}