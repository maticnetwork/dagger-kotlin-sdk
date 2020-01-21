package network.matic.dagger

import com.google.inject.AbstractModule

class DependencyInjector : AbstractModule() {

    override fun configure() {
        bind(MqttOptionsHelper::class.java).to(MqttOptionsHelperImpl::class.java)
        bind(MqttClientPersistenceHelper::class.java).to(MqttClientPersistenceHelperImpl::class.java)
        bind(MqttClientHelper::class.java).to(MqttClientHelperImpl::class.java)
        bind(MqttRegexHelper::class.java).to(MqttRegexHelperImpl::class.java)
    }

}