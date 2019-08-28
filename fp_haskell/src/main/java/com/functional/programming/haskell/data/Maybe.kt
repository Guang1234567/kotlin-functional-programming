package com.functional.programming.haskell.data

import com.functional.programming.haskell.typeclass.Eq
import com.functional.programming.haskell.typeclass.Functor
import com.functional.programming.haskell.typeclass.Monad

sealed class Maybe<out T> {

    companion object

    object Nothing : Maybe<kotlin.Nothing>() {
        override fun toString(): String = "Maybe.Nothing"
    }

    data class Just<out T>(val value: T) : Maybe<T>() {
        override fun toString(): String = "Maybe.Just { $value }"
    }
}

//------------------------------
// Monad
//------------------------------

private interface MaybeMonad<T, R> : Monad<T, Maybe<T>, Maybe<R>> {
    override infix fun Maybe<T>.binding(f: (T) -> Maybe<R>): Maybe<R> = when (this) {
        Maybe.Nothing -> Maybe.Nothing
        is Maybe.Just -> f(this.value)
    }
}

fun <T, R> Maybe.Companion.monad(): Monad<T, Maybe<T>, Maybe<R>> =
    object : MaybeMonad<T, R> {}

//------------------------------
// Eq
//------------------------------

private interface MaybeEq<T> : Eq<Maybe<T>> {
    override fun Maybe<T>.eqv(b: Maybe<T>): Boolean = when (this) {
        Maybe.Nothing -> b == Maybe.Nothing
        is Maybe.Just -> (b is Maybe.Just) && (this.value == b.value)
    }
}

fun <T> Maybe.Companion.eq(): Eq<Maybe<T>> =
    object : MaybeEq<T> {}

//------------------------------
// Functor
//------------------------------

private interface MaybeFunctor<T, R> : Functor<Maybe<T>, Maybe<R>> {
    override fun <T, R> Maybe<T>.fmap(f: (T) -> R): Maybe<R> = when (this) {
        Maybe.Nothing -> Maybe.Nothing
        is Maybe.Just -> Maybe.Just(f(this.value))
    }
}

fun <T, R> Maybe.Companion.functor(): Functor<T, R, Maybe<T>, Maybe<R>> =
    object : MaybeFunctor<T, R> {}