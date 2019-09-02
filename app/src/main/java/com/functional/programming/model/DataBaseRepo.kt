package com.functional.programming.model

import android.util.Log
import arrow.effects.IO
import arrow.effects.instances.io.async.async
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.asCoroutineDispatcher

typealias OnDataBaseChangedListener = (List<String>) -> IO<Unit>

class DataBaseRepo {

    companion object {
        const val TAG: String = "DataBaseRepo"
    }


    private val mUserList: MutableList<String> by lazy {
        mutableListOf<String>()
    }

    private val mListeners: MutableList<OnDataBaseChangedListener> by lazy {
        mutableListOf<OnDataBaseChangedListener>()
    }

    fun addListener(listener: OnDataBaseChangedListener): Boolean {
        val result = if (mListeners.contains(listener))
            false
        else
            mListeners.add(listener)

        Log.d(TAG, "# addListener : isSuccess = $result")
        return result
    }

    fun removeListener(listener: OnDataBaseChangedListener): Boolean {
        val result = mListeners.remove(listener)
        Log.d(TAG, "# removeListener : isSuccess = $result")
        return result
    }

    fun addUser(userName: String): IO<Boolean> =
        IO.async().run {
            binding {
                Log.d(TAG, "# addUser : $userName")

                // 本来这里应该用 `Dispatcher.IO`,
                // 但 `mUserList`  不是线程安全的, 故还是用回 `single()`
                // 换成真实数据库就可用回 `IO()`
                continueOn(Schedulers.single().asCoroutineDispatcher())

                val result = mUserList.add(userName)

                if (result) {
                    try {
                        continueOn(Dispatchers.Main)
                        // notify `controller` that `Model` have been changed
                        mListeners.forEach {
                            // or `bind {it.OnDataBaseChangedListener()}`
                            it(mUserList).bind() // 注意后面有个 `.bind()`
                        }
                    } catch (e: Throwable) {
                        Log.e(TAG, "# addUser", e)
                    }
                }
                result
            }
        } as IO<Boolean>
}