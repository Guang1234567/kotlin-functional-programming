package com.functional.programming.haskell.typeclass

interface Eq<T> {
    fun T.eqv(b: T): Boolean

    fun T.neqv(b: T): Boolean =
        !eqv(b)
}