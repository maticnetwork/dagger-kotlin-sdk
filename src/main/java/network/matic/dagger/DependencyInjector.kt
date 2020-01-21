package network.matic.dagger

import com.google.inject.AbstractModule

class DependencyInjector : AbstractModule() {

    override fun configure() {
        bind(InstanceHelper::class.java).to(InstanceHelperImpl::class.java)
    }

}
