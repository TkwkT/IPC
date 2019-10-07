package com.example.ipcapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.ipcapplication.bean.Student
import kotlinx.android.synthetic.main.activity_ipc.*
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.lang.Exception

class IPCActivity : AppCompatActivity() {
    private lateinit var ipcText:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ipc)
        initView()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume")
        ipcByFile()
    }

    private fun ipcByFile(){
        val file = File(getExternalFilesDir(null), "text.txt")
        val objeInputStream:ObjectInputStream
        try{
            objeInputStream = ObjectInputStream(FileInputStream(file))
            val student = objeInputStream.readObject() as Student
            Log.d(TAG,student.toString())
            ipcText.text = student.toString()
            objeInputStream.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun initView(){
        ipcText = ipc_text
    }

    companion object{
        private val TAG = IPCActivity::class.java.name
    }
}
