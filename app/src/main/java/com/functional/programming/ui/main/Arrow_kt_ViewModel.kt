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
import io.sellmair.disposer.Disposer
import io.sellmair.disposer.disposers
import kotlinx.coroutines.Dispatchers
import arrow.fx.typeclasses.Disposable as FxDisposable
import io.reactivex.disposables.Disposable as RxDisposable


class Arrow_kt_ViewModel : ViewModel(), LifecycleOwner {

    companion object {

        const val TAG = "Arrow_kt_ViewModel"
    }

    private val mLifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    init {
        mLifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
    }


    override fun getLifecycle(): Lifecycle = mLifecycleRegistry

    override fun onCleared() {
        super.onCleared()
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    fun FxDisposable.disposeBy(disposer: Disposer): FxDisposable {
        disposer += object : RxDisposable {
            override fun isDisposed(): Boolean = false

            override fun dispose() = this@disposeBy()
        }
        return this
    }


    suspend fun sayHello(): Int =
        Log.d(TAG, "Hello World")


    suspend fun sayGoodBye(): Int =
        Log.d(TAG, "Good bye World!")

    fun greet(callBackOnUI01: (Int) -> IO<Unit>, callBackOnUI02: suspend (Int) -> Unit) {
        IO.fx {

            Log.d(TAG, "greet")

            effect { sayHello() }
            !effect { sayGoodBye() }


            /*val result1 = !effect { sayHello() }
            val result2 = !effect { sayGoodBye() }

            val (result3) = effect { sayHello() }
            val (result4) = effect { sayGoodBye() }

            val result5 = effect { sayHello() }.bind()
            val result6 = effect { sayGoodBye() }.bind()*/

            continueOn(Dispatchers.Main)
            !callBackOnUI01(123)
            !effect { callBackOnUI02(321) }

        }.unsafeRunAsyncCancellable { result ->
            result.fold(
                { Log.e(MainFragment.TAG, "Boom! caused by ", it) },
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

            println(result)
        }

}
