package network.matic.dagger

import network.matic.dagger.exceptions.DaggerException

object Main {
    @Throws(DaggerException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val options = Options()
        options.callback = object : Callback {
            override fun connectionLost(cause: Throwable?) {
                println("Connection lost. Reason: $cause")
            }
        }
        val dagger = Dagger("tcp://ropsten.dagger.matic.network", options)
        dagger.start()
        dagger.on("latest:block", object : Listener {
            override fun callback(topic: String?, data: ByteArray?) {
                if (data != null) {
                    println(String(data))
                }
            }
        })
        // Wait and keep listening dagger events
        synchronized(dagger) {
            while (true) {
                try {
                    Thread.sleep(5000)
                    println(java.lang.String.format("Connected: %s, Subscriptions: %s", dagger.isConnected(), dagger.getAllSubscriptions()))
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
