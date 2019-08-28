package com.functional.programming.haskell.data

import com.functional.programming.haskell.typeclass.Monad

sealed class Maybe<out T> : Monad<Maybe<T>> {
    object Nothing : Maybe<kotlin.Nothing>() {

        override fun <R> binding(f: (kotlin.Nothing) -> Maybe<R>): Maybe<R> = this

        override fun toString(): String = "Maybe.Nothing"
    }

    data class Just<out T>(val value: T) : Maybe<T>() {
        override fun toString(): String = "Maybe.Just { $value }"

        override fun <R> binding(f: (T) -> Maybe<R>): Maybe<R> = f(this.value)
    }
}