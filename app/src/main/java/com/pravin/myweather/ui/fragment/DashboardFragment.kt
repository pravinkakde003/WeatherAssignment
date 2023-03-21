package com.pravin.myweather.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.pravin.myweather.R
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.databinding.FragmentDashboardBinding
import com.pravin.myweather.ui.viewmodel.DashboardViewModel
import com.pravin.myweather.utils.positiveButtonClick
import com.pravin.myweather.utils.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textView.text = "Pravin"
        dashboardViewModel.getWeatherData("18.5204", "73.8567")
        collects()
    }

    private fun collects() {
        lifecycleScope.launchWhenCreated {
            dashboardViewModel.weatherResponseLiveData.observe(requireActivity()) { responseData ->
//                progressDialog.hide()
                when (responseData) {
                    is NetworkResult.Loading -> {
//                        progressDialog.show()
                    }
                    is NetworkResult.Error -> {
                        activity?.showAlertDialog {
                            setTitle(context.resources.getString(R.string.error))
                            setMessage(responseData.errorMessage?.message)
                            positiveButtonClick(context.resources.getString(R.string.ok)) { }
                        }
                    }
                    is NetworkResult.Success -> {
                        responseData.data?.let {
                            val apiResponse = responseData.data
                            if (null != apiResponse) {

                                Log.e("TAGG", Gson().toJson(apiResponse))
//                                if (apiResponse.data.isNotEmpty()) {
//                                    val preparedList =
//                                        daySalesReconViewModel.getPreparedItemList(apiResponse.data)
//                                    dayReconRecyclerViewAdapter.items = preparedList
//                                } else {
//                                    binding.withDataLayout.visibility = View.GONE
//                                    binding.noDataLayout.visibility = View.VISIBLE
//                                }
                            } else {
                                activity?.showAlertDialog {
                                    setTitle(context.resources.getString(R.string.error))
                                    setMessage(resources.getString(R.string.generic_error_message))
                                    positiveButtonClick(context.resources.getString(R.string.ok)) { }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}