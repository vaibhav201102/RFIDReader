@file:Suppress("DEPRECATION")

package com.tupleinfotech.rfidtagreader.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tupleinfotech.rfidtagreader.BaseApplication
import com.tupleinfotech.rfidtagreader.MainActivity
import com.tupleinfotech.rfidtagreader.Model.TagScan
import com.tupleinfotech.rfidtagreader.R
import com.tupleinfotech.rfidtagreader.databinding.FragmentTagScanBinding
import com.ubx.usdk.rfid.aidl.IRfidCallback
import com.ubx.usdk.util.QueryMode
import com.ubx.usdk.util.SoundTool
import java.util.Locale

/**
 * @Author: athulyatech
 * @Date: 5/9/24
 */

@SuppressLint("SetTextI18n","HandlerLeak","NotifyDataSetChanged")
class TagScanFragment : Fragment() {

    private var _binding: FragmentTagScanBinding? = null
    private val binding get() = _binding!!

    private var data: ArrayList<TagScan> = arrayListOf()
    private var mapData: HashMap<String, TagScan> = hashMapOf()
    private var callback: ScanCallback? = null
    private var scanListAdapterRv: ScanListAdapterRv = ScanListAdapterRv()
    private var tagTotal = 0
    private val MSG_UPDATE_UI = 0
    private var time = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentTagScanBinding.inflate(LayoutInflater.from(context))
        val view = binding.root

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initStartScanButton()

        mapData = HashMap()

        binding.scanListRv.setLayoutManager(LinearLayoutManager(activity,RecyclerView.VERTICAL,false))
        binding.scanListRv.addItemDecoration(DividerItemDecoration(activity,DividerItemDecoration.VERTICAL))

        scanListAdapterRv = ScanListAdapterRv()
        binding.scanListRv.setAdapter(scanListAdapterRv)

