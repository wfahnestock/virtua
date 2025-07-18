package org.access411.rdpclient.interop

//import platform.posix.*

class Win32Facade {
    private val advapi32 = Advapi32.INSTANCE
    private val credUi = CredUI.INSTANCE

    init {
        println("Win32Facade initialized")
        println("Advapi32: $advapi32")
        println("CredUI: $credUi")
        println("Done!")
    }
}