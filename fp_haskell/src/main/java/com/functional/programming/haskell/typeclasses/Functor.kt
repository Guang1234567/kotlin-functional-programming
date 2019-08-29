package com.functional.programming.haskell.typeclasses

interface Functor<F> {
    infix fun <A, B> Kind<F, A>.fmap(f: (A) -> B): Kind<F, B>
}