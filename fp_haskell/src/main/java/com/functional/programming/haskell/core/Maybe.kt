package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Eq
import com.functional.programming.haskell.typeclasses.Functor
import com.functional.programming.haskell.typeclasses.Kind
import com.functional.programming.haskell.typeclasses.Monad


class ForMaybe private constructor() {
    companion object
}

typealias MaybeOf<A> = Kind<ForMaybe, A>

/**
 * 将 Kind 变回 Maybe
 */
inline fun <A> MaybeOf<A>.fix(): Maybe<A> =
    this as Maybe<A>

sealed class Maybe<out A> : MaybeOf<A> {

    companion object

    object Nothing : Maybe<kotlin.Nothing>() {
        override fun toString(): String = "Maybe.Nothing"
    }

    data class Just<out A>(val value: A) : Maybe<A>() {
        override fun toString(): String = "Maybe.Just { $value }"
    }

    internal inline infix fun <B> binding(f: (A) -> MaybeOf<B>): Maybe<B> = when (this) {
        Nothing -> this as Maybe<B>
        is Just -> f(value).fix()
    }

    internal inline infix fun <B> fmap(f: (A) -> B): Maybe<B> = when (this) {
        Nothing -> Nothing
        is Just -> Just(f(value))
    }
}

//------------------------------
// Eq
//------------------------------

private interface MaybeEq<A> : Eq<Maybe<A>> {
    override fun Maybe<A>.eqv(b: Maybe<A>): Boolean = when (this) {
        Maybe.Nothing -> b == Maybe.Nothing
        is Maybe.Just -> (b is Maybe.Just) && (this.value == b.value)
    }
}

fun <A> Maybe.Companion.eq(): Eq<Maybe<A>> =
    object : MaybeEq<A> {}

//------------------------------
// Monad
//------------------------------

private interface MaybeMonad : Monad<ForMaybe> {

    override fun <A, B> MaybeOf<A>.binding(f: (A) -> MaybeOf<B>): Maybe<B> = fix().binding(f)
}

fun Maybe.Companion.monad(): Monad<ForMaybe> =
    object : MaybeMonad {}

//------------------------------
// Functor
//------------------------------

private interface MaybeFunctor : Functor<ForMaybe> {
    override fun <A, B> MaybeOf<A>.fmap(f: (A) -> B): Maybe<B> = fix().fmap(f)
}

fun Maybe.Companion.functor(): Functor<ForMaybe> =
    object : MaybeFunctor {}