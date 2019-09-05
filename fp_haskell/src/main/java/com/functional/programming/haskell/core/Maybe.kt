package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Eq
import com.functional.programming.haskell.typeclasses.Functor
import com.functional.programming.haskell.typeclasses.Kind
import com.functional.programming.haskell.typeclasses.Monad
import com.functional.programming.haskell.typeclasses.Monad2

// or `typealias ForMaybe = KClass<Maybe<Any>>` but only work for JVM platform
// or `object ForMaybe` but `ForMaybe` can not have `companion object`
class ForMaybe private constructor() {
    companion object
}

typealias MaybeOf<A> = Kind<ForMaybe, A>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
private inline fun <A> MaybeOf<A>.fix(): Maybe<A> =
    this as Maybe<A>

sealed class Maybe<out A> : MaybeOf<A>, Monad2<ForMaybe, A> {

    companion object

    object Nothing : Maybe<kotlin.Nothing>() {
        override fun toString(): String = "Maybe.Nothing"
    }

    data class Just<out A>(val value: A) : Maybe<A>() {
        override fun toString(): String = "Maybe.Just($value)"
    }

    internal inline fun <B> fmap(f: (A) -> B): Maybe<B> = when (this) {
        is Nothing -> this
        is Just -> Just(f(value))
    }

    /*
    internal inline fun <B> MaybeOf<MaybeOf<out B>>.flatten(): Maybe<B> = when (this) {
        is Nothing -> this
        is Just -> this.value.fix()
    }
    */

    // `binding` 又名 `flatMap` = `fmap` + `flatten`
    internal inline fun <B> binding(f: (A) -> MaybeOf<B>): Maybe<B> = when (this) {
        is Nothing -> this
        is Just -> f(value).fix() /* or `fmap(f).flatten()`*/
    }

    override fun <B> binding2(f: (A) -> MaybeOf<B>): Maybe<B> = when (this) {
        is Nothing -> this
        is Just -> f(value).fix()
    }

    internal inline fun bind(): A? = when (this) {
        is Nothing -> null
        is Just -> value
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

infix fun <A, B> ((A) -> B).fmapFlip(maybe: MaybeOf<A>): Maybe<B> = maybe.fmap(this)

//------------------------------
// Monad
//------------------------------

interface MaybeMonad : Monad<ForMaybe> {
    override fun <A> just(value: A): Maybe<A> = Maybe.Just(value)

    override fun <A, B> MaybeOf<A>.binding(f: (A) -> MaybeOf<B>): Maybe<B> = fix().binding(f)

    override suspend fun <A> bind(m: MaybeOf<A>): A? = m.fix().bind()
}

fun Maybe.Companion.monad(): MaybeMonad =
    object : MaybeMonad {}

infix fun <A, B> MaybeOf<A>.binding(f: (A) -> MaybeOf<B>): Maybe<B> = Maybe.monad().run {
    this@binding.binding(f)
}

infix fun <A, B> Maybe<A>.binding(f: (A) -> MaybeOf<B>): Maybe<B> = (this as MaybeOf<A>).binding(f)