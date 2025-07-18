package org.access411.rdpclient.interop

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.WString
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.W32APIOptions

interface CredUI : Library {

    companion object {
        val INSTANCE by lazy { Native.load("credui", CredUI::class.java, W32APIOptions.DEFAULT_OPTIONS) }
    }

    fun CredUIParseUserName(
        pszUserName: WString,    // Input username string to parse
        user: CharArray,        // Buffer to receive the username portion
        userMaxChars: Int,      // Size of username buffer
        domain: CharArray,      // Buffer to receive the domain portion
        domainMaxChars: Int     // Size of domain buffer
    ): Int

    fun CredPackAuthenticationBuffer(
        dwFlags: Int,
        pszUserName: WString,
        pszPassword: WString,
        pPackedCredentials: Pointer?,
        pcbPackedCredentials: IntByReference
    ): Boolean

    fun CredUnPackAuthenticationBuffer(
        dwFlags: Int,
        pAuthBuffer: Pointer,
        cbAuthBuffer: UInt,
        pszUserName: CharArray,
        pcchMaxUserName: IntByReference,
        pszDomainName: CharArray,
        pcchMaxDomainName: IntByReference,
        pszPassword: CharArray,
        pcchMaxPassword: IntByReference
    ): Boolean

    fun CredUIPromptForWindowsCredentials(
        creditUR: CredentialUIInfo.ByReference,
        authError: Int,
        authPackage: IntByReference,
        inAuthBuffer: Pointer?,
        inAuthBufferSize: Int,
        refOutAuthBuffer: PointerByReference,
        refOutAuthBufferSize: WinDef.UINTByReference,
        fSave: WinDef.BOOLByReference,
        flags: Int
    ): Int

    fun CredUICmdLinePromptForCredentials(
        targetName: WString,
        reserved: Pointer,
        iError: Int,
        userName: CharArray,
        maxUserName: Int,
        password: CharArray,
        maxPassword: Int,
        pfSave: WinDef.BOOLByReference,
        flags: Int
    ): Int

    open class CredentialUIInfo : Structure() {
        @JvmField var cbSize: Int = 0
        @JvmField var hwndParent: Pointer? = null
        @JvmField var pszMessageText: WString? = null
        @JvmField var pszCaptionText: WString? = null
        @JvmField var hbmBanner: Pointer? = null

        class ByReference : CredentialUIInfo(), Structure.ByReference

        override fun getFieldOrder(): List<String> {
            return listOf(
                "cbSize",
                "hwndParent",
                "pszMessageText",
                "pszCaptionText",
                "hbmBanner"
            )
        }
    }

    open class NativeCredential : Structure() {
        @JvmField var Flags: Long = 0
        @JvmField var Type: Long = 0
        @JvmField var TargetName: WString? = null
        @JvmField var Comment: WString? = null
        @JvmField var LastWritten: WinBase.FILETIME = WinBase.FILETIME()
        @JvmField var CredentialBlobSize: Long = 0
        @JvmField var CredentialBlob: Pointer? = null
        @JvmField var Persist: Long = 0
        @JvmField var AttributeCount: Long = 0
        @JvmField var Attributes: Pointer? = null
        @JvmField var TargetAlias: WString? = null
        @JvmField var UserName: WString? = null

        class ByReference : NativeCredential(), Structure.ByReference

        override fun getFieldOrder(): List<String> {
            return listOf(
                "Flags",
                "Type",
                "TargetName",
                "Comment",
                "LastWritten",
                "CredentialBlobSize",
                "CredentialBlob",
                "Persist",
                "AttributeCount",
                "Attributes",
                "TargetAlias",
                "UserName"
            )
        }
    }

    object CredentialHelper {
        fun promptForCredentials(caption: String, message: String): NetworkCredential? {
            val credUI = INSTANCE

            // Setup the UI info
            val uiInfo = CredentialUIInfo.ByReference()
            uiInfo.pszCaptionText = WString(caption)
            uiInfo.pszMessageText = WString(message)
            uiInfo.cbSize = uiInfo.size()

            // Output parameters
            val authPackage = IntByReference()
            val outCredBuffer = PointerByReference()
            val outCredSize = WinDef.UINTByReference()
            val save = WinDef.BOOLByReference()

            // Show credential dialog
            val result = credUI.CredUIPromptForWindowsCredentials(
                uiInfo, 0, authPackage, null, 0,
                outCredBuffer, outCredSize, save, 0
            )

            if (result != 0) {
                return null // User canceled or error
            }

            // Unpack the authentication buffer
            val maxUsername = 100
            val maxDomain = 100
            val maxPassword = 100

            val usernameBuffer = CharArray(maxUsername)
            val domainBuffer = CharArray(maxDomain)
            val passwordBuffer = CharArray(maxPassword)

            val usernameSize = IntByReference(maxUsername)
            val domainSize = IntByReference(maxDomain)
            val passwordSize = IntByReference(maxPassword)

            val success = credUI.CredUnPackAuthenticationBuffer(
                0,
                outCredBuffer.value,
                outCredSize.value.toByte().toUInt(),
                usernameBuffer,
                usernameSize,
                domainBuffer,
                domainSize,
                passwordBuffer,
                passwordSize
            )

            return if (success) {
                // Convert char arrays to strings and create NetworkCredential
                NetworkCredential(
                    userName = usernameBuffer.copyOf(usernameSize.value).concatToString(),
                    password = passwordBuffer.copyOf(passwordSize.value).concatToString(),
                    domain = domainBuffer.copyOf(domainSize.value).concatToString()
                )
            } else {
                null
            }
        }
    }
}