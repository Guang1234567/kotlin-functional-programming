package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Kind
import com.functional.programming.haskell.typeclasses.Monad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext


class ForFutureK private constructor() {
    companion object
}

typealias FutureKOf<A> = Kind<ForFutureK, A>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
private inline fun <A> FutureKOf<A>.fix(): FutureK<A> =
    this as FutureK<A>

/**
 *  covert `Deferred<A>` to `FutureK<A>`
 */
private inline fun <A> Deferred<A>.k(): FutureK<A> = FutureK(value = this)

class FutureK<out A> internal constructor(
    private val ctx: CoroutineContext = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(ctx),
    private val start: CoroutineStart = CoroutineStart.LAZY,
    private val value: Deferred<A>
) : Kind<ForFutureK, A> {

    companion object {}

    override fun toString(): String {
        return "FutureK($value)"
    }

    constructor(
        ctx: CoroutineContext = Dispatchers.Default,
        scope: CoroutineScope = CoroutineScope(ctx),
        start: CoroutineStart = CoroutineStart.LAZY,
        f: suspend CoroutineScope.() -> A
    ) : this(ctx, scope, start, scope.async(ctx, start, f))

    internal inline fun <B> binding(crossinline f: (A) -> FutureKOf<B>): FutureK<B> =
        FutureK(ctx, scope, start) {
            val current = value.await()
            f(current).fix().value.await()
        }

    internal suspend fun bind(): A? = value.await()

    fun runSync(): A = runBlocking {
        value.await()
    }

    fun runASync(
        ctx: CoroutineContext = Dispatchers.Default,
        scope: CoroutineScope = CoroutineScope(ctx),
        start: CoroutineStart = CoroutineStart.LAZY,
        onError: suspend (Throwable) -> Unit,
        onCompleted: suspend (result: A) -> Unit
    ): Job =
        scope.launch(ctx, start) {
            try {
                onCompleted(value.await())
            } catch (e: Throwable) {
                onError(e)
            }
        }
}


//------------------------------
// Monad
//------------------------------

interface FutureKMonad : Monad<ForFutureK> {

    override fun <A> just(value: A): FutureK<A> = FutureK { value }

    override fun <A, B> FutureKOf<A>.binding(f: (A) -> FutureKOf<B>): FutureK<B> = fix().binding(f)

    override suspend fun <A> bind(m: FutureKOf<A>): A? = m.fix().bind()
}

fun FutureK.Companion.monad(): FutureKMonad =
    object : FutureKMonad {}

infix fun <A, B> FutureKOf<A>.binding(f: (A) -> FutureKOf<B>): FutureK<B> = FutureK.monad().run {
    this@binding.binding(f)
}

infix fun <A, B> FutureK<A>.binding(f: (A) -> FutureKOf<B>): FutureK<B> =
    (this as FutureKOf<A>).binding(f)


fun <B> FutureK.Companion.monadComprehensions(
    name: String = "",
    ctx: CoroutineContext = Dispatchers.IO,
    f: suspend MonadComprehensions<ForFutureK, *>.() -> B
): FutureK<B> = FutureK.monad().run {
    comprehensions(name, this, ctx, f)
}.fix()

fun <B> FutureK.Companion.monadComprehensions2(
    name: String = "",
    ctx: CoroutineContext = Dispatchers.Default,
    scope: CoroutineScope = CoroutineScope(ctx),
    start: CoroutineStart = CoroutineStart.LAZY,
    f: suspend MonadComprehensions2<ForFutureK, *>.() -> B
): FutureK<B> = FutureK.monad().run {
    comprehensions2(name, this, ctx, scope, start, f)
}.fix()