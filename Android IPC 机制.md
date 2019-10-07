## Android IPC 机制

###  一、跨进程通信方式

#### 1、Bundle

bundle在Android 中，相当于一个数据容器，主要通过bundle.***put()/get***()方法进行数据的存储和获取，其内部实际上是维护着一个Map，传输的数据类型除了基本数据类型、集合等常见的数据外，还可传输用户实现了Parcelable或者Serializable接口的自定义类。

```kotlin
        val intent = Intent()
        val bundle = Bundle()
        bundle.putBoolean("boolean",true)
        bundle.putByte("byte",1)
        bundle.putChar("char",'a')
        bundle.putInt("age",20)
        bundle.putLong("long",1L)
        bundle.putString("String","aa")
        intent.putExtras(bundle)
        startActivity(intent)

```

#### 2、文件共享

文件共享是通过将数据在某一进程中进行存储，再在另一进程中进行读取来实现的跨进程通信，因为Android 是基于Linux的，所以可进行文件的同时读/写（虽然可能会出问题）。需要注意的是，该方式其本质是通过数据持久化实现的，而Parcelable接口不适合用于数据持久化的序列/反序列化，所以只能实现Serializable 接口来实现自定义类的传输。

MainActivity.kt

```kotlin
val student = Student("张三", 21)
        val objectOutputStream:ObjectOutputStream
        try {
            val file = File(getExternalFilesDir(null), "text.txt")
            objectOutputStream = ObjectOutputStream(FileOutputStream(file))
            objectOutputStream.writeObject(student)
            objectOutputStream.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
```

IPCActivity.kt

```kotlin
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
```

说到文件共享，就顺便提一句，在Android 中，有两种文件存储：**Internal storage**（内部存储空间）和**External storage**（外部存储空间）。二者的区别：

|          |                         内部存储空间                         |                         外部存储空间                         |
| -------- | :----------------------------------------------------------: | :----------------------------------------------------------: |
| 访问权限 | 文件为应用私有，其他应用（和用户）不能访问这些文件（除非拥有 Root 访问权限） | 保存至外部存储的文件是全局可读取文件，而且当在计算机上启用 USB 大容量存储来传输文件时，用户可对这些文件进行修改。但也有私有空间，不能被其他用户及用户访问。 |
| 卸载时   |  当用户卸载您的应用时，保存在内部存储中的文件也将随之移除。  | 保存在外部存储空间中的内容不会随着应用的卸载而清除，但可能会被其他操作修改，如其他应用读写，用户删除等 |
| 权限申请 |              不需要权限获取，可直接进行文件操作              | 操作公共文件需要在manifest清单中声明读写权限[^1][^2]。从Android 4.4(API level 19)开始，操作私有文件不再需要申请权限。 |

