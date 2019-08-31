package com.functional.programming.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import arrow.fx.IO
import arrow.fx.extensions.fx
import com.functional.programming.R

class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"

        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private val viewModel_arrow_kt: Arrow_kt_ViewModel by lazy {
        ViewModelProviders.of(this).get(Arrow_kt_ViewModel::class.java)
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

        viewModel_arrow_kt.apply {
            greet(
                callBackOnUI01 = {
                    IO.fx {
                        Log.d(TAG, "Update UI #01 currentThread(${Thread.currentThread()}) : $it")
                        Unit
                    }
                },
                callBackOnUI02 = {Log.d(TAG, "Update UI #02 currentThread(${Thread.currentThread()}) : $it")}
            )
        }
    }

}
