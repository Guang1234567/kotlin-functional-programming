package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Eq
import com.functional.programming.haskell.typeclasses.Functor
import com.functional.programming.haskell.typeclasses.Kind
import com.functional.programming.haskell.typeclasses.Monad
import com.functional.programming.haskell.typeclasses.Monad2


class ForMaybe private constructor() {
    companion object
}

typealias MaybeOf<A> = Kind<ForMaybe, A>

/**
 * 将 Kind 变回 Maybe
 */
inline fun <A> MaybeOf<A>.fix(): Maybe<A> =
    this as Maybe<A>

sealed class Maybe<out A> : MaybeOf<A>, Monad2<ForMaybe, A> {

    companion object

    object Nothing : Maybe<kotlin.Nothing>() {
        override fun toString(): String = "Maybe.Nothing"
    }

    data class Just<out A>(val value: A) : Maybe<A>() {
        override fun toString(): String = "Maybe.Just($value)"
    }

    internal inline fun <B> binding(f: (A) -> MaybeOf<B>): Maybe<B> = when (this) {
        is Nothing -> this
        is Just -> f(value).fix()
    }

    internal inline fun <B> fmap(f: (A) -> B): Maybe<B> = when (this) {
        is Nothing -> this
        is Just -> Just(f(value))
    }

    override fun <B> binding2(f: (A) -> MaybeOf<B>): Maybe<B> = when (this) {
        is Nothing -> this
        is Just -> f(value).fix()
    }
}

//------------------------------
// Eq
//------------------------------

interface MaybeEq<A> : Eq<Maybe<A>> {

    fun valueEq(): Eq<A>

    override fun Maybe<A>.eqv(b: Maybe<A>): Boolean = when (this) {
        is Maybe.Just -> when (b) {
            Maybe.Nothing -> false
            is Maybe.Just -> valueEq().run { value.eqv(b.value) }
        }
        Maybe.Nothing -> when (b) {
            Maybe.Nothing -> true
            is Maybe.Just -> false
        }
    }
}

fun <A> Maybe.Companion.eq(valueEq: Eq<A>): MaybeEq<A> =
    object : MaybeEq<A> {
        override fun valueEq(): Eq<A> = valueEq
    }

fun <A> Maybe<A>.eqv(valueEq: Eq<A>, b: Maybe<A>): Boolean = Maybe.eq(valueEq).run {
    this@eqv.eqv(b)
}

fun <A> Maybe<A>.neqv(valueEq: Eq<A>, b: Maybe<A>): Boolean = Maybe.eq(valueEq).run {
    this@neqv.neqv(b)
}

//------------------------------
// Monad
//------------------------------

interface MaybeMonad : Monad<ForMaybe> {
    override fun <A, B> MaybeOf<A>.binding(f: (A) -> MaybeOf<B>): Maybe<B> = fix().binding(f)
}

fun Maybe.Companion.monad(): MaybeMonad =
    object : MaybeMonad {}

infix fun <A, B> MaybeOf<A>.binding(f: Function1<A, MaybeOf<B>>): Maybe<B> = Maybe.monad().run {
    this@binding.binding(f)
}

//------------------------------
// Functor
//------------------------------

interface MaybeFunctor : Functor<ForMaybe> {
    override fun <A, B> MaybeOf<A>.fmap(f: (A) -> B): Maybe<B> = fix().fmap(f)
}

fun Maybe.Companion.functor(): MaybeFunctor =
    object : MaybeFunctor {}

infix fun <A, B> MaybeOf<A>.fmap(f: (A) -> B): Maybe<B> = Maybe.functor().run {
    this@fmap.fmap(f)
}