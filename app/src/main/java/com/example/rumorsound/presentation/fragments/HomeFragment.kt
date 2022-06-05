package com.example.rumorsound.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rumorsound.databinding.FragmentHomeBinding
import com.example.rumorsound.other.Status
import com.example.rumorsound.presentation.adapters.SongAdapter
import com.example.rumorsound.presentation.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel

    private var _viewBinding: FragmentHomeBinding? = null
    private val viewBinding
        get() = _viewBinding ?: throw RuntimeException("FragmentHomeBinding = null ")

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setUpMainViewModel()
        subscribeToObservers()

    }

    private fun setUpMainViewModel() {
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun setupRecyclerView() = viewBinding.rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    viewBinding.allSongsProgressBar.isVisible = false
                    Log.d("MusicLoad", "Success: ${result.data.toString()}")
                    result.data?.let { listOfSongs ->
                        songAdapter.listOfSongs = listOfSongs
                    }
                }
                Status.ERROR -> {
                    Log.d("MusicLoad", "ERROR: ${result.data.toString()}")
                    Unit
                }
                Status.LOADING -> viewBinding.allSongsProgressBar.isVisible = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }
}