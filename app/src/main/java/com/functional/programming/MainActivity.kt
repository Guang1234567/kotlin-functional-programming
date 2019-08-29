package com.functional.programming

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.functional.programming.haskell.core.Maybe
import com.functional.programming.haskell.core.binding
import com.functional.programming.haskell.core.eq
import com.functional.programming.haskell.core.eqv
import com.functional.programming.haskell.core.fmap
import com.functional.programming.haskell.core.neqv
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
        var result = Maybe.Just(20) binding ::half binding ::half binding ::half
        Log.d(TAG, "testMonad #1 : $result")

        result = Maybe.Just(20) binding2 ::half binding2 ::half
        Log.d(TAG, "testMonad #2 : $result")

        result = Maybe.Just(20) binding2 ::half
        Log.d(TAG, "testMonad #3 : $result")
    }

    fun testFunctor() {
        val result = Maybe.Just(20) fmap { it + 1 } fmap { it > 20 }
        Log.d(TAG, "testFunctor #1 : $result")
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
