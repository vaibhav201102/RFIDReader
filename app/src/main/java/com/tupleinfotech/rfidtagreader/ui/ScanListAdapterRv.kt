package com.tupleinfotech.rfidtagreader.ui

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tupleinfotech.rfidtagreader.Model.TagScan
import com.tupleinfotech.rfidtagreader.R
import com.tupleinfotech.rfidtagreader.databinding.TagScanItemBinding

/**
 * @Author: athulyatech
 * @Date: 5/9/24
 */

@SuppressLint("NotifyDataSetChanged","SetTextI18n")
class ScanListAdapterRv : RecyclerView.Adapter<ScanListAdapterRv.ViewHolder?>() {

    private var tagScanList: List<TagScan> = listOf()

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with(holder){
            with(tagScanList[position]){
                holder.refreshView(position, tagScanList[position])
                //set color
                if(position%2==0){
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,R.color.odd_row))
                }else{
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,R.color.even_row))
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TagScanItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return tagScanList.size
    }


    inner class ViewHolder(val binding: TagScanItemBinding) :     RecyclerView.ViewHolder(binding.root){
        internal fun refreshView(position: Int, data: TagScan) {
            binding.listEpcText.text = "EPC:" + data.epc
            binding.listTidText.text = "TID:" + data.tid
            if (TextUtils.isEmpty(data.tid)) {
                binding.listTidText.visibility = View.GONE
            } else {
                binding.listTidText.visibility = View.VISIBLE
            }
            binding.listTotalText.text = data.count.toString() + ""
            binding.listRssiText.text = data.rssi
        }
    }

    fun updateList(list: List<TagScan>) {
        tagScanList = list ?: emptyList()
        if (tagScanList.isNotEmpty()) notifyItemRangeChanged(0, tagScanList.size) else notifyDataSetChanged()
    }

}
