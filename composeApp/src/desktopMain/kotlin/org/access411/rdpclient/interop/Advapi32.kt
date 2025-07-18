package org.access411.rdpclient.interop

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.W32APIOptions

interface Advapi32 : Library {
    companion object {
        val INSTANCE = Native.load("Advapi32", Advapi32::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }

    fun CredDeleteW(
        target: String,
        type: UInt,
        reservedFlag: Int
    ): Boolean

    fun CredEnumerateW(
        target: String,
        flags: UInt,
        count: IntByReference,
        credentialsPtr: PointerByReference
    ): Boolean

    fun CredReadW(
        target: String,
        type: UInt,
        reservedFlag: Int,
        credentialPtr: PointerByReference
    ): Boolean

    fun CredWriteW(
        userCredential: CredUI.NativeCredential,
        flags: UInt
    ): Boolean
}