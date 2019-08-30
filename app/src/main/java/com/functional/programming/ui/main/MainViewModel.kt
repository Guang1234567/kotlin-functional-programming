package com.functional.programming.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.functional.programming.haskell.core.Maybe
import com.functional.programming.haskell.core.binding
import com.functional.programming.haskell.core.eq
import com.functional.programming.haskell.core.eqv
import com.functional.programming.haskell.core.fmap
import com.functional.programming.haskell.core.fmapFlip
import com.functional.programming.haskell.core.neqv

class MainViewModel : ViewModel() {

    companion object {
        const val TAG = "MainViewModel"
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun test001() {
        testMonad()
        testEq()
        testFunctor()
    }


    fun half(x: Int): Maybe<Int> =
        if (x % 2 == 0)
            Maybe.Just(x / 2)
        else
            Maybe.Nothing


    fun testMonad() {
        var result = Maybe.Just(20) binding ::half binding ::half binding ::half
        Log.d(TAG, "testMonad #1 : $result")

        result = Maybe.Just(20) binding2 ::half binding2 ::half
        Log.d(TAG, "testMonad #2 : $result")

        result = Maybe.Just(20) binding2 ::half
        Log.d(TAG, "testMonad #3 : $result")
    }

    fun testFunctor() {
        var result = Maybe.Just(20) fmap { it + 1 } fmap { it > 20 }
        Log.d(TAG, "testFunctor #1 : $result")

        result = { x: Int -> x < 20 } fmapFlip ({ x: Int -> x + 1 } fmapFlip Maybe.Just(20))
        Log.d(TAG, "testFunctor #2 : $result")

        var result1 = listOf(1, 2, 3, 4, 5, 6, 7) fmap { it + 1 }
        Log.d(TAG, "testFunctor #3 : $result1")

        // function compose   (f.g)(x) = f(g(x))
        // https://learnyoua.haskell.sg/zh-cn/ch06/high-order-function#function-composition
        val foo = { x: Int -> x + 2 } fmap { x: Int -> x + 3 }
        Log.d(TAG, "testFunctor #3 : ${foo(10)}")
    }

    fun testEq() {
        var result = Maybe.Just(20).eqv(Int.eq(), Maybe.Just(10))
        Log.d(TAG, "testEq #1 : $result")

        result = Maybe.Just(20).eqv(Int.eq(), Maybe.Just(20))
        Log.d(TAG, "testEq #2 : $result")

        result = Maybe.Just(20).eqv(Int.eq(), Maybe.Nothing)
        Log.d(TAG, "testEq #3 : $result")

        result = Maybe.Nothing.eqv(Int.eq(), Maybe.Just(20))
        Log.d(TAG, "testEq #4 : $result")

        result = Maybe.Nothing.eqv(Int.eq(), Maybe.Nothing)
        Log.d(TAG, "testEq #5 : $result")

        result = Maybe.Nothing.neqv(Int.eq(), Maybe.Nothing)
        Log.d(TAG, "testEq #6 : $result")

        result = Maybe.Nothing.neqv(Int.eq(), Maybe.Just(20))
        Log.d(TAG, "testEq #7 : $result")
    }
}
