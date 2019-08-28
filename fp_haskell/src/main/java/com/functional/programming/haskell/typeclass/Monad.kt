package com.functional.programming.haskell.typeclass

interface Monad<T, MT, MR> {
    infix fun MT.binding(f: (T) -> MR): MR
}