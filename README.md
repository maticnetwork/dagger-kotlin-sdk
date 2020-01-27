# Dagger Kotlin SDK

Dagger client for Kotlin

#### Installation

Add Jitpack to your project level build.gradle file

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Add the dependency in your app module's build.gradle file:

[![](https://jitpack.io/v/maticnetwork/dagger-java-sdk.svg)](https://jitpack.io/#maticnetwork/dagger-java-sdk)

```
dependencies {
    implementation "com.github.maticnetwork:dagger-java-sdk:$latest_version"
}
```

#### Getting Started

```
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
                    println("Connected: ${dagger.isConnected()}, Subscriptions: ${dagger.getAllSubscriptions()}")
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
```
