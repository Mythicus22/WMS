package com.example.myapplication.shared.communication.di

import com.example.myapplication.shared.communication.mqtt.MqttClient
import com.example.myapplication.shared.communication.mqtt.MqttClientFactory
import com.example.myapplication.shared.communication.service.CommunicationService
import com.example.myapplication.shared.communication.service.CommunicationServiceImpl
import com.example.myapplication.shared.communication.transport.DirectTransport
import com.example.myapplication.shared.communication.transport.MqttTransport
import org.koin.dsl.module

val communicationModule = module {
    single<MqttClient> { MqttClientFactory.create() }
    single { MqttTransport(get()) }
    single { DirectTransport() }
    single<CommunicationService> { CommunicationServiceImpl(get(), get()) }
}
