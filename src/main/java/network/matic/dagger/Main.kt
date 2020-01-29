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

        val dagger = Dagger("tcp://ropsten.dagger.matic.network")
        dagger.start()
        dagger.on("latest:block", object : Listener {
            override fun callback(topic: String?, data: ByteArray?) {
                if (data != null) {
                    println("latest block data is ${String(data)}")
                }
            }
        })
        // Wait and keep listening dagger events
        synchronized(dagger) {
            while (true) {
                try {
                    Thread.sleep(5000)
                    println("Connected: ${dagger.isConnected()}, Subscriptions: ${dagger.getAllSubscriptions()}")
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
