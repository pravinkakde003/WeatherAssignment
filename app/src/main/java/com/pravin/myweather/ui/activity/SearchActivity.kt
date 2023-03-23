package com.pravin.myweather.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pravin.myweather.R
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.databinding.ActivitySearchBinding
import com.pravin.myweather.ui.viewmodel.SearchCityViewModel
import com.pravin.myweather.utils.AppConstant.RESPONSE_OBJECT_KEY
import com.pravin.myweather.utils.hideKeyboard
import com.pravin.myweather.utils.positiveButtonClick
import com.pravin.myweather.utils.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val searchCityViewModel: SearchCityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            lifecycleOwner = this@SearchActivity
            viewModel = searchCityViewModel
        }
        initView()
        initObserver()
        binding.searchTextView.setOnClickListener {
            hideKeyboard(binding.root)
            searchCityViewModel.getWeatherDataForCity(binding.searchEditText.text.toString())
        }

        binding.layoutResult.setOnClickListener {
            val intent = Intent()
            intent.putExtra(RESPONSE_OBJECT_KEY, searchCityViewModel.getApiResponseObject())
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.searchEditText.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(binding.root)
                searchCityViewModel.getWeatherDataForCity(binding.searchEditText.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun initView() {
        setVisibility(View.GONE)
        val actionBar = supportActionBar
        actionBar?.let {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = resources.getString(R.string.search)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setVisibility(visibility: Int) {
        binding.textSearchResult.visibility = visibility
        binding.layoutResult.visibility = visibility
    }

    private fun initObserver() {
        lifecycleScope.launchWhenCreated {
            searchCityViewModel.weatherResponseLiveData.observe(this@SearchActivity) { responseData ->
                binding.progressBar.visibility = View.GONE
                when (responseData) {
                    is NetworkResult.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is NetworkResult.Error -> {
                        showAlertDialog {
                            setTitle(context.resources.getString(R.string.error))
                            setMessage(responseData.errorMessage?.message)
                            positiveButtonClick(context.resources.getString(R.string.ok)) { }
                        }
                    }
                    is NetworkResult.Success -> {
                        responseData.data?.let {
                            val apiResponse = responseData.data
                            if (null != apiResponse) {
                                searchCityViewModel.setData(apiResponse)
                                setVisibility(View.VISIBLE)
                            } else {
                                showAlertDialog {
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