package com.example.ipcapplication.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * 自定义数据类，实现Parcelable接口
 */
data class Book(val name: String, val price: Int) : Parcelable {

    //另一个构造函数，用于传输对象时进行反序列化
    constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readInt())

    //Parcelable的接口方法，用于对象的序列化
    //这个flags我看官方文档没看懂有什么用
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeString(name)
            writeInt(price)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    //下面这段代码是用于从Parcel生成Parcelable类的实例
    companion object CREATOR : Parcelable.Creator<Book> {
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }
}