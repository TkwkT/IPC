package com.example.ipcapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.example.ipcapplication.IPC.BookManager
import com.example.ipcapplication.IPC.Stub
import com.example.ipcapplication.bean.Book
import com.example.ipcapplication.bean.Student
import com.example.ipcapplication.service.IPCAIDLService
import com.example.ipcapplication.service.IPCBinderService
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.name
    private lateinit var addBookButton: Button
    private lateinit var getBooksButton: Button
    private lateinit var changeButton: Button

    private var bookManager: BookManager? = null
    private var aidlBookManager: com.example.ipcapplication.BookManager? = null
    private lateinit var connection: MyConnection
    private var isAIDL = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initButton()
        initService()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        ipcByFile()
    }

    private fun ipcByFile() {
        val student = Student("张三", 21)
        val objectOutputStream: ObjectOutputStream
        try {
            val file = File(getExternalFilesDir(null), "text.txt")
            objectOutputStream = ObjectOutputStream(FileOutputStream(file))
            objectOutputStream.writeObject(student)
            objectOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initButton() {
        addBookButton = add_book_button
        getBooksButton = get_book_button
        changeButton = change_button
        addBookButton.setOnClickListener {
            bookManager?.addBook(Book("TEST", 20))
        }
        getBooksButton.setOnClickListener {
            val result = bookManager?.getAllBook()
            Log.d(TAG, "获得所有书籍 -> $result")
        }
        changeButton.setOnClickListener {
            val intent = Intent(this, IPCActivity::class.java)
            startActivity(intent)

        }
    }

    private fun initService(){
        if (isAIDL){
            initAIDLService()
        }else{
            initBinderService()
        }
    }

    private fun initBinderService() {
        connection = MyConnection()
        val intent = Intent(this, IPCBinderService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun initAIDLService() {
        connection = MyConnection()
        val intent = Intent(this, IPCAIDLService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }


    inner class MyConnection : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            if (isAIDL) {
                aidlBookManager = com.example.ipcapplication.BookManager.Stub.asInterface(p1)
            } else {
                bookManager = Stub.asInterface(p1)
            }
        }
    }
}
