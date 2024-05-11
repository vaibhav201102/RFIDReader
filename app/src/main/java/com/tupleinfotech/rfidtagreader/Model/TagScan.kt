package com.tupleinfotech.rfidtagreader.Model

/**
 * @Author: athulyatech
 * @Date: 5/9/24
 */

data class TagScan(
    var rssi: String? = null,
    var epc: String? = null,
    var tid: String? = null,
    var count: Int = 0
)
