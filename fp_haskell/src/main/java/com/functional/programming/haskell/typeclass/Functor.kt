package com.functional.programming.haskell.typeclass

interface Functor<FT, FR> {

    fun <T, R> FT.fmap(f: (T) -> R): FR
}