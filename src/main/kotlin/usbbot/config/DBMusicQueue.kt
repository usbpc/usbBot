package usbbot.config

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.collections4.queue.CircularFifoQueue
import org.apache.commons.dbutils.ResultSetHandler
import usbbot.modules.music.Music
import usbbot.modules.music.MyData
import util.getLogger
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

data class DBMusicQueueEntry(val id: Int, val forGuild: Long, val youtubeId: String, val requetedBy: Long, val addedAt: Timestamp) {
    fun getAudioTrack() : AudioTrack? {
        var retTrack : AudioTrack? = null
        Music.playerManager.loadItem(youtubeId, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) {
                DBMusicQueue.logger.warn("This is awkward... there was a video in the DB that won't load: $youtubeId")
            }

            override fun trackLoaded(track: AudioTrack) {
                retTrack = track
            }

            override fun noMatches() {
                DBMusicQueue.logger.warn("This is awkward... there was a video in the DB that no longer exists: $youtubeId")
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                throw IllegalStateException("There should never be a playlist loaded from the database backend!")
            }
        }).get()
        retTrack?.userData = MyData(requetedBy, id, forGuild, youtubeId)
        return retTrack
    }
}

fun getSongsFromDB(guildID: Long, limit: Int) : List<DBMusicQueueEntry> =
        DatabaseConnection.queryRunner
                .query("SELECT * FROM music_queue WHERE forGuild = ? ORDER BY addedAt ASC LIMIT ?",
                        DBMusicQueue.songDBEntryCreator,
                        guildID, limit)

fun getMusicQueueForGuild(guildID: Long) : DBMusicQueue {
    val result = getSongsFromDB(guildID, 10)
    val queue = CircularFifoQueue<AudioTrack>(10)
    result.forEach {
        val tmp = it.getAudioTrack()
        if (tmp != null) {
            queue.add(tmp)
        } else {
            DatabaseConnection.queryRunner.update("DELETE FROM music_queue WHERE id = ?", it.id)
        }
    }
    val rsh = ResultSetHandler {
        if (it.next()) it.getInt(1)
        else 0
    }
    val size = DatabaseConnection.queryRunner.query("SELECT COUNT(*) FROM music_queue WHERE forGuild = ?",
            rsh,
            guildID)

    return DBMusicQueue(guildID, queue, size)
}

class DBMusicQueue(val guildID: Long, val queue : Queue<AudioTrack>, var size: Int) {
    companion object {
        val logger = DBMusicQueue.getLogger()
        val songDBEntryCreator = ResultSetHandler <List<DBMusicQueueEntry>> {
            val output = mutableListOf<DBMusicQueueEntry>()
            while (it.next()) {
                output.add(DBMusicQueueEntry(
                        it.getInt("id"),
                        it.getLong("forGuild"),
                        it.getString("youtubeId"),
                        it.getLong("requestedBy"),
                        it.getTimestamp("addedAt")))
            }
            output
        }


        val musicQueueWorker = newFixedThreadPoolContext(2, "Music Queue Worker")
    }

    fun removeFromDB(track: AudioTrack) = launch(musicQueueWorker) {
        DatabaseConnection.queryRunner.update("DELETE FROM music_queue WHERE id = ?", (track.userData as MyData).id)
    }


    fun add(track: AudioTrack) {
        val data = (track.userData as MyData)
        DatabaseConnection.queryRunner
                .update("INSERT INTO music_queue (forGuild, youtubeId, requestedBy) VALUES (?, ?, ?)",
                        data.forGuild, data.youtubeId, data.requestedBy)
        synchronized(size) {
            if (size < 10) {
                synchronized(queue) {
                    val musicQueueEntry = DatabaseConnection.queryRunner
                            .query("SELECT * FROM music_queue WHERE forGuild = ? AND youtubeId = ? AND requestedBy = ?",
                                    DBMusicQueue.songDBEntryCreator,
                                    data.forGuild, data.youtubeId, data.requestedBy)
                    (track.userData as MyData).id = musicQueueEntry.first().id
                    queue.add(track)
                }
            }
            size++
        }
    }

    fun hasNext() : Boolean = queue.isNotEmpty()
    fun getNext() : AudioTrack? {
        val out = synchronized(queue) {
            queue.poll()
        }

        removeFromDB(out)

        if (queue.size < 5) {
            launch(musicQueueWorker) {
                val size = synchronized(queue) {
                    queue.size
                }
                val songs = getSongsFromDB(guildID, 10 - size)

                songs.forEach {
                    val audioTrack = it.getAudioTrack()
                    if (audioTrack != null) {
                        synchronized(queue) {
                            queue.add(audioTrack)
                        }
                    }
                }
            }
        }

        synchronized(size) {
            size--
        }

        return out
    }
}