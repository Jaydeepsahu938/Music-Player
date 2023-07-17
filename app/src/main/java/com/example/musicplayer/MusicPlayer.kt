package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.ActivityMusicPlayerBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class MusicPlayer : AppCompatActivity(),itemClicked {
    private val mBinding by lazy {
           ActivityMusicPlayerBinding.inflate(LayoutInflater.from(this))
    }
    private var mediaPlayer:MediaPlayer?=null
    private lateinit var musicList: MutableList<Music>
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: Music_Adapter
    private lateinit var recyclerView: RecyclerView
    private var currPosition:Int=0
    private var state=false
    private lateinit var seekBar:SeekBar

    lateinit var fab_play: FloatingActionButton
    lateinit var fab_next: FloatingActionButton
    lateinit var fab_prev: FloatingActionButton
    lateinit var past_time:TextView
    lateinit var remains_time:TextView
    companion object{
        private const val REQUEST_CODE_READ_MEDIA_AUDIO=1
    }
    @SuppressLint("SuspiciousIndentation", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        musicList= mutableListOf()
        if(Build.VERSION.SDK_INT>=23)
        checkPermission()


        fab_play=mBinding.fabPlay
        fab_play.setOnClickListener{
            play(currPosition)
        }
        fab_next=mBinding.fabNext
        fab_next.setOnClickListener{

            mediaPlayer?.stop()
            state=false
            if(currPosition<musicList.size-1)
            currPosition+=1
            play(currPosition)

        }

        fab_prev=mBinding.fabPrevious
        fab_prev.setOnClickListener{
            mediaPlayer?.stop()
            state=false
            if(currPosition>0)
                currPosition-=1
            play(currPosition)
        }
        seekBar=mBinding.seekBar
        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
              if(fromUser){
                  mediaPlayer?.seekTo(progress*1000)
              }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

    }
    fun play(currPosition: Int){

        if(!state){
            fab_play.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
            state=true

               mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(applicationContext, Uri.parse( musicList[currPosition].songUri))
                prepare()
                start()
            }
            val mHandler=Handler()
            this@MusicPlayer.runOnUiThread(object :Runnable{
                override fun run() {
                    val playerPosition=mediaPlayer?.currentPosition!!/1000
                    val totalDuration=mediaPlayer?.duration!!/1000
                    seekBar.max=totalDuration
                    seekBar.progress=playerPosition

                    past_time=mBinding.pastTime
                    past_time.text=timerFormat(playerPosition.toLong())
                    remains_time=mBinding.remainsString
                    remains_time.text=timerFormat((totalDuration-playerPosition).toLong())
                    mHandler.postDelayed(this,1000)

                }

            })
        }
        else{
            state=false
            mediaPlayer?.stop()
            fab_play.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow))
        }

    }
    private fun timerFormat(time:Long):String{

        val result= String.format("%02d:%02d",TimeUnit.SECONDS.toMinutes(time),
        TimeUnit.SECONDS.toSeconds(time)-TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time)))

        var convert=""
        for(i in 0 until result.length)
            convert+=result[i]

        return convert
    }
   private fun getSongs(){
       val selection=MediaStore.Audio.Media.IS_MUSIC
       val projection= arrayOf(
           MediaStore.Audio.Media.ARTIST,
           MediaStore.Audio.Media.TITLE,
           MediaStore.Audio.Media.DATA
       )
        val cursor: Cursor?=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection,null,null)
       while (cursor!!.moveToNext()) {
          musicList.add( Music(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
       }
       cursor.close()
       linearLayoutManager=LinearLayoutManager(this)
       adapter= Music_Adapter(musicList,this)
       recyclerView=mBinding.recyclerview
       recyclerView.layoutManager=linearLayoutManager
       recyclerView.adapter=adapter
   }
    private fun checkPermission(){

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED){
             getSongs()
        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_MEDIA_AUDIO)) {
                Toast.makeText(this, "Music Player need Acess your Files", Toast.LENGTH_SHORT)
                    .show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                REQUEST_CODE_READ_MEDIA_AUDIO)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode){
          REQUEST_CODE_READ_MEDIA_AUDIO ->
               if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getSongs() }
              else{
                Toast.makeText(this,"Permission Is Not Granted",Toast.LENGTH_SHORT).show() }
            else-> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun itemClicked(position: Int) {
        state=false
        mediaPlayer?.stop()
        this.currPosition=position
        play(currPosition)
    }
}