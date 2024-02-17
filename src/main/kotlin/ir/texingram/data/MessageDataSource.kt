package ir.texingram.data

import ir.texingram.data.model.Message

interface MessageDataSource {

    suspend fun getAll(): List<Message>
    suspend fun insert(message: Message)
}