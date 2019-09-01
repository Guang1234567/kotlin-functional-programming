package com.functional.programming.ui.main

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.Right
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.async.async
import arrow.fx.fix
import com.functional.programming.model.DataBaseRepo
import com.functional.programming.model.OnDataBaseChangedListener
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.sellmair.disposer.Disposer
import io.sellmair.disposer.disposers
import kotlinx.coroutines.Dispatchers
import arrow.fx.typeclasses.Disposable as FxDisposable
import io.reactivex.disposables.Disposable as RxDisposable


class DemoViewModel : ViewModel(), LifecycleOwner, OnDataBaseChangedListener {

    companion object {

        const val TAG = "DemoViewModel"
    }

    val onDataBaseChangedListener: Flowable<List<String>>
        get() = mOnDataBaseChangedListener

    private val mOnDataBaseChangedListener: FlowableProcessor<List<String>> by lazy {
        PublishProcessor.create<List<String>>()
    }

    private val mDataBaseRepo: DataBaseRepo by lazy {
        DataBaseRepo()
    }


    private val mLifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }


    init {
        mLifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
        mDataBaseRepo.addListener(this@DemoViewModel)
    }


    override fun onCleared() {
        super.onCleared()
        mDataBaseRepo.removeListener(this@DemoViewModel)
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }


    override fun getLifecycle(): Lifecycle = mLifecycleRegistry


    private fun FxDisposable.disposeBy(disposer: Disposer): FxDisposable {
        disposer += object : RxDisposable {
            override fun isDisposed(): Boolean = false

            override fun dispose() = this@disposeBy()
        }
        return this
    }


    /**
     * `OnDataBaseChangedListener`
     */
    override fun invoke(users: List<String>): IO<Unit> =
        IO.fx {
            // 切换回 UI 线程
            continueOn(Dispatchers.Main)

            // Update UI here
            mOnDataBaseChangedListener.onNext(users)
        }


    fun addUser(userName: String): FxDisposable =
        mDataBaseRepo.addUser(userName)
            .unsafeRunAsyncCancellable { result ->
                result.fold(
                    { Log.e(TAG, "Boom! caused by ", it) },
                    { println(it.toString()) })
            }.disposeBy(disposers.onDestroy)


    suspend fun sayHello(): Int =
        Log.d(TAG, "Hello World")


    suspend fun sayGoodBye(): Int =
        Log.d(TAG, "Good bye World!")

    fun greet(callBackOnUI01: (Int) -> IO<Unit>, callBackOnUI02: suspend (Int) -> Unit) {
        IO.fx {
            Log.d(TAG, "greet")

            !effect { sayHello() }
            effect { sayGoodBye() } // 不会执行, 要手动在前面加个 `!` 才会执行.


            /*
            val result1 = !effect { sayHello() }
            val result2 = !effect { sayGoodBye() }

            val (result3) = effect { sayHello() }
            val (result4) = effect { sayGoodBye() }

            val result5 = effect { sayHello() }.bind()
            val result6 = effect { sayGoodBye() }.bind()
            */

            continueOn(Dispatchers.Main)

            // 注意: 不要写类型为  `x -> IO(y)` 这种返回 `IO Monad` 的回调函数. 这只是例子而已.
            // 下面3种等价
            val v0 = !callBackOnUI01(123)
            val v1 = callBackOnUI01(1234).bind()
            val (v2) = callBackOnUI01(12345)

            !effect { callBackOnUI02(321) }

        }.unsafeRunAsyncCancellable { result ->
            result.fold(
                { Log.e(TAG, "Boom! caused by ", it) },
                { println(it.toString()) })
        }.disposeBy(disposers.onDestroy)
    }


    fun greet2() =
        IO.async { cb: (Either<Throwable, Int>) -> Unit ->
            cb(Right(321))
        }

    fun greet3() =
        IO.async().run {
            val result = fx.async() {
                continueOn(Dispatchers.Default)
                Thread.currentThread().name
            }.fix().unsafeRunSync()
        }

}
