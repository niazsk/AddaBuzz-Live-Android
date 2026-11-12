package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LineKind { INPUT, OUTPUT, ERROR, MUTED, ACCENT }

data class TerminalLine(val text: String, val kind: LineKind = LineKind.OUTPUT)

object TerminalEngine {

    const val CLEAR = "__clear__"

    private val virtualFiles = listOf(
        "help.txt", "rooms.txt", "users.txt", "wallet.txt",
        "gifts.txt", "notifications.txt", "messages.txt", "seats.txt"
    )

    fun execute(raw: String, repository: AddaBuzzRepository): List<TerminalLine> {
        val command = raw.trim()
        if (command.isEmpty()) return emptyList()

        val parts = command.split(Regex("\\s+"))
        val name = parts[0].lowercase(Locale.US)
        val args = parts.drop(1)

        return when (name) {
            "clear" -> listOf(TerminalLine(CLEAR, LineKind.MUTED))
            "help" -> help()
            "whoami" -> whoami(repository)
            "status" -> status(repository)
            "ls" -> listOf(
                TerminalLine("virtual filesystem of AddaBuzz Live", LineKind.MUTED),
                TerminalLine("drwxr-xr-x  app  app  ${virtualFiles.size} files", LineKind.OUTPUT)
            ) + virtualFiles.map { TerminalLine("  $it", LineKind.OUTPUT) }

            "cat" -> cat(args, repository)
            "rooms" -> rooms(repository)
            "users" -> users(repository)
            "wallet" -> wallet(repository)
            "tx" -> transactions(repository)
            "gifts" -> gifts(repository)
            "notifications", "notif" -> notifications(repository)
            "msgs", "messages" -> messages(repository)
            "seats" -> seats(repository)
            "react" -> react(args, repository)
            "follow" -> follow(args, repository, true)
            "unfollow" -> follow(args, repository, false)
            "echo" -> listOf(TerminalLine(args.joinToString(" "), LineKind.OUTPUT))
            "date" -> listOf(TerminalLine(SimpleDateFormat("EEE MMM dd, HH:mm:ss z", Locale.US).format(Date()), LineKind.OUTPUT))
            "version" -> listOf(
                TerminalLine("AddaBuzz Live v1.0.0", LineKind.ACCENT),
                TerminalLine("applicationId: com.aistudio.addabuzzlive.qvxkzp", LineKind.OUTPUT),
                TerminalLine("minSdk 24 / targetSdk 36", LineKind.OUTPUT)
            )
            "ping" -> listOf(TerminalLine("pong from ${repository.sessionManager.getServerUrl()} (42 ms)", LineKind.OUTPUT))
            else -> listOf(TerminalLine("$name: command not found. Type 'help' for the command list.", LineKind.ERROR))
        }
    }

    private fun help(): List<TerminalLine> {
        val entries = listOf(
            "help" to "show this command list",
            "whoami" to "print the signed-in streamer profile",
            "status" to "summarise app, session and wallet state",
            "ls" to "list files in the virtual filesystem",
            "cat <file>" to "print a file from the virtual filesystem",
            "rooms" to "list all live rooms",
            "users" to "list known creators and viewers",
            "wallet" to "show coin and diamond balances",
            "tx" to "show wallet transaction history",
            "gifts" to "list the gift catalog with prices",
            "notifications" to "show the notification feed",
            "msgs" to "show recent conversations",
            "seats" to "show the 8-seat guest stage",
            "react [n]" to "add n reactions to the live room",
            "follow <user>" to "follow a creator by username",
            "unfollow <user>" to "unfollow a creator by username",
            "echo <text>" to "print text back",
            "date" to "print the current date and time",
            "version" to "print app version info",
            "ping" to "check backend reachability",
            "clear" to "clear the terminal output"
        )
        return listOf(TerminalLine("AddaBuzz Live Shell — available commands", LineKind.ACCENT)) +
            entries.map { TerminalLine("  ${it.first.padEnd(14)} ${it.second}", LineKind.OUTPUT) }
    }

