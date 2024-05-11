package com.tupleinfotech.rfidtagreader.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tupleinfotech.rfidtagreader.R

/**
 * @Author: athulyatech
 * @Date: 5/10/24
 */
class SectionsPagerAdapter(context: Context, fm: FragmentManager?, data: List<Fragment>?) :
    FragmentPagerAdapter(fm!!) {
    private val mContext = context
    private val data: List<Fragment>? = data

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return data!![position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mContext.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return data?.size ?: 0
    }

    companion object {
        @StringRes
        private val TAB_TITLES =
            intArrayOf(R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3)
    }
}