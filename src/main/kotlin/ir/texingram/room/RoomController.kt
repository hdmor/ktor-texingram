package ir.texingram.room

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import ir.texingram.data.MessageDataSource
import ir.texingram.data.model.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class RoomController(private val messageDataSource: MessageDataSource) {

    private val members = ConcurrentHashMap<String, Member>()

    fun onJoin(username: String, sessionId: String, socket: WebSocketSession) {
        if (members.containsKey(username)) throw MemberAlreadyExistsException()
        members[username] = Member(username = username, sessionId = sessionId, socket = socket)
    }

    suspend fun sendMessage(senderUsername: String, message: String) {
        val messageEntity = Message(username = senderUsername, message = message, timestamp = System.currentTimeMillis())
        messageDataSource.insert(messageEntity)
        members.values.forEach { member ->
            val parseMessage = Json.encodeToString(messageEntity)
            member.socket.send(Frame.Text(parseMessage))
        }
    }

    suspend fun getAllMessages(): List<Message> = messageDataSource.getAll()

    suspend fun tryToDisconnect(username: String) {
        members[username]?.socket?.close()
        if (members.containsKey(username)) members.remove(username)
    }
}