    private fun whoami(repository: AddaBuzzRepository): List<TerminalLine> {
        val user = repository.sessionManager.currentUser.value
            ?: return listOf(TerminalLine("no active session. Please sign in first.", LineKind.ERROR))
        return listOf(
            TerminalLine("id          ${user.id}", LineKind.OUTPUT),
            TerminalLine("username    @${user.username}", LineKind.OUTPUT),
            TerminalLine("name        ${user.displayName}", LineKind.OUTPUT),
            TerminalLine("email       ${user.email}", LineKind.OUTPUT),
            TerminalLine("level       ${user.level}", LineKind.OUTPUT),
            TerminalLine("badge       ${user.badge}", LineKind.OUTPUT),
            TerminalLine("coins       ${user.coins}", LineKind.OUTPUT),
            TerminalLine("diamonds    ${user.diamonds}", LineKind.OUTPUT)
        )
    }

    private fun status(repository: AddaBuzzRepository): List<TerminalLine> {
        val user = repository.sessionManager.currentUser.value
        val room = repository.currentRoom.value
        return listOf(
            TerminalLine("── app ──────────────────────────", LineKind.MUTED),
            TerminalLine("version     1.0.0 (build 1)", LineKind.OUTPUT),
            TerminalLine("backend     ${repository.sessionManager.getServerUrl()}", LineKind.OUTPUT),
            TerminalLine("quality     ${repository.sessionManager.getStreamResolution()}", LineKind.OUTPUT),
            TerminalLine("── session ───────────────────────", LineKind.MUTED),
            TerminalLine("user        ${user?.username ?: "anonymous"}", LineKind.OUTPUT),
            TerminalLine("coins       ${user?.coins ?: 0}", LineKind.OUTPUT),
            TerminalLine("diamonds    ${user?.diamonds ?: 0}", LineKind.OUTPUT),
            TerminalLine("live room   ${room?.title ?: "none"}", LineKind.OUTPUT),
            TerminalLine("rooms       ${repository.allRooms.value.size}", LineKind.OUTPUT),
            TerminalLine("following   ${repository.followingUsers.value.size}", LineKind.OUTPUT),
            TerminalLine("reactions   ${repository.reactionCount.value}", LineKind.OUTPUT)
        )
    }

    private fun cat(args: List<String>, repository: AddaBuzzRepository): List<TerminalLine> {
        if (args.isEmpty()) return listOf(TerminalLine("usage: cat <file>", LineKind.ERROR))
        val file = args[0].lowercase(Locale.US)
        if (file !in virtualFiles) {
            return listOf(TerminalLine("cat: $file: no such file. Try 'ls'.", LineKind.ERROR))
        }
        return when (file) {
            "help.txt" -> help()
            "rooms.txt" -> rooms(repository)
            "users.txt" -> users(repository)
            "wallet.txt" -> wallet(repository)
            "gifts.txt" -> gifts(repository)
            "notifications.txt" -> notifications(repository)
            "messages.txt" -> messages(repository)
            "seats.txt" -> seats(repository)
            else -> listOf(TerminalLine("cat: $file: empty", LineKind.MUTED))
        }
    }

    private fun rooms(repository: AddaBuzzRepository): List<TerminalLine> {
        val rooms = repository.allRooms.value
        if (rooms.isEmpty()) return listOf(TerminalLine("no live rooms right now", LineKind.MUTED))
        val lines = mutableListOf(
            TerminalLine("id          type          viewers  title", LineKind.MUTED)
        )
        rooms.forEach { room ->
            lines += TerminalLine(
                "${room.id.padEnd(12)} ${room.roomType.name.padEnd(13)} ${room.viewerCount.toString().padEnd(8)} ${room.title}",
                LineKind.OUTPUT
            )
        }
        return lines
    }

    private fun users(repository: AddaBuzzRepository): List<TerminalLine> {
        val users = buildList {
            addAll(repository.followingUsers.value)
            addAll(repository.followersUsers.value)
            repository.allRooms.value.forEach { room -> add(room.host) }
        }.distinctBy { it.id }
        val lines = mutableListOf(
            TerminalLine("username          level  live   coins   name", LineKind.MUTED)
        )
        users.forEach { user ->
            lines += TerminalLine(
                "${user.username.padEnd(18)} ${user.level.toString().padEnd(6)} ${(if (user.isLive) "yes" else "no").padEnd(6)} ${user.coins.toString().padEnd(7)} ${user.displayName}",
                LineKind.OUTPUT
            )
        }
        return lines
    }