文件相关官方文档： [数据和文件存储概览](https://developer.android.google.cn/guide/topics/data/data-storage)

#### 3、共享参数（SharedPreferences）

听着名字可能不知道这是个什么东西，但其实大家很早就接触过了，这就是《第一行代码》上数据持久化中讲的SharedPreferences，该方式也相当于文件存储，但比较轻量级，能传输的数据有限，仅支持Boolean、Float、Int、Long、String以及Set\<String> （有问题）类型的数据。也是通过XML键值对的形式进行存储。但官方不支持跨多个进程使用。

存入使用方式

```kotlin
            context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit {
                putBoolean("isLogin", true)
                putString("account", account)
                putString("token", token)
                putInt("userId", id)
                putBoolean("inSchool", inSchool)
                commit()
            }
```

取出使用方式

```kotlin
        val sharedPreferences = context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE)
            sharedPreferences.apply { 
                getBoolean("isLogin",false)
                getString("account","")
                getString("token","")
                getInt("userId",0)
                getBoolean("inSchool",false)
            }
```

在SharedPreferences中，有两个方法，分别为

```kotlin
abstract fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener!): Unit
abstract fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener!): Unit
```

用于对sharedPreferences内容变化的监听及解除监听。

SharedPreferences相关官方文档： [SharedPreferences](https://developer.android.google.cn/reference/kotlin/android/content/SharedPreferences?hl=en)

#### 4、Handler

看其源码，实际上也是通过Bundle来实现的跨进程通信，而Bundle又实现了Parcelable接口，Message也实现了Parcelable接口，所以本质上还是通过Parcel，或者说是IBinder来实现的跨进程通信。

#### 5、AIDL

AIDL全名，接口描述语言，AIDL文件的创建方法如下图，这样就可以创建一个AIDL文件了。

<img src="C:\Users\TKW\AppData\Roaming\Typora\typora-user-images\1569750038791.png" alt="1569750038791"  />

生成的文件内容如下图

![1569750713456](C:\Users\TKW\AppData\Roaming\Typora\typora-user-images\1569750713456.png)

其中只有一个方法，basicTypes()，这个方法没什么用处，可直接删掉。

可在这个接口里面定义自己需要的方法，语法规则按照Java语法进行编写，需要引入自定义Parcelable接口实现类，则必须自行引入完整包名。可使用的数据类型有如下这些：

- Java 编程语言中的所有基本数据类型（如 `int`、`long`、`char`、`boolean` 等）

- `String`

- `CharSequence`

- ```
  List
  ```

  `List` 中的所有元素必须是以上列表中支持的数据类型，或者您所声明的由 AIDL 生成的其他接口或 Parcelable 类型。您可选择将 `List` 用作“泛型”类（例如，`List<String>`）。尽管生成的方法旨在使用 `List` 接口，但另一方实际接收的具体类始终是 `ArrayList`。

- ```
  Map
  ```

  `Map` 中的所有元素必须是以上列表中支持的数据类型，或者您所声明的由 AIDL 生成的其他接口或 Parcelable 类型。不支持泛型 Map（如 `Map<String,Integer>` 形式的 Map）。尽管生成的方法旨在使用 `Map` 接口，但另一方实际接收的具体类始终是 `HashMap`。

  而如果要传输对象的话，则对象必须实现Parcelable接口，如下案例：

  ```kotlin
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
  ```

  那这个数据嘞类该怎么使用呢，使用方法如下：
  
  ![1569913161634](C:\Users\TKW\AppData\Roaming\Typora\typora-user-images\1569913161634.png)
  
  先在AIDL文件中创建相同的包结构，然后下方只需要 parcelable 类名 就行，注意parcelable是小写字母开头。
  
  ![1569913241908](C:\Users\TKW\AppData\Roaming\Typora\typora-user-images\1569913241908.png)
  
  这样就可以在接口中使用改数据类了。

#### 6 、ContentProvider

#### 7、Socket

#### 二、Binder

这个就借鉴了：

[写给 Android 应用工程师的 Binder 原理剖析](https://www.jianshu.com/p/429a1ff3560c)

[Android Bander设计与实现 - 设计篇](https://blog.csdn.net/universus/article/details/6211589)

这两篇博客应该把Binder解释清楚了。





#### 三、Bundle跨进程通信原理

因为我们存取数据都是以Key-Value的形式进行的，所以猜想Bundle内部应该也是用键值对来存的，查看源码，果不其然，在BaseBundle类中，发现了一行代码，这就是用来存数据的地方啊。

```java
    // Invariant - exactly one of mMap / mParcelledData will be null
    // (except inside a call to unparcel)

    @UnsupportedAppUsage
    ArrayMap<String, Object> mMap = null;
```

而在BaseBundle中还发现了一个比较有意思的东西，所有的get()/set()方法内，都调用了unparcel()这个方法。

```java
 /**
     * If the underlying data are stored as a Parcel, unparcel them
     * using the currently assigned class loader.
     */
    @UnsupportedAppUsage
    /* package */ void unparcel() {
        synchronized (this) {
            final Parcel source = mParcelledData;
            if (source != null) {
                initializeFromParcelLocked(source, /*recycleParcel=*/ true, mParcelledByNative);
            } else {
                if (DEBUG) {
                    Log.d(TAG, "unparcel "
                            + Integer.toHexString(System.identityHashCode(this))
                            + ": no parcelled data");
                }
            }
        }
    }
```

其中主要又调用了initializeFromParcelLocked(source, /*recycleParcel=*/ true, mParcelledByNative)方法，我猜想跨进程通信的入口就在下面这个函数吧，然后看下去，果然发现，该函数进行了一系列的判断，其中有一个判断是parcelledByNative，也就是Native层来的，猜测这就是跨进程通信的入口了。

```java
private void initializeFromParcelLocked(@NonNull Parcel parcelledData, boolean recycleParcel,
            boolean parcelledByNative) {
        if (LOG_DEFUSABLE && sShouldDefuse && (mFlags & FLAG_DEFUSABLE) == 0) {
            Slog.wtf(TAG, "Attempting to unparcel a Bundle while in transit; this may "
                    + "clobber all data inside!", new Throwable());
        }

        if (isEmptyParcel(parcelledData)) {
            if (DEBUG) {
                Log.d(TAG, "unparcel "
                        + Integer.toHexString(System.identityHashCode(this)) + ": empty");
            }
            if (mMap == null) {
                mMap = new ArrayMap<>(1);
            } else {
                //mMap为ArrayMap，erase方法是一个内部方法，被标识为@hide
                //该方法类似于ArrayMap的clear()方法，但不会减少ArrayMap的容量。
                //应该是为了避免内存空间的重复分配吧
                mMap.erase();
            }
            mParcelledData = null;
            mParcelledByNative = false;
            return;
        }

        final int count = parcelledData.readInt();
        if (DEBUG) {
            Log.d(TAG, "unparcel " + Integer.toHexString(System.identityHashCode(this))
                    + ": reading " + count + " maps");
        }
        if (count < 0) {
            return;
        }
        ArrayMap<String, Object> map = mMap;
        if (map == null) {
            map = new ArrayMap<>(count);
        } else {
            map.erase();
            //该函数是应该用于给ArrayMap分配空间的，在这不再深究
            map.ensureCapacity(count);
        }
        try {
            if (parcelledByNative) {
                // If it was parcelled by native code, then the array map keys aren't sorted
                // by their hash codes, so use the safe (slow) one.
            	//该行应该就是跨进程通信的入口了
                parcelledData.readArrayMapSafelyInternal(map, count, mClassLoader);
            } else {
                // If parcelled by Java, we know the contents are sorted properly,
                // so we can use ArrayMap.append().
                parcelledData.readArrayMapInternal(map, count, mClassLoader);
            }
        } catch (BadParcelableException e) {
            if (sShouldDefuse) {
                Log.w(TAG, "Failed to parse Bundle, but defusing quietly", e);
                map.erase();
            } else {
                throw e;
            }
        } finally {
            mMap = map;
            if (recycleParcel) {
                recycleParcel(parcelledData);
            }
            mParcelledData = null;
            mParcelledByNative = false;
        }
        if (DEBUG) {
            Log.d(TAG, "unparcel " + Integer.toHexString(System.identityHashCode(this))
                    + " final map: " + mMap);
        }
    }
```

在Parcel类中，找到这个方法parcelledData.readArrayMapSafelyInternal(map, count, mClassLoader);

其中有一个String key = readString(); 和Object value = readValue(loader);，这两个方法就开始拿数据了，并通过传值过去的类加载器对类进行加载。

```java
    /* package */ void readArrayMapSafelyInternal(@NonNull ArrayMap outVal, int N,
            @Nullable ClassLoader loader) {
        if (DEBUG_ARRAY_MAP) {
            RuntimeException here =  new RuntimeException("here");
            here.fillInStackTrace();
            Log.d(TAG, "Reading safely " + N + " ArrayMap entries", here);
        }
        while (N > 0) {
            //通过该方法从native层拿到key
            String key = readString();
            if (DEBUG_ARRAY_MAP) Log.d(TAG, "  Read safe #" + (N-1) + ": key=0x"
                    + (key != null ? key.hashCode() : 0) + " " + key);
            //同过该方法从netive层拿到对应的value
            Object value = readValue(loader);
            outVal.put(key, value);
            N--;
        }
    }
```

Parcel类最前面的注释中写到，IBinder作为一个接口，而Parcel是作为一个数据容器，来沟通Native层。个人认为实际进行跨进程通信的应该是Parcel类

```java
Container for a message (data and object references) that can
be sent through an IBinder.  A Parcel can contain both flattened data
that will be unflattened on the other side of the IPC (using the various
methods here for writing specific types, or the general
{@link Parcelable} interface), and references to live {@link IBinder}
objects that will result in the other side receiving a proxy IBinder
connected with the original IBinder in the Parcel.
消息的容器（数据和对象引用）通过IBinder发送。 一个Parcel可以同时包含扁平化数据将在IPC的另一端展开（使用各种此处用于编写特定类型或通用类型的方法{@link Parcelable}接口），以及对实时{@link IBinder}的引用会导致另一端接收代理IBinder的对象与包裹中的原始IBinder连接。

(吐槽谷歌翻译)，大致意思就是数据或对象可以通过IBinder进行传输,Parcel可以包含基本数据或者实现了Parcelable接口的对象（猜测是这样），这些数据会被发送至另一进程，并在另一进程生成一个代理类，进行接收
```

差不多Bundle的跨进程就是这样实现了。



[^1]: <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
[^2]: <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
