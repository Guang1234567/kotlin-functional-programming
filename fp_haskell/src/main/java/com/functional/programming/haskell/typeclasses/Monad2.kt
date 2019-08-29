package com.functional.programming.haskell.typeclasses

import com.functional.programming.haskell.core.ForMaybe

interface Monad2<F, out A> {
    infix fun <B> binding2(f: (A) -> Kind<ForMaybe, B>): Kind<ForMaybe, B>
}