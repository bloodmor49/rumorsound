package com.example.rumorsound.presentation

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.rumorsound.R
import com.example.rumorsound.data.exoplayer.isPlaying
import com.example.rumorsound.data.exoplayer.toSong
import com.example.rumorsound.databinding.ActivityMainBinding
import com.example.rumorsound.domain.entities.Song
import com.example.rumorsound.other.Status
import com.example.rumorsound.presentation.adapters.SwipeSongAdapter
import com.example.rumorsound.presentation.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var currentPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        subscribeToObservers()

        //#TODO Доработать navHostFragment с ViewBinding

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.vpSong.adapter = swipeSongAdapter

        binding.ivPlayPause.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navController.navigate(R.id.globalActionToSongFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.listOfSongs[position])
                } else {
                    currentPlayingSong = swipeSongAdapter.listOfSongs[position]
                }
            }
        })
    }

    private fun hideBottomBar() {
        binding.ivCurSongImage.isVisible = false
        binding.vpSong.isVisible = false
        binding.ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        binding.ivCurSongImage.isVisible = true
        binding.vpSong.isVisible = true
        binding.ivPlayPause.isVisible = true
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.listOfSongs.indexOf(song)
        if (newItemIndex != -1) {
            binding.vpSong.currentItem = newItemIndex
            currentPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { listOfSongs ->
                            swipeSongAdapter.listOfSongs = listOfSongs
                            if (listOfSongs.isNotEmpty()) {
                                glide.load((currentPlayingSong ?: listOfSongs[0]).imageUrl)
                                    .into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        mainViewModel.currentPlayingSong.observe(this) {
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
            glide.load(currentPlayingSong?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
        }

        mainViewModel.playBackState.observe(this) {
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true)
                    com.google.android.exoplayer2.ui.R.drawable.exo_icon_pause
                else
                    com.google.android.exoplayer2.ui.R.drawable.exo_icon_play
            )
        }
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(binding.rootLayout,
                        result.message ?: "Неизвестная ошибка", Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }
    }
}
