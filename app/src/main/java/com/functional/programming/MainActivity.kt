package com.functional.programming

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.functional.programming.haskell.data.Maybe
import com.functional.programming.haskell.data.eq
import com.functional.programming.haskell.data.functor
import com.functional.programming.haskell.data.monad
import com.functional.programming.ui.main.MainFragment

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }

        testMonad()
        testEq()
        testFunctor()
    }

    fun half(x: Int): Maybe<Int> = if (x % 2 == 0)
        Maybe.Just(x / 2)
    else
        Maybe.Nothing

    fun testMonad() {
        val result = Maybe.monad<Int, Int>().run {
            Maybe.Just(20) binding ::half binding ::half binding ::half
        }

        Log.d(TAG, "testMonad : $result")
    }

    fun testFunctor() {
        val result = Maybe.functor<Int, Int>().run {
            Maybe.Just(20).fmap { it + 1 }
        }

        Log.d(TAG, "testFunctor #1 : $result")

        val result = Maybe.functor<Int, Boolean>().run {
            Maybe.Just(20).fmap({ it + 1 }).fmap({ it > 20 })
        }

        Log.d(TAG, "testFunctor #1 : $result")
    }

    fun testEq() {
        var result = Maybe.eq<Int>().run {
            Maybe.Just(20).eqv(Maybe.Just(10))
        }

        Log.d(TAG, "testEq #1 : $result")

        result = Maybe.eq<Int>().run {
            Maybe.Just(20).eqv(Maybe.Just(20))
        }

        Log.d(TAG, "testEq #2 : $result")

        result = Maybe.eq<Int>().run {
            Maybe.Just(20).eqv(Maybe.Nothing)
        }

        Log.d(TAG, "testEq #3 : $result")

        result = Maybe.eq<Int>().run {
            Maybe.Nothing.eqv(Maybe.Just(20))
        }

        Log.d(TAG, "testEq #4 : $result")

        result = Maybe.eq<Int>().run {
            Maybe.Nothing.eqv(Maybe.Nothing)
        }

        Log.d(TAG, "testEq #5 : $result")
    }
}
