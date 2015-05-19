/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loroclip.soundfile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.loroclip.model.FrameGains;
import com.loroclip.model.Record;
import com.loroclip.util.Util;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

public class SoundFile {
    private ProgressListener mProgressListener = null;
    private File mInputFile = null;

    // Member variables representing frame data
    private String mFileType;
    private int mFileSize;
    private int mAvgBitRate;  // Average bit rate in kbps.
    private int mSampleRate;
    private int mChannels;
    private int mNumSamples;  // total number of samples per channel in audio file
    private ByteBuffer mDecodedBytes;  // Raw audio data
    private ShortBuffer mDecodedSamples;  // shared buffer with mDecodedBytes.
    // mDecodedSamples has the following format:
    // {s1c1, s1c2, ..., s1cM, s2c1, ..., s2cM, ..., sNc1, ..., sNcM}
    // where sicj is the ith sample of the jth channel (a sample is a signed short)
    // M is the number of channels (e.g. 2 for stereo) and N is the number of samples per channel.

    // Member variables for hack (making it work with old version, until app just uses the samples).
    private int mNumFrames;
    private int[] mFrameGains;
    private int[] mFrameLens;
    private int[] mFrameOffsets;
    private JSONArray mJSONArray;

    // Progress listener interface.
    public interface ProgressListener {
        /**
         * Will be called by the SoundFile class periodically
         * with values between 0.0 and 1.0.  Return true to continue
         * loading the file or recording the audio, and false to cancel or stop recording.
         */
        boolean reportProgress(double fractionComplete);
    }

    // Custom exception for invalid inputs.
    public class InvalidInputException extends Exception {
        // Serial version ID generated by Eclipse.
        private static final long serialVersionUID = -2505698991597837165L;
        public InvalidInputException(String message) {
            super(message);
        }
    }

    // TODO(nfaralli): what is the real list of supported extensions? Is it device dependent?
    public static String[] getSupportedExtensions() {
        return new String[] {"mp3", "wav", "3gpp", "3gp", "amr", "aac", "m4a", "ogg"};
    }

    public static boolean isFilenameSupported(String filename) {
        String[] extensions = getSupportedExtensions();
        for (int i=0; i<extensions.length; i++) {
            if (filename.endsWith("." + extensions[i])) {
                return true;
            }
        }
        return false;
    }

    // Create and return a SoundFile object using the file fileName.
    public static SoundFile create(String fileName,
                                   ProgressListener progressListener)
        throws java.io.FileNotFoundException,
        java.io.IOException, InvalidInputException {
        // First check that the file exists and that its extension is supported.
        File f = new File(fileName);
        if (!f.exists()) {
            throw new java.io.FileNotFoundException(fileName);
        }
        String name = f.getName().toLowerCase();
        String[] components = name.split("\\.");
        if (components.length < 2) {
            return null;
        }
        if (!Arrays.asList(getSupportedExtensions()).contains(components[components.length - 1])) {
            return null;
        }
        SoundFile soundFile = new SoundFile();
        soundFile.setProgressListener(progressListener);
        soundFile.ReadFile(f);
        return soundFile;
    }

    // Create and return a SoundFile object by recording a mono audio stream.
    public static SoundFile record(ProgressListener progressListener) {
        if (progressListener ==  null) {
            // must have a progessListener to stop the recording.
            return null;
        }
        SoundFile soundFile = new SoundFile();
        soundFile.setProgressListener(progressListener);
        soundFile.RecordAudio();
        return soundFile;
    }

    public String getFiletype() {
        return mFileType;
    }

    public int getFileSizeBytes() {
        return mFileSize;
    }

