package com.functional.programming.haskell.typeclasses

interface Monad<F> {
    fun <A, B> Kind<F, A>.binding(f: (A) -> Kind<F, B>): Kind<F, B>
}