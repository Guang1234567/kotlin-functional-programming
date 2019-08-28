package com.functional.programming.haskell.typeclass

interface Functor<out T> {

    fun <R> fmap(transform: (T) -> R): Functor<T>
}