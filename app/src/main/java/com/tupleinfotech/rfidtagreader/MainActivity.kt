package com.tupleinfotech.rfidtagreader

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.tupleinfotech.rfidtagreader.Model.TagScan
import com.tupleinfotech.rfidtagreader.databinding.ActivityMainBinding
import com.tupleinfotech.rfidtagreader.databinding.FragmentTagScanBinding
import com.tupleinfotech.rfidtagreader.ui.SectionsPagerAdapter
import com.tupleinfotech.rfidtagreader.ui.TagScanFragment
import com.ubx.usdk.USDKManager
import com.ubx.usdk.USDKManager.STATUS
import com.ubx.usdk.rfid.RfidManager
import com.ubx.usdk.util.QueryMode
import com.ubx.usdk.util.SoundTool

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    var RFID_INIT_STATUS: Boolean = false
    var mRfidManager: RfidManager? = null
    var mDataParents: ArrayList<String> = arrayListOf()
    var tagScanSpinner: ArrayList<TagScan> = arrayListOf()
    private var fragments: List<Fragment> = listOf()
    var readerType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        mDataParents    = ArrayList()
        tagScanSpinner  = ArrayList()

        SoundTool.getInstance(this@MainActivity)
        initRfid()

        //        initRfidService();
        fragments = listOf(
            TagScanFragment.newInstance(this@MainActivity),
//            TagManageFragment.newInstance(this@MainActivity),
//            SettingFragment.newInstance(this@MainActivity)
        )

        val sectionsPagerAdapter = SectionsPagerAdapter(this@MainActivity,supportFragmentManager, fragments)

        binding.viewPager.setAdapter(sectionsPagerAdapter)
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    private fun initRfid() {

        USDKManager.getInstance().init(this@MainActivity) { status ->
            if (status == STATUS.SUCCESS) {
                Log.d(TAG, "initRfid()  success.")
                mRfidManager = USDKManager.getInstance().rfidManager
                (fragments[0] as TagScanFragment).setCallback()
                mRfidManager?.setOutputPower(30.toByte())

                Log.d(TAG,"initRfid: getDeviceId() = " + mRfidManager?.deviceId)

                readerType = mRfidManager?.readerType!!

                runOnUiThread {
                    Toast.makeText(this@MainActivity,"module：$readerType", Toast.LENGTH_LONG).show()
                }

                Log.d(TAG,"initRfid: GetReaderType() = $readerType")
            } else {
                Log.d(TAG, "initRfid  fail.")
            }
        }
    }

    private fun setQueryMode(mode: Int) {
        mRfidManager!!.queryMode = QueryMode.EPC_TID
    }

    private fun writeTagByTid() {
        val tid = "24 length TID"
        val writeData = "need write EPC datas "
        mRfidManager!!.writeTagByTid(tid,0.toByte(),2.toByte(),"00000000".toByteArray(),writeData)
    }

    override fun onDestroy() {
        super.onDestroy()

        SoundTool.getInstance(this@MainActivity).release()
        RFID_INIT_STATUS = false
        if (mRfidManager != null) {
            mRfidManager!!.disConnect()
            mRfidManager!!.release()

            Log.d(TAG, "onDestroyView: rfid close")
            //            System.exit(0);
        }
    }

    private fun setScanInteral(interal: Int) {
        val setScanInterval = mRfidManager!!.setScanInterval(interal)
        Log.v(TAG,"--- setScanInterval()   ----$setScanInterval")
    }

    private val scanInteral: Unit get() {
            val getScanInterval = mRfidManager!!.scanInterval
            Log.v(TAG,"--- getScanInterval()   ----$getScanInterval")
        }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == 523 && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            //TODO set scanning functionality on system key event
            println(event.keyCode)
            println(event.action)
            val fragment = TagScanFragment()

            return true
        } else if (event.keyCode == 523 && event.action == KeyEvent.ACTION_UP && event.repeatCount == 0) {
            //TODO set scanning functionality on system key event
            println(event.keyCode)
            println(event.action)

            val fragment = TagScanFragment()

            return true
        }
        return super.dispatchKeyEvent(event)
    }

    companion object {
        const val TAG: String = "usdk"
    }
}