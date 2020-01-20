# Dagger Java SDK

Dagger client for Java

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

```
dependencies {
    implementation 'com.github.maticnetwork:dagger-java-sdk:Tag'
}
```

#### Getting Started

```java
// Import Matic SDK
import network.matic.dagger.Dagger;
import network.matic.dagger.Options;
import network.matic.dagger.Listener;
import network.matic.dagger.exceptions.DaggerException;


public class Main {
    public static void main(String[] args) throws DaggerException {
        Options options = new Options();
        options.setCallback(new Callback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost. Reason: " +  cause);
            }
        });

        Dagger dagger = new Dagger("tcp://ropsten.dagger.matic.network", options);
        dagger.start();
        dagger.on("latest:block", new Listener() {
            @Override
            public void Callback(String topic, byte[] data) {
                System.out.println(data);
            }
        });

        // Wait and keep listening dagger events
        synchronized (dagger) {
            while(true) {
                try {
                    dagger.wait(5000);
                    System.out.println(String.format("Connected: %s, Subscriptions: %s", dagger.isConnected(), dagger.getSubscriptions()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```