    public int getAvgBitrateKbps() {
        return mAvgBitRate;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getChannels() {
        return mChannels;
    }

    public int getNumSamples() {
        return mNumSamples;  // Number of samples per channel.
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    public int getNumFrames() {
        return mNumFrames;
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    public int getSamplesPerFrame() {
        return 1024;  // just a fixed value here...
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    public int[] getFrameGains() {
        return mFrameGains;
    }

    public ShortBuffer getSamples() {
        if (mDecodedSamples != null) {
            return mDecodedSamples.asReadOnlyBuffer();
        } else {
            return null;
        }
    }

    // A SoundFile object should only be created using the static methods create() and record().
    private SoundFile() {
    }

    private void setProgressListener(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    private void ReadFile(File inputFile)
        throws java.io.FileNotFoundException,
        java.io.IOException, InvalidInputException {
        MediaExtractor extractor = new MediaExtractor();
        MediaFormat format = null;
        int i;

        mInputFile = inputFile;
        String[] components = mInputFile.getPath().split("\\.");
        mFileType = components[components.length - 1];
        mFileSize = (int)mInputFile.length();
        extractor.setDataSource(mInputFile.getPath());
        int numTracks = extractor.getTrackCount();
        // find and select the first audio track present in the file.
        for (i=0; i<numTracks; i++) {
            format = extractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                extractor.selectTrack(i);
                break;
            }
        }
        if (i == numTracks) {
            throw new InvalidInputException("No audio track found in " + mInputFile);
        }
        mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);

        try {
            Record r = Record.find(Record.class, "file = ?", mInputFile.getAbsolutePath()).get(0);
            FrameGains fg = FrameGains.find(FrameGains.class, "record = ?", String.valueOf(r.getId())).get(0);
            JSONArray testJSON = new JSONArray(fg.getFrames());
            mFrameGains = Util.JSONArrayToIntArray(testJSON);
            mNumFrames = mFrameGains.length;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void RecordAudio() {
        if (mProgressListener ==  null) {
            // A progress listener is mandatory here, as it will let us know when to stop recording.
            return;
        }
        mInputFile = null;
        mFileType = "raw";
        mFileSize = 0;
        mSampleRate = 44100;
        mChannels = 1;  // record mono audio.
        short[] buffer = new short[1024];  // buffer contains 1 mono frame of 1024 16 bits samples
        int minBufferSize = AudioRecord.getMinBufferSize(
            mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        // make sure minBufferSize can contain at least 1 second of audio (16 bits sample).
        if (minBufferSize < mSampleRate * 2) {
            minBufferSize = mSampleRate * 2;
        }
        AudioRecord audioRecord = new AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            mSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        );

        // Allocate memory for 20 seconds first. Reallocate later if more is needed.
        mDecodedBytes = ByteBuffer.allocate(20 * mSampleRate * 2)
        ;
        mDecodedBytes.order(ByteOrder.LITTLE_ENDIAN);
        mDecodedSamples = mDecodedBytes.asShortBuffer();
        audioRecord.startRecording();
        while (true) {
            // check if mDecodedSamples can contain 1024 additional samples.
            if (mDecodedSamples.remaining() < 1024) {
                // Try to allocate memory for 10 additional seconds.
                int newCapacity = mDecodedBytes.capacity() + 10 * mSampleRate * 2;
                ByteBuffer newDecodedBytes = null;
                try {
                    newDecodedBytes = ByteBuffer.allocate(newCapacity);
                } catch (OutOfMemoryError oome) {
                    break;
                }
                int position = mDecodedSamples.position();
                mDecodedBytes.rewind();
                newDecodedBytes.put(mDecodedBytes);
                mDecodedBytes = newDecodedBytes;
                mDecodedBytes.order(ByteOrder.LITTLE_ENDIAN);
                mDecodedBytes.rewind();
                mDecodedSamples = mDecodedBytes.asShortBuffer();
                mDecodedSamples.position(position);
            }
            // TODO(nfaralli): maybe use the read method that takes a direct ByteBuffer argument.
            audioRecord.read(buffer, 0, buffer.length);
            mDecodedSamples.put(buffer);
            // Let the progress listener know how many seconds have been recorded.
            // The returned value tells us if we should keep recording or stop.
            if (!mProgressListener.reportProgress(
                (float)(mDecodedSamples.position()) / mSampleRate)) {
                break;
            }
        }
        audioRecord.stop();
        audioRecord.release();
        mNumSamples = mDecodedSamples.position();
        mDecodedSamples.rewind();
        mDecodedBytes.rewind();
        mAvgBitRate = mSampleRate * 16 / 1000;

        // Temporary hack to make it work with the old version.
        mNumFrames = mNumSamples / getSamplesPerFrame();
        mFrameGains = new int[mNumFrames];
        mFrameLens = null;  // not needed for recorded audio
        mFrameOffsets = null;  // not needed for recorded audio
        int i, j;
        int gain, value;
        for (i=0; i<mNumFrames; i++){
            gain = -1;
            for(j=0; j<getSamplesPerFrame(); j++) {
                value = java.lang.Math.abs(mDecodedSamples.get());
                if (gain < value) {
                    gain = value;
                }
            }
            mFrameGains[i] = (int)Math.sqrt(gain);  // here gain = sqrt(max value of 1st channel)...
        }
        mDecodedSamples.rewind();

        mJSONArray = new JSONArray();

//        for(int values : mFrameGains)
//        {
//
//            mJSONArray.put(values);
//        }

        for (int k=0;k<488000;k++) {
            mJSONArray.put(0);
        }



    }

    // should be removed in the near future...
    public void WriteFile(File outputFile, int startFrame, int numFrames)
        throws java.io.IOException {
        float startTime = (float)startFrame * getSamplesPerFrame() / mSampleRate;
        float endTime = (float)(startFrame + numFrames) * getSamplesPerFrame() / mSampleRate;
        WriteFile(outputFile, startTime, endTime);
    }

    public void WriteFile(File outputFile, float startTime, float endTime)
        throws java.io.IOException {
        int startOffset = (int)(startTime * mSampleRate) * 2 * mChannels;
        int numSamples = (int)((endTime - startTime) * mSampleRate);

        String mime_type = "audio/mp4a-latm";
        int bitrate = 64000 * mChannels;
        MediaCodec codec = MediaCodec.createEncoderByType(mime_type);
        MediaFormat format = MediaFormat.createAudioFormat(mime_type, mSampleRate, mChannels);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        codec.start();

        // Get an estimation of the encoded data based on the bitrate. Add 10% to it.
        int estimatedEncodedSize = (int)((endTime - startTime) * (bitrate / 8) * 1.1);
        ByteBuffer encodedBytes = ByteBuffer.allocate(estimatedEncodedSize);
        ByteBuffer[] inputBuffers = codec.getInputBuffers();
        ByteBuffer[] outputBuffers = codec.getOutputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean done_reading = false;
        long presentation_time = 0;

        int frame_size = 1024;  // number of samples per frame for an mp4 (AAC) frame.
        byte buffer[] = new byte[frame_size * mChannels * 2];  // each sample is coded with a short
        mDecodedBytes.position(startOffset);
        int tot_num_frames = 1 + (numSamples / frame_size);  // first AAC frame = 2 bytes
        if (numSamples % frame_size != 0) {
            tot_num_frames++;
        }
        int[] frame_sizes = new int[tot_num_frames];
        int num_out_frames = 0;
        int num_frames=0;
        int num_samples_left = numSamples;
        int encodedSamplesSize = 0;  // size of the output buffer containing the encoded samples.
        byte[] encodedSamples = null;
        while (true) {
            // Feed the samples to the encoder.
            int inputBufferIndex = codec.dequeueInputBuffer(100);
            if (!done_reading && inputBufferIndex >= 0) {
                if (num_samples_left <= 0) {
                    // All samples have been read.
                    codec.queueInputBuffer(
                        inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    done_reading = true;
                } else {
                    inputBuffers[inputBufferIndex].clear();
                    if (buffer.length > inputBuffers[inputBufferIndex].remaining()) {
                        // Input buffer is smaller than one frame. This should never happen.
                        continue;
                    }
                    if (mDecodedBytes.remaining() < buffer.length) {
                        for (int i=mDecodedBytes.remaining(); i<buffer.length; i++) {
                            buffer[i] = 0;  // pad with extra 0s to make a full frame.
                        }
                        mDecodedBytes.get(buffer, 0, mDecodedBytes.remaining());
                    } else {
                        mDecodedBytes.get(buffer);
                    }
                    num_samples_left -= frame_size;
                    inputBuffers[inputBufferIndex].put(buffer);
                    presentation_time = (long) (((num_frames++) * frame_size * 1e6) / mSampleRate);
                    codec.queueInputBuffer(
                        inputBufferIndex, 0, buffer.length, presentation_time, 0);
                }
            }

            // Get the encoded samples from the encoder.
            int outputBufferIndex = codec.dequeueOutputBuffer(info, 100);
            if (outputBufferIndex >= 0 && info.size > 0 && info.presentationTimeUs >=0) {
                if (num_out_frames < frame_sizes.length) {
                    frame_sizes[num_out_frames++] = info.size;
                }
                if (encodedSamplesSize < info.size) {
                    encodedSamplesSize = info.size;
                    encodedSamples = new byte[encodedSamplesSize];
                }
                outputBuffers[outputBufferIndex].get(encodedSamples, 0, info.size);
                outputBuffers[outputBufferIndex].clear();
                codec.releaseOutputBuffer(outputBufferIndex, false);
                if (encodedBytes.remaining() < info.size) {  // Hopefully this should not happen.
                    estimatedEncodedSize = (int)(estimatedEncodedSize * 1.2);  // Add 20%.
                    ByteBuffer newEncodedBytes = ByteBuffer.allocate(estimatedEncodedSize);
                    int position = encodedBytes.position();
                    encodedBytes.rewind();
                    newEncodedBytes.put(encodedBytes);
                    encodedBytes = newEncodedBytes;
                    encodedBytes.position(position);
                }
                encodedBytes.put(encodedSamples, 0, info.size);
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = codec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // We could check that codec.getOutputFormat(), which is the new output format,
                // is what we expect.
            }
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                // We got all the encoded data from the encoder.
                break;
            }
        }
        int encoded_size = encodedBytes.position();
        encodedBytes.rewind();
        codec.stop();
        codec.release();
        codec = null;

        // Write the encoded stream to the file, 4kB at a time.
        buffer = new byte[4096];
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(
    / Write the ded stream to the file, 4kB at a time.
        buffer = new byte[4096];
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(
                MP4Header.getMP4Header(mSampleRate, mChannels, frame_sizes, bitrate));
            while (encoded_size - encodedBytes.position() > buffer.length) {
                encodedBytes.get(buffer);
                outputStream.write(buffer);
            }
            int remaining = encoded_size - encodedB
            tes.position();
            if (remaining > 0) {
                encodedBytes.get(buffer, 0, remaining);
                outputStream.write(buffer, 0, remaining);
            }
            outputStream.close();


            // save record to database
            Record r = new Record(outputFile.getName(), outputFile.getAbsolutePath());
            r.save();
            FrameGains fg = new FrameGains(mJSONArray.toString());
            fg.setRecord(r);
            fg.save();
        } catch (IOException e) {
            // TODO(nfaralli): Should and exception be thrown here? At least log this error.
        }
    }
}
