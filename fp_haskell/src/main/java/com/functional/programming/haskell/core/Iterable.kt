package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Functor
import com.functional.programming.haskell.typeclasses.Kind

class ForIterableK private constructor() {
    companion object
}

typealias IterableKOf<A> = Kind<ForIterableK, A>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
private inline fun <A> IterableKOf<A>.fix(): IterableK<A> =
    this as IterableK<A>

/**
 *  covert `Iterable<A>` to `IterableK<A>`
 */
private inline fun <A> Iterable<A>.k(): IterableK<A> = IterableK(this)

/**
 *  covert `Iterable<A>` to `IterableKOf<A>`
 */
private inline fun <A> Iterable<A>.kk(): IterableKOf<A> = k()

data class IterableK<out A>(private val iterable: Iterable<A>) :
    Iterable<A> by iterable,
    IterableKOf<A> {

    companion object {}

    override fun toString(): String {
        return "IterableK($iterable)"
    }

    internal inline fun <B> fmap(f: (A) -> B): IterableK<B> = iterable.map(f).k()
}

//------------------------------
// Functor
//------------------------------


interface IterableKFunctor : Functor<ForIterableK> {
    override fun <A, B> IterableKOf<A>.fmap(f: (A) -> B): IterableK<B> = fix().fmap(f)
}

fun IterableK.Companion.functor(): IterableKFunctor =
    object : IterableKFunctor {}


infix fun <A, B> IterableKOf<A>.fmap(f: (A) -> B): IterableK<B> = IterableK.functor().run {
    this@fmap.fmap(f)
}


infix fun <A, B> Iterable<A>.fmap(f: (A) -> B): Iterable<B> = IterableK.functor().run {
    this@fmap.kk().fmap(f)
} /* or map(f) */
