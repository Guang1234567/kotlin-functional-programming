package com.functional.programming.ui.main

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import arrow.core.Right
import arrow.effects.DeferredK
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.async.async
import arrow.typeclasses.MonadContinuation
import com.functional.programming.model.DataBaseRepo
import com.functional.programming.model.OnDataBaseChangedListener
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.sellmair.disposer.Disposer
import io.sellmair.disposer.disposers
import kotlinx.coroutines.Dispatchers
import arrow.effects.typeclasses.Disposable as FxDisposable
import io.reactivex.disposables.Disposable as RxDisposable


fun <B> binding(arg0: suspend MonadContinuation<ForIO, *>.() -> B): IO<B> =
    IO.async().run {
        binding<B>(arg0)
    }.fix()


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
        mLifecycleRegistry.markState(Lifecycle.State.CREATED)
        mDataBaseRepo.addListener(this@DemoViewModel)
    }


    override fun onCleared() {
        super.onCleared()
        mDataBaseRepo.removeListener(this@DemoViewModel)
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED)

        val deferredK = DeferredK { throw RuntimeException("BOOM!") }
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
        IO.async().run {
            bindingCatch {
                // 切换回 UI 线程
                continueOn(Dispatchers.Main)

                // Update UI here
                mOnDataBaseChangedListener.onNext(users)
            }
        }.fix()


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
        IO.async().run {
            bindingCatch {
                Log.d(TAG, "greet")
                continueOn(Dispatchers.Main)
                val v1 = callBackOnUI01(1234).bind()

            }
        }.fix().unsafeRunAsyncCancellable { result ->
            result.fold(
                { Log.e(TAG, "Boom! caused by ", it) },
                { println(it.toString()) })
        }.disposeBy(disposers.onDestroy)
    }


    fun greet2(): IO<Int> =
        IO.async { _, cb ->
            cb(Right(321))
        }

    fun greet3(): String =
        IO.async().run {
            bindingCatch {
                continueOn(Dispatchers.Default)
                Thread.currentThread().name
            }.fix().unsafeRunSync()
        }
}