        initCheckBox()
        initClearButton()
    }

    private fun initCheckBox(){
        binding.checkBox.setOnCheckedChangeListener { _, b ->
            if (mActivity.mRfidManager != null) {
                if (b) {
                    mActivity.mRfidManager!!.queryMode = QueryMode.EPC_TID
                } else {
                    mActivity.mRfidManager!!.queryMode = QueryMode.EPC
                }
            }
        }
    }

    private fun initClearButton(){
        binding.scanClearBtn.setOnClickListener {
            if (binding.scanStartBtn.getText() == getString(R.string.btInventory)) {
                tagTotal = 0
                binding.scanCountText.text = "0"
                binding.scanTotalText.text = "0"
                mapData.clear()
                mActivity.mDataParents.clear()
                mActivity.tagScanSpinner.clear()
                data.clear()
                scanListAdapterRv.updateList(data)
            }
        }
    }

    private fun initStartScanButton(){

        binding.scanStartBtn.setOnClickListener {
            if (mActivity.RFID_INIT_STATUS) {
                if (binding.scanStartBtn.getText() == getString(R.string.btInventory)) {
                    setCallback()
                    binding.scanStartBtn.text = getString(R.string.btn_stop_Inventory)
                    setScanStatus(true)
                } else {
                    binding.scanStartBtn.text = getString(R.string.btInventory)
                    setScanStatus(false)
                }
            } else {
                Log.d(TAG,"scanStartBtn  RFID ")
                Toast.makeText(requireContext(), "RFID Not initialized", Toast.LENGTH_SHORT).show()
            }
        }
/*        binding.scanStartBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                run {
                    if (mActivity.RFID_INIT_STATUS) {
                        if (binding.scanStartBtn.getText() == getString(R.string.btInventory)) {
                            setCallback()
                            binding.scanStartBtn.text = getString(R.string.btn_stop_Inventory)
                            setScanStatus(true)
                        } else {
                            binding.scanStartBtn.text = getString(R.string.btInventory)
                            setScanStatus(false)
                        }
                    } else {
                        Log.d(TAG,"scanStartBtn  RFID ")
                        Toast.makeText(requireContext(), "RFID Not initialized", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })*/
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_UPDATE_UI -> {
                    scanListAdapterRv.notifyDataSetChanged()
                    handlerUpdateUI()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            if (mActivity.mRfidManager != null) {
                Log.v(TAG, "--- getFirmwareVersion()   ----")
                mActivity.RFID_INIT_STATUS = true
                val firmware = mActivity.mRfidManager!!.firmwareVersion
                binding.textFirmware.text = getString(R.string.firmware) + firmware
            } else {
                Log.v(TAG,"onStart()  --- getFirmwareVersion()   ----  mActivity.mRfidManager == null")
            }
        }, 5000)


    }

    private fun setScanStatus(isScan: Boolean) {
        if (isScan) {
//            tagTotal = 0
//            if (mapData.isNotEmpty()) {
//                mapData.clear()
//            }
//            if (mActivity.mDataParents.isNotEmpty()) {
//                mActivity.mDataParents.clear()
//            }
//            if (mActivity.tagScanSpinner != null) {
//                mActivity.tagScanSpinner.clear()
//            }
//            if (data.isNotEmpty()) {
//                data.clear()
//                scanListAdapterRv?.setData(data)
//                println("RFID data$data")
//            }

            Log.v(TAG, "--- startInventory()   ----")
            handlerUpdateUI()
            mActivity.mRfidManager!!.startInventory(0.toByte())
        } else {
            Log.v(TAG, "--- stopInventory()   ----")
            mActivity.mRfidManager!!.stopInventory()
            handlerStopUI()
        }
    }

    private fun handlerUpdateUI() {
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 1000)
    }

    private fun handlerStopUI() {
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun readTagOnce() {

        val data = mActivity.mRfidManager!!.readTagOnce(0.toByte(), 0.toByte())
    }

    private fun setTagMask() {
        mActivity.mRfidManager!!.setTagMask(2, 24, 16, "7020")
    }

    private fun writeTagByTid(TID: String,Mem: Byte,WordPtr: Byte,pwd: ByteArray,datas: String) {
//                String TID = "E280110C20007642903D094D";
//                 byte[] pwd = hexStringToBytes("00000000");
//                 String datas = "1111111111111111";
        val ret = mActivity.mRfidManager!!.writeTagByTid(TID, 1.toByte(), 2.toByte(), pwd, datas)
        if (ret == -6) {
            Toast.makeText(mActivity,requireContext().getString(R.string.gj_no_support),Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeEpcString(epc: String, password: String) {
        mActivity.mRfidManager!!.writeEpcString(epc, password)
    }

    internal inner class ScanCallback : IRfidCallback {
        override fun onInventoryTag(EPC: String, TID: String, strRSSI: String) {
            notiyDatas(EPC, TID, strRSSI)
            Log.d("EPC", EPC)
        }

        override fun onInventoryTagEnd() {
            Log.d(TAG, "onInventoryTag()")
        }
    }

    private fun notiyDatas(s2: String, TID: String, strRSSI: String) {

        val mapContainStr = if (!TextUtils.isEmpty(TID)) TID else s2

        Log.d(TAG, "onInventoryTag: EPC: $s2")
//        SoundTool.getInstance(BaseApplication.context).playBeep(1)
        requireActivity().runOnUiThread(object : Runnable {
            override fun run() {
                run {
                    if (mapData.containsKey(mapContainStr)) {
                        val tagScan = mapData[mapContainStr]!!
                        tagScan.count   = mapData[mapContainStr]!!.count+1
                        tagScan.tid     = TID
                        tagScan.rssi    = strRSSI
                        tagScan.epc     = s2
                        mapData[mapContainStr] = tagScan
                    } else {
                        mActivity.mDataParents.add(s2.toString())

                        val tagScan = TagScan(strRSSI,s2, TID, 1)
                        mapData[mapContainStr] = tagScan
                        mActivity.tagScanSpinner.add(tagScan)
                    }
                    binding.scanTotalText.text = (++tagTotal).toString() + ""

                    val nowTime = System.currentTimeMillis()
                    if ((nowTime - time) > 1000) {
                        time = nowTime
                        data = ArrayList<TagScan>(mapData.values)
                        Log.d(TAG,"onInventoryTag: data = " + data.toTypedArray().contentToString())
                        scanListAdapterRv.updateList(data)
                        binding.scanCountText.text = mapData.keys.size.toString() + ""
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            setCallback()
        }
    }

    fun setCallback() {
        if (mActivity.mRfidManager != null) {
            if (callback == null) {
                callback = ScanCallback()
            }
            mActivity.mRfidManager!!.registerCallback(callback)
        }
    }

    companion object {
        val TAG: String = "usdk-" + TagScanFragment::class.java.simpleName
        private var mActivity: MainActivity = MainActivity()

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ScanFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(activity: MainActivity): TagScanFragment {
            mActivity = activity
            val fragment = TagScanFragment()
            return fragment
        }

        fun hexStringToBytes(hexString: String): ByteArray {
            var hexString = hexString
            hexString = hexString.lowercase(Locale.getDefault())
            val byteArray = ByteArray(hexString.length shr 1)
            var index = 0
            for (i in hexString.indices) {
                if (index > hexString.length - 1) {
                    return byteArray
                }
                val highDit = ((hexString[index].digitToIntOrNull(16) ?: (-1 and 0xFF))).toByte()
                val lowDit = ((hexString[index + 1].digitToIntOrNull(16) ?: (-1 and 0xFF))).toByte()
                byteArray[i] = (highDit.toInt() shl 4 or lowDit.toInt()).toByte()
                index += 2
            }
            return byteArray
        }
    }

}