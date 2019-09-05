package com.functional.programming.haskell.typeclasses

import android.util.Log
import com.functional.programming.haskell.core.MonadComprehensions
import com.functional.programming.haskell.core.MonadComprehensions2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.startCoroutine

interface Monad<F> {

    companion object {
        const val TAG = "Monad"
    }

    fun <A> just(value: A): Kind<F, A>

    fun <A, B> Kind<F, A>.binding(f: (A) -> Kind<F, B>): Kind<F, B>

    suspend fun <A> bind(m: Kind<F, A>): A?

    fun <B> comprehensions(
        name: String,
        monadInstance: Monad<F>,
        ctx: CoroutineContext = Dispatchers.Default,
        f: suspend MonadComprehensions<F, *>.() -> B
    ): Kind<F, B> {
        val continuation = MonadComprehensions<F, B>(name, this)
        val safeName = if (name == "") "$continuation" else name

        val wrapReturn: suspend MonadComprehensions<F, *>.() -> Kind<F, B> = {
            Log.w(TAG, "$safeName    wrapReturn just before      ${Thread.currentThread()}")
            val resultFrom = f()
            val result = just(resultFrom)
            Log.w(
                TAG,
                "$safeName    wrapReturn just after $resultFrom      ${Thread.currentThread()}"
            )
            result
        }
        Log.w(TAG, "$safeName   wrapReturn.startCoroutine before  ${Thread.currentThread()}")
        wrapReturn.startCoroutine(continuation, continuation)
        val returnMonad = continuation.returnedMonad()
        Log.w(
            TAG,
            "$safeName    wrapReturn.startCoroutine after  $returnMonad     ${Thread.currentThread()}"
        )
        return returnMonad
    }


    fun <B> comprehensions2(
        name: String,
        monadInstance: Monad<F>,
        ctx: CoroutineContext = Dispatchers.Default,
        scope: CoroutineScope = CoroutineScope(ctx),
        start: CoroutineStart = CoroutineStart.LAZY,
        f: suspend MonadComprehensions2<F, *>.() -> B
    ): Kind<F, B> {
        val safeName = if (name == "") "MonadComprehensions#StartPoint" else name
        val start: Kind<F, String> = monadInstance.just(safeName)
        return start.binding {
            val mcResult = runBlocking(ctx) {
                MonadComprehensions2<F, B>(name, monadInstance, scope).run {
                    f()
                }
            }
            monadInstance.just(mcResult)
        }
    }
}