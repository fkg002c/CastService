package com.fkg002c.apps.castservice;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class StreamThread extends Thread {
    private static final int SAMPLE_RATE = 16000; // 44100 for music

    private boolean mIsRunning = true;
    private int mPort;

    public StreamThread(int port) {
        super("StreamThread-" + port);
        mPort = port;
    }

    public void selfDestroy() {
        mIsRunning = false;
    }

    @Override
    public void run() {
        super.run();
        try {
            Log.d(this.getName(), "Thread started");
            Socket socket = new Socket(InetAddress.getLocalHost(), mPort);
            OutputStream out = socket.getOutputStream();

            int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize * 4);
            Log.d(this.getName(), "Audio Recorder initialized");

            byte[] buffer = new byte[minBufSize * 2];

            recorder.startRecording();
            Log.d(this.getName(), "Audio Recorder running");

            int readLength = 0;
            long prevTime = System.currentTimeMillis();
            while ((readLength = recorder.read(buffer, 0, buffer.length)) != -1 && mIsRunning) {
                out.write(buffer, 0, readLength);
                Log.d(this.getName(), "    sent " + readLength + " bytes in " + (System.currentTimeMillis() - prevTime) + " milliseconds");
                prevTime = System.currentTimeMillis();
            }
            out.flush();
            socket.close();
            recorder.stop();
            recorder.release();
            Log.d(this.getName(), "Thread finished");
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
