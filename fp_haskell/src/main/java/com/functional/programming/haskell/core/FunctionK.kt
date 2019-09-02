package com.functional.programming.haskell.core

import com.functional.programming.haskell.typeclasses.Functor
import com.functional.programming.haskell.typeclasses.Kind
import com.functional.programming.haskell.typeclasses.Kind2

class ForFunction1K private constructor() {
    companion object
}

typealias Function1KOf<A, B> = Kind2<ForFunction1K, A, B> /* = Kind<Kind<ForFunction1K, A>, B> */

private typealias Function1PartialOf<A> = Kind<ForFunction1K, A>

private typealias Function1<A, B> = (A) -> B

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
private inline fun <A, B> Function1KOf<A, B>.fix(): Function1K<A, B> =
    this as Function1K<A, B>

private fun <A, B> Function1<A, B>.k(): Function1K<A, B> = Function1K(this)

private fun <A, B> Function1<A, B>.kk(): Function1KOf<A, B> = Function1K(this)

// 通过扩展方法, 把函数调用操作符 `()` 添加到 Function1KOf 类型上, 使之跟普通方法一样.
// 注释它的原因是我用了代理模式  `data class Function1K<A, out B>(val f: Function1<A, B>) : Function1<A, B> by f`
// operator fun <A, B> Function1KOf<A, B>.invoke(a: A): B = this.fix().f(a)

data class Function1K<A, out B>(val f: Function1<A, B>) :
    Function1<A, B> by f,
    Function1KOf<A, B> {

    companion object {}

    // ((A) -> B).fmap((B) -> C)
    // https://www.kotlincn.net/docs/reference/inline-functions.html#non-local-returns
    internal inline fun <C> fmap(crossinline g: (B) -> C): Function1K<A, C> =
        ({ a: A -> g(f(a)) }).k()
}


//------------------------------
// Functor
//------------------------------


interface Function1KFunctor<A> : Functor<Function1PartialOf<A>> {
    override fun <B, C> Function1KOf<A, B>.fmap(g: (B) -> C): Function1K<A, C> = fix().fmap(g)
}

fun <A> Function1K.Companion.functor(): Function1KFunctor<A> =
    object : Function1KFunctor<A> {}


infix fun <A, B, C> Function1KOf<A, B>.fmap(g: (B) -> C): Function1K<A, C> =
    Function1K.functor<A>().run {
        this@fmap.fmap<B, C>(g)
    }

infix fun <A, B, C> Function1<A, B>.fmap(g: (B) -> C): Function1<A, C> =
    Function1K.functor<A>().run {
        this@fmap.kk<A, B>().fmap<B, C>(g)
    }
