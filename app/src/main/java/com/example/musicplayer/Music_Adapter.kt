package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Music_Adapter(private var musicList:MutableList<Music>,private var itemClicked: itemClicked):RecyclerView.Adapter<Music_Adapter.MusicViewHolder>() {

    @Suppress("DEPRECATION")
    inner class MusicViewHolder(v: View):RecyclerView.ViewHolder(v) ,View.OnClickListener{

        private var view:View=v
        private lateinit var music:Music
        private var artistName: TextView
        private var songName:TextView

        init {
            artistName=view.findViewById(R.id.tv_artist)
            songName=view.findViewById(R.id.tv_song)

            view.setOnClickListener(this)
        }
        fun bindMusic(music: Music){
            this.music=music
            artistName.text=music.artistName
            songName.text=music.songName

        }
        override fun onClick(p0: View?) {
                 itemClicked.itemClicked(adapterPosition)
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): MusicViewHolder {

        val context =viewGroup.context
        val infater=LayoutInflater.from(context)
        val ShouldAttachToParentImmediately=false

        val view=infater.inflate(R.layout.music_items,viewGroup,ShouldAttachToParentImmediately)

        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
      return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
       val item=musicList[position]

        holder.bindMusic(item)
    }


}