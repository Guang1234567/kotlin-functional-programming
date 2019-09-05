package com.functional.programming.haskell.core

import android.util.Log
import arrow.typeclasses.stateStack
import com.functional.programming.haskell.typeclasses.Kind
import com.functional.programming.haskell.typeclasses.Monad
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume

open class MonadComprehensions<F, A>(
    val name: String,
    val monadInstance: Monad<F>,
    override val context: CoroutineContext = EmptyCoroutineContext
) : Continuation<Kind<F, A>> {

    companion object {
        const val TAG: String = "MonadComprehensions"
    }

    private lateinit var returnedMonad: Kind<F, A>

    override fun resumeWith(result: Result<Kind<F, A>>) =
        result.fold(::resume, ::resumeWithException)

    fun resume(value: Kind<F, A>) {
        returnedMonad = value
    }

    fun resumeWithException(exception: Throwable) {
        throw exception
    }

    fun returnedMonad(): Kind<F, A> = returnedMonad

    suspend fun <B> Kind<F, B>.bind(): B = suspendCoroutineUninterceptedOrReturn { c ->
        Log.w(TAG, "$name  bind   ${Thread.currentThread()}")
        val labelHere = c.stateStack // save the whole coroutine stack labels
        monadInstance.run {
            returnedMonad = this@bind.binding { x: B ->
                c.stateStack = labelHere
                Log.w(TAG, "$name resume $x          ${Thread.currentThread()}")
                c.resume(x)
                Log.w(
                    TAG,
                    "$name  returnedMonad $returnedMonad         ${Thread.currentThread()}"
                )
                returnedMonad
            }
        }
        Log.w(TAG, "$name  COROUTINE_SUSPENDED $returnedMonad        ${Thread.currentThread()}")
        COROUTINE_SUSPENDED
    }
}

open class MonadComprehensions2<F, A>(
    val name: String,
    val monadInstance: Monad<F>,
    scope: CoroutineScope
) : CoroutineScope by scope {

    suspend fun <B> Kind<F, B>.bind(): B = monadInstance.bind(this)!!
}
