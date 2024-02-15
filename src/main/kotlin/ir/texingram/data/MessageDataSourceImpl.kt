package ir.texingram.data

import ir.texingram.data.model.Message
import org.litote.kmongo.coroutine.CoroutineDatabase

class MessageDataSourceImpl(private val db: CoroutineDatabase) : MessageDataSource {

    private val messages = db.getCollection<Message>()
    override suspend fun getAll(): List<Message> =
        messages.find().descendingSort(Message::timestamp).toList()

    override suspend fun insert(message: Message) {
        messages.insertOne(message)
    }
}