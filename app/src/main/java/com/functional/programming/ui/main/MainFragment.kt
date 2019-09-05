package com.functional.programming.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.functional.programming.R

class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"

        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private val demoViewModel: DemoViewModel by lazy {
        ViewModelProviders.of(this).get(DemoViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.apply {
            test001()
        }

        /*demoViewModel.apply {
            *//*greet(
                callBackOnUI01 = {
                    IO.fx {
                        Log.d(TAG, "Update UI #01 currentThread(${Thread.currentThread()}) : $it")
                        Unit
                    }
                },
                callBackOnUI02 = {
                    Log.d(
                        TAG,
                        "Update UI #02 currentThread(${Thread.currentThread()}) : $it"
                    )
                }
            )*//*

            onDataBaseChangedListener
                .subscribe {
                    Log.d(
                        TAG,
                        "Update UI #02 currentThread(${Thread.currentThread()}) : $it"
                    )
                }
                .disposeBy(disposers.onDestroy)

            addUser("小李子")
        }*/
    }
}
