package com.functional.programming.haskell.data

import com.functional.programming.haskell.typeclass.Monad

sealed class Maybe<out T> : Monad<T> {
    object Nothing : Maybe<kotlin.Nothing>() {

        override fun <R> binding(f: (kotlin.Nothing) -> Monad<R>): Maybe<R> = this

        override fun toString(): String = "Maybe.Nothing"
    }

    data class Just<out T>(val value: T) : Maybe<T>() {
        override fun toString(): String = "Maybe.Just { $value }"

        override fun <R> binding(f: (T) -> Monad<R>): Monad<R> = f(this.value)
    }
}