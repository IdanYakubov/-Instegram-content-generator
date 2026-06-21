package com.thecompass.contentstudio.render

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer

/**
 * Encodes a sequence of ARGB bitmaps into an H.264/MP4 file using the byte-buffer
 * (non-Surface) MediaCodec API. Buffer-mode is used instead of an input Surface so each
 * frame's presentation timestamp can be set deterministically (frameIndex * 1_000_000 / fps)
 * rather than relying on wall-clock time, which is unreliable for offline, non-realtime encodes.
 */
class ReelEncoder(
    private val width: Int,
    private val height: Int,
    private val fps: Int,
    private val bitRate: Int = 6_000_000,
) {
    private val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
    private lateinit var codec: MediaCodec
    private lateinit var muxer: MediaMuxer
    private var trackIndex = -1
    private var muxerStarted = false
    private var colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
    private var frameIndex = 0L
    private val bufferInfo = MediaCodec.BufferInfo()

    fun start(outputPath: String) {
        codec = MediaCodec.createEncoderByType(mimeType)
        colorFormat = chooseColorFormat(codec.codecInfo.getCapabilitiesForType(mimeType))

        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun encodeFrame(bitmap: Bitmap) {
        val inputBufferId = codec.dequeueInputBuffer(10_000)
        if (inputBufferId >= 0) {
            val inputBuffer = codec.getInputBuffer(inputBufferId)!!
            val image = codec.getInputImage(inputBufferId)
            if (image != null) {
                val argb = IntArray(width * height)
                bitmap.getPixels(argb, 0, width, 0, 0, width, height)
                fillImageWithArgb(image, argb)
            }
            val presentationTimeUs = frameIndex * 1_000_000L / fps
            codec.queueInputBuffer(inputBufferId, 0, inputBuffer.limit(), presentationTimeUs, 0)
            frameIndex += 1
        }
        drainEncoder(false)
    }

    fun finish() {
        val inputBufferId = codec.dequeueInputBuffer(10_000)
        if (inputBufferId >= 0) {
            codec.queueInputBuffer(inputBufferId, 0, 0, frameIndex * 1_000_000L / fps, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        }
        drainEncoder(true)
        codec.stop()
        codec.release()
        if (muxerStarted) {
            muxer.stop()
        }
        muxer.release()
    }

    private fun drainEncoder(endOfStream: Boolean) {
        while (true) {
            val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            when {
                outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) return
                }
                outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    trackIndex = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
                outputBufferId >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputBufferId)!!
                    if (bufferInfo.size > 0 && muxerStarted) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }
                    val isEos = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    codec.releaseOutputBuffer(outputBufferId, false)
                    if (isEos) return
                }
            }
        }
    }

    private fun chooseColorFormat(caps: MediaCodecInfo.CodecCapabilities): Int {
        val preferred = intArrayOf(
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
        )
        for (candidate in preferred) {
            if (caps.colorFormats.contains(candidate)) return candidate
        }
        return caps.colorFormats.first()
    }

    private fun fillImageWithArgb(image: android.media.Image, argb: IntArray) {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuf = yPlane.buffer
        val uBuf = uPlane.buffer
        val vBuf = vPlane.buffer
        val yRowStride = yPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride

        for (row in 0 until height) {
            val rowOffset = row * width
            for (col in 0 until width) {
                val pixel = argb[rowOffset + col]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val yVal = (((66 * r + 129 * g + 25 * b + 128) shr 8) + 16).coerceIn(0, 255)
                yBuf.put(row * yRowStride + col, yVal.toByte())
            }
        }

        val chromaHeight = height / 2
        val chromaWidth = width / 2
        for (row in 0 until chromaHeight) {
            val srcRow = row * 2
            for (col in 0 until chromaWidth) {
                val srcCol = col * 2
                val pixel = argb[srcRow * width + srcCol]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val uVal = (((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128).coerceIn(0, 255)
                val vVal = (((112 * r - 94 * g - 18 * b + 128) shr 8) + 128).coerceIn(0, 255)
                uBuf.put(row * uRowStride + col * uPixelStride, uVal.toByte())
                vBuf.put(row * vRowStride + col * vPixelStride, vVal.toByte())
            }
        }
    }
}
