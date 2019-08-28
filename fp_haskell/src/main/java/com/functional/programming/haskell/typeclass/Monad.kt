package com.functional.programming.haskell.typeclass

interface Monad<out T> {

    infix fun <R> binding(f: (T) -> Monad<R>): Monad<R>
}