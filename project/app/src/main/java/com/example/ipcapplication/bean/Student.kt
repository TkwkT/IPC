package com.example.ipcapplication.bean

import java.io.Serializable

data class Student(val name: String, val age: Int) : Serializable {
    companion object {
        private val serialVersionUID = 1L
    }
}