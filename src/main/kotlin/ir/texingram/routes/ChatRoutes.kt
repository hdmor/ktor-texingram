package ir.texingram.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import ir.texingram.room.MemberAlreadyExistsException
import ir.texingram.room.RoomController
import ir.texingram.session.ChatSession
import kotlinx.coroutines.channels.consumeEach

fun Route.chatSocket(roomController: RoomController) {
    webSocket(path = "/chat-socket") {
        val session = call.sessions.get<ChatSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }
        try {
            roomController.onJoin(username = session.username, sessionId = session.sessionId, socket = this)
            incoming.consumeEach { frame ->
                if (frame is Frame.Text)
                    roomController.sendMessage(senderUsername = session.username, message = frame.readText())
            }
        } catch (e: MemberAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            roomController.tryToDisconnect(session.username)
        }
    }
}

fun Route.getAllMessages(roomController: RoomController) {
    get(path = "/messages") {
        call.respond(HttpStatusCode.OK, roomController.getAllMessages())
    }
}