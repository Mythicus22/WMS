package com.example.myapplication.shared.communication.di

import com.example.myapplication.shared.communication.mqtt.MqttClient
import com.example.myapplication.shared.communication.mqtt.MqttClientFactory
import com.example.myapplication.shared.communication.service.CommunicationService
import com.example.myapplication.shared.communication.service.CommunicationServiceImpl
import org.koin.dsl.module

val communicationModule = module {
    single<MqttClient> { MqttClientFactory.create() }
    single<CommunicationService> { CommunicationServiceImpl(get()) }
}
