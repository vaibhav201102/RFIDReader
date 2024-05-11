package com.tupleinfotech.rfidtagreader

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tupleinfotech.rfidtagreader.Model.TagScan
import com.tupleinfotech.rfidtagreader.databinding.ActivityMainBinding
import com.tupleinfotech.rfidtagreader.databinding.ActivityRfidreaderBinding
import com.tupleinfotech.rfidtagreader.ui.ScanListAdapterRv
import com.tupleinfotech.rfidtagreader.ui.TagScanFragment
import com.ubx.usdk.USDKManager
import com.ubx.usdk.rfid.RfidManager
import com.ubx.usdk.rfid.aidl.IRfidCallback
import com.ubx.usdk.util.QueryMode
import com.ubx.usdk.util.SoundTool
import java.util.Locale

@SuppressLint("SetTextI18n","HandlerLeak","NotifyDataSetChanged")
class RFIDReaderActivity : AppCompatActivity() {

    private var _binding: ActivityRfidreaderBinding? = null
    private val binding get() = _binding!!
    var mDataParents: ArrayList<String> = arrayListOf()
    var tagScanSpinner: ArrayList<TagScan> = arrayListOf()
    var readerType: Int = 0
    var RFID_INIT_STATUS: Boolean = false
    var mRfidManager: RfidManager? = null

    private var data: ArrayList<TagScan> = arrayListOf()
    private var mapData: HashMap<String, TagScan> = hashMapOf()
    private var callback: ScanCallback? = null
    private var scanListAdapterRv: ScanListAdapterRv = ScanListAdapterRv()
    private var tagTotal = 0
    private val MSG_UPDATE_UI = 0
    private var time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRfidreaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDataParents    = ArrayList()
        tagScanSpinner  = ArrayList()

