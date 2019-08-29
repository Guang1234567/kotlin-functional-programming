package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Eq

interface IntEq : Eq<Int> {
    override fun Int.eqv(b: Int): Boolean = this == b
}

fun Int.Companion.eq(): Eq<Int> = object : IntEq {}