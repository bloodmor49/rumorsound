package com.example.rumorsound.presentation.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.rumorsound.data.exoplayer.isPlaying
import com.example.rumorsound.data.exoplayer.toSong
import com.example.rumorsound.databinding.FragmentSongBinding
import com.example.rumorsound.domain.entities.Song
import com.example.rumorsound.other.Status
import com.example.rumorsound.presentation.viewmodels.MainViewModel
import com.example.rumorsound.presentation.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SongFragment : Fragment() {

    //    @Inject
    //    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var currentPlayingSong: Song? = null

    private var playBackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    private val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    private var _viewBinding: FragmentSongBinding? = null
    private val viewBinding
        get() = _viewBinding ?: throw RuntimeException("FragmentSongBinding = null ")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _viewBinding = FragmentSongBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMainViewModel()
        subscribeToObservers()

    }

    private fun setUpMainViewModel() {
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewBinding.ivPlayPauseDetail.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
        viewBinding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        viewBinding.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
        viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurrentPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = song.title
        viewBinding.tvSongName.text = title
        //#TODO: Добавить картинку
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { listOfSongs ->
                            if (currentPlayingSong == null && listOfSongs.isNotEmpty()) {
                                currentPlayingSong = listOfSongs[0]
                                updateTitleAndSongImage(listOfSongs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.currentPlayingSong.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
            updateTitleAndSongImage(currentPlayingSong!!)
        }
        mainViewModel.playBackState.observe(viewLifecycleOwner) {
            playBackState = it
            viewBinding.ivPlayPauseDetail.setImageResource(
                if (playBackState?.isPlaying == true)
                    com.google.android.exoplayer2.ui.R.drawable.exo_icon_pause
                else
                    com.google.android.exoplayer2.ui.R.drawable.exo_icon_play
            )
            viewBinding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.currentPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekBar) {
                viewBinding.seekBar.progress = it.toInt()
                setCurrentPlayerTimeToTextView(it)
            }
        }
        songViewModel.currentSongDuration.observe(viewLifecycleOwner) {
            viewBinding.seekBar.max = it.toInt()
            viewBinding.tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurrentPlayerTimeToTextView(ms: Long) {
        viewBinding.tvCurTime.text = dateFormat.format(ms)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }
}