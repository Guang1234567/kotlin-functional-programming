package com.functional.programming.haskell.typeclasses

interface Monad<F> {
    infix fun <A, B> Kind<F, A>.binding(f: (A) -> Kind<F, B>): Kind<F, B>
}