    private fun wallet(repository: AddaBuzzRepository): List<TerminalLine> {
        val user = repository.sessionManager.currentUser.value
            ?: return listOf(TerminalLine("no active session", LineKind.ERROR))
        return listOf(
            TerminalLine("coins      ${user.coins}  (spendable)", LineKind.ACCENT),
            TerminalLine("diamonds   ${user.diamonds}  (withdrawable)", LineKind.ACCENT),
            TerminalLine("transactions ${repository.transactions.value.size}", LineKind.MUTED)
        )
    }

    private fun transactions(repository: AddaBuzzRepository): List<TerminalLine> {
        val txs = repository.transactions.value
        if (txs.isEmpty()) return listOf(TerminalLine("no transactions yet", LineKind.MUTED))
        return txs.map { tx ->
            TerminalLine("${tx.date.padEnd(18)} ${tx.amount.padEnd(14)} ${tx.title}", LineKind.OUTPUT)
        }
    }

    private fun gifts(repository: AddaBuzzRepository): List<TerminalLine> {
        return repository.availableGifts.map { gift ->
            TerminalLine("${gift.iconEmoji} ${gift.name.padEnd(18)} ${gift.coinPrice.toString().padStart(6)} coins", LineKind.OUTPUT)
        }
    }

    private fun notifications(repository: AddaBuzzRepository): List<TerminalLine> {
        val items = repository.notifications.value
        if (items.isEmpty()) return listOf(TerminalLine("no notifications", LineKind.MUTED))
        return items.map { item ->
            TerminalLine("${item.timeAgo.padEnd(8)} [${item.type.name}] ${item.title}", LineKind.OUTPUT)
        }
    }

    private fun messages(repository: AddaBuzzRepository): List<TerminalLine> {
        val conversations = repository.conversations.value
        if (conversations.isEmpty()) return listOf(TerminalLine("no conversations", LineKind.MUTED))
        return conversations.map { conv ->
            TerminalLine("@${conv.peer.username.padEnd(16)} ${conv.lastMessage}", LineKind.OUTPUT)
        }
    }

    private fun seats(repository: AddaBuzzRepository): List<TerminalLine> {
        val seats = repository.guestSeats.value
        return seats.map { seat ->
            val state = when {
                seat.isLocked -> "locked"
                seat.guest == null -> "empty"
                seat.isMuted -> "muted"
                else -> "live"
            }
            TerminalLine(
                "seat ${(seat.index + 1).toString().padEnd(3)} $state".padEnd(18) + (seat.guest?.displayName ?: "-"),
                LineKind.OUTPUT
            )
        }
    }

    private fun react(args: List<String>, repository: AddaBuzzRepository): List<TerminalLine> {
        val count = args.firstOrNull()?.toIntOrNull() ?: 1
        if (count < 1 || count > 100) {
            return listOf(TerminalLine("usage: react [1..100]", LineKind.ERROR))
        }
        repeat(count) { repository.addReaction() }
        return listOf(TerminalLine("+$count reactions — total ${repository.reactionCount.value}", LineKind.ACCENT))
    }

    private fun follow(args: List<String>, repository: AddaBuzzRepository, follow: Boolean): List<TerminalLine> {
        if (args.isEmpty()) return listOf(TerminalLine("usage: ${if (follow) "follow" else "unfollow"} <username>", LineKind.ERROR))
        val target = args[0].removePrefix("@")
        val user = repository.allRooms.value.map { it.host }
            .plus(repository.followingUsers.value)
            .plus(repository.followersUsers.value)
            .firstOrNull { it.username.equals(target, ignoreCase = true) }
            ?: return listOf(TerminalLine("user not found: $target", LineKind.ERROR))

        val isFollowing = repository.followingUsers.value.any { it.id == user.id }
        if (follow && isFollowing) return listOf(TerminalLine("already following @${user.username}", LineKind.MUTED))
        if (!follow && !isFollowing) return listOf(TerminalLine("not following @${user.username}", LineKind.MUTED))

        repository.toggleFollow(user)
        return listOf(
            TerminalLine(
                "${if (follow) "following" else "unfollowed"} @${user.username}",
                LineKind.ACCENT
            )
        )
    }
}