        binding.scanListRv.setLayoutManager(
            LinearLayoutManager(this,
                RecyclerView.VERTICAL,false)
        )
        binding.scanListRv.addItemDecoration(
            DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL)
        )

        scanListAdapterRv = ScanListAdapterRv()
        binding.scanListRv.setAdapter(scanListAdapterRv)

        SoundTool.getInstance(this@RFIDReaderActivity)
        initRfid()
        initStartScanButton()
        initCheckBox()
        initClearButton()
    }

    private fun initCheckBox(){
        binding.checkBox.setOnCheckedChangeListener { _, b ->
            if (mRfidManager != null) {
                if (b) {
                    mRfidManager!!.queryMode = QueryMode.EPC_TID
                } else {
                    mRfidManager!!.queryMode = QueryMode.EPC
                }
            }
        }
    }

    private fun initClearButton(){
        binding.scanClearBtn.setOnClickListener {
            if (binding.scanStartBtn.getText() == getString(R.string.btInventory)) {
                initClearData()
            }
        }
    }

    private fun initClearData(){
        tagTotal = 0
        binding.scanCountText.text = "0"
        binding.scanTotalText.text = "0"
        mapData.clear()
        mDataParents.clear()
        tagScanSpinner.clear()
        data.clear()
        scanListAdapterRv.updateList(data)
    }

    private fun initStartScanButton(){

        binding.scanStartBtn.setOnClickListener {
            if (RFID_INIT_STATUS) {
                if (binding.scanStartBtn.getText() == getString(R.string.btInventory)) {
                    setCallback()
                    binding.scanStartBtn.text = getString(R.string.btn_stop_Inventory)
                    setScanStatus(true)
                }
                else {
                    binding.scanStartBtn.text = getString(R.string.btInventory)
                    setScanStatus(false)
                    binding.scanClearBtn.performClick()
                }
            } else {
                Log.d(TagScanFragment.TAG,"scanStartBtn  RFID ")
                Toast.makeText(this@RFIDReaderActivity, "RFID Not initialized", Toast.LENGTH_SHORT).show()
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
            mRfidManager!!.startInventory(0.toByte())
        } else {
            Log.v(TAG, "--- stopInventory()   ----")
            mRfidManager!!.stopInventory()
            handlerStopUI()
        }
    }

    private fun handlerUpdateUI() {
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 1000)
    }

    private fun handlerStopUI() {
        mHandler.removeCallbacksAndMessages(null)
    }

    internal inner class ScanCallback : IRfidCallback {
        override fun onInventoryTag(EPC: String, TID: String, strRSSI: String) {
            notiyDatas(EPC, TID, strRSSI)
            Log.d("EPC", EPC)
        }

        override fun onInventoryTagEnd() {
            Log.d(TagScanFragment.TAG, "onInventoryTag()")
        }
    }

    private fun notiyDatas(s2: String, TID: String, strRSSI: String) {

        val mapContainStr = if (!TextUtils.isEmpty(TID)) TID else s2

        Log.d(TAG, "onInventoryTag: EPC: $s2")
//        SoundTool.getInstance(BaseApplication.context).playBeep(1)
        runOnUiThread(object : Runnable {
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
                        mDataParents.add(s2.toString())

                        val tagScan = TagScan(strRSSI,s2, TID, 1)
                        mapData[mapContainStr] = tagScan
                        tagScanSpinner.add(tagScan)
                    }
                    binding.scanTotalText.text = (++tagTotal).toString() + ""

                    val nowTime = System.currentTimeMillis()
                    if ((nowTime - time) > 1000) {
                        time = nowTime
                        data = ArrayList<TagScan>(mapData.values)
                        Log.d(TagScanFragment.TAG,"onInventoryTag: data = " + data.toTypedArray().contentToString())
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

    override fun setVisible(visible: Boolean) {
        super.setVisible(visible)
        if (visible) {
            setCallback()
        }
    }

    fun setCallback() {
        if (mRfidManager != null) {
            if (callback == null) {
                callback = ScanCallback()
            }
            mRfidManager!!.registerCallback(callback)
        }
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            if (mRfidManager != null) {
                Log.v(TagScanFragment.TAG, "--- getFirmwareVersion()   ----")
                RFID_INIT_STATUS = true
                val firmware = mRfidManager!!.firmwareVersion
                binding.textFirmware.text = getString(R.string.firmware) + firmware
            } else {
                Log.v(TagScanFragment.TAG,"onStart()  --- getFirmwareVersion()   ----  mActivity.mRfidManager == null")
            }
        }, 5000)


    }
    private fun initRfid() {

        USDKManager.getInstance().init(this@RFIDReaderActivity) { status ->
            if (status == USDKManager.STATUS.SUCCESS) {
                Log.d(MainActivity.TAG, "initRfid()  success.")
                mRfidManager = USDKManager.getInstance().rfidManager
                setCallback()
                mRfidManager?.setOutputPower(30.toByte())

                Log.d(MainActivity.TAG,"initRfid: getDeviceId() = " + mRfidManager?.deviceId)

                readerType = mRfidManager?.readerType!!

                runOnUiThread {
                    Toast.makeText(this@RFIDReaderActivity,"module：$readerType", Toast.LENGTH_LONG).show()
                }

                Log.d(MainActivity.TAG,"initRfid: GetReaderType() = $readerType")
            } else {
                Log.d(MainActivity.TAG, "initRfid  fail.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        SoundTool.getInstance(this@RFIDReaderActivity).release()
        RFID_INIT_STATUS = false
        if (mRfidManager != null) {
            mRfidManager!!.disConnect()
            mRfidManager!!.release()

            Log.d(MainActivity.TAG, "onDestroyView: rfid close")
            //            System.exit(0);
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == 523 && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            //TODO set scanning functionality on system key event
            println(event.keyCode)
            println(event.action)
            binding.scanStartBtn.performClick()

            return true
        }
        else if (event.keyCode == 523 && event.action == KeyEvent.ACTION_UP && event.repeatCount == 0) {
            //TODO set scanning functionality on system key event
            println(event.keyCode)
            println(event.action)


            return true
        }
        return super.dispatchKeyEvent(event)
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