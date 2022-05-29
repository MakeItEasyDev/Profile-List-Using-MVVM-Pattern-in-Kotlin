package com.kotlin.task.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kotlin.task.R
import com.kotlin.task.database.entity.ProfileList
import com.kotlin.task.databinding.ProfileListItemBinding

class ProfileAdapter : RecyclerView.Adapter<ProfileAdapter.MainViewHolder>() {

    private var profileList: List<ProfileList> = ArrayList()
    private var duplicateList: ArrayList<ProfileList> = ArrayList()
    private var listener: OnItemClickListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setProfileData(profileLists: List<ProfileList>, listener: OnItemClickListener) {
        this.profileList = profileLists
        this.listener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = ProfileListItemBinding.inflate(inflater, parent, false)
        return MainViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val profileList = profileList[position]

        holder.binding.userName.text = profileList.name
        holder.binding.emailId.text = profileList.email
        Glide.with(holder.itemView.context)
            .load(profileList.medium)
            .placeholder(R.drawable.default_image)
            .error(R.drawable.image_not_supported)
            .into(holder.binding.profileImage)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(profileList)
        }
    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    class MainViewHolder(val binding: ProfileListItemBinding) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<ProfileList>) {
        profileList = filterList
        notifyDataSetChanged()
    }
}

interface OnItemClickListener {
    fun onItemClick(position: ProfileList)
}