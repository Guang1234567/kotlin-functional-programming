package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Eq


//------------------------------
// Eq
//------------------------------

interface IntEq : Eq<Int> {
    override fun Int.eqv(b: Int): Boolean = this == b
}

fun Int.Companion.eq(): IntEq = object : IntEq {}

fun Int.eqv(b: Int): Boolean = Int.eq().run {
    this@eqv.eqv(b)
}

fun Int.neqv(b: Int): Boolean = Int.eq().run {
    this@neqv.neqv(b)
}