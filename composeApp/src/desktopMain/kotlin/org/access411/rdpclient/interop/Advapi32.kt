package org.access411.rdpclient.interop

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.win32.W32APIOptions

interface Advapi32 : Library {
    companion object {
        val INSTANCE = Native.load("Advapi32", Advapi32::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }
}