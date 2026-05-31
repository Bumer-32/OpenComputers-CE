package li.cil.oc.client.audio

import li.cil.oc.util.BlockPosition

import java.io.ByteArrayOutputStream
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10
import li.cil.oc.util.Audio
import li.cil.oc.{OpenComputers, Settings}
import net.minecraft.client.Minecraft

class AudioSession(
    val handle: Int,
    val channel: Int,
    val sampleRate: Int,
    val channels: Int,
    val format: Int,
    val pos: BlockPosition,
    val speakerPositions: Seq[BlockPosition]
  ) {

  private val bufferStream = new ByteArrayOutputStream()
  var loop: Boolean = false

  private var alBuffer: Int = -1
  private var alSources: Array[Int] = Array.empty
  private var isPlayCalled: Boolean = false

  private def effectivePositions: Seq[BlockPosition] =
    if (speakerPositions.nonEmpty) speakerPositions else Seq(pos)

  def append(data: Array[Byte]): Unit = {
    if (!isPlayCalled) bufferStream.write(data)
  }

  def play(): Unit = {
    if (isPlayCalled) {
      // resume from pause
      alSources.foreach { src =>
        if (src != -1) AL10.alSourcePlay(src)
      }
      return
    }
    isPlayCalled = true

    val pcmData = bufferStream.toByteArray
    if (pcmData.isEmpty) return

    val mc = Minecraft.getInstance
    if (mc.getSoundManager == null || mc.getSoundManager.soundEngine == null) return

    mc.getSoundManager.soundEngine.executor.execute(() => {
      try {
        AL10.alGetError()

        alBuffer = AL10.alGenBuffers()
        Audio.checkALError()

        val dataBuffer = BufferUtils.createByteBuffer(pcmData.length)
        dataBuffer.put(pcmData)
        dataBuffer.flip()
        AL10.alBufferData(alBuffer, format, dataBuffer, sampleRate)
        Audio.checkALError()

        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED)

        val maxDist = Settings.get.beepRadius.toFloat
        val volume  = mc.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.BLOCKS)

        val positions = effectivePositions
        alSources = new Array[Int](positions.size)

        positions.zipWithIndex.foreach { case (sp, i) =>
          val src = AL10.alGenSources()
          Audio.checkALError()
          alSources(i) = src

          AL10.alSourcei(src, AL10.AL_BUFFER, alBuffer)

          val sx = sp.x + 0.5f
          val sy = sp.y + 0.5f
          val sz = sp.z + 0.5f
          AL10.alSource3f(src, AL10.AL_POSITION, sx, sy, sz)

          AL10.alSourcef(src, AL10.AL_REFERENCE_DISTANCE, 1.0f)
          AL10.alSourcef(src, AL10.AL_MAX_DISTANCE, maxDist)
          AL10.alSourcef(src, AL10.AL_ROLLOFF_FACTOR, maxDist / 2.0f)
          AL10.alSourcef(src, AL10.AL_GAIN, volume * 0.3f)
          AL10.alSourcei(src, AL10.AL_LOOPING, if (loop) AL10.AL_TRUE else AL10.AL_FALSE)

          Audio.checkALError()
          AL10.alSourcePlay(src)
          Audio.checkALError()
        }

      } catch {
        case t: Throwable =>
          OpenComputers.log.error("Failed to play audio", t)
          cleanup()
      }
    })
  }

  def pause(): Unit = {
    if (alSources.nonEmpty) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        alSources.foreach { src =>
          if (src != -1 && AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING)
            AL10.alSourcePause(src)
        }
      })
    }
  }

  def resume(): Unit = {
    if (alSources.nonEmpty) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        alSources.foreach { src =>
          if (src != -1 && AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED)
            AL10.alSourcePlay(src)
        }
      })
    }
  }

  def stop(): Unit = {
    if (alSources.nonEmpty) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        alSources.foreach { src =>
          if (src != -1) AL10.alSourceStop(src)
        }
      })
    }
  }

  def setLoopMode(newLoop: Boolean): Unit = {
    loop = newLoop
    if (alSources.nonEmpty) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        alSources.foreach { src =>
          if (src != -1)
            AL10.alSourcei(src, AL10.AL_LOOPING, if (loop) AL10.AL_TRUE else AL10.AL_FALSE)
        }
      })
    }
  }

  def checkFinished: Boolean = {
    if (isPlayCalled && alSources.nonEmpty) {
      alSources.forall { src =>
        if (src == -1) true
        else {
          val state = AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)
          state != AL10.AL_PLAYING && state != AL10.AL_PAUSED
        }
      }
    } else false
  }

  def cleanup(): Unit = {
    alSources.foreach { src =>
      if (src != -1) try AL10.alDeleteSources(src) catch { case _: Throwable => }
    }
    alSources = Array.empty
    if (alBuffer != -1) {
      try AL10.alDeleteBuffers(alBuffer) catch { case _: Throwable => }
      alBuffer = -1
    }
  }
}
