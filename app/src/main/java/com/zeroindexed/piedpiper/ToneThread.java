package com.zeroindexed.piedpiper;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ToneThread extends Thread {
    public interface ToneCallback {
        void onProgress(int current, int total);

        void onDone();
    }

    public interface ToneIterator extends Iterable<Integer> {
        int size();
    }

    private static final int sample_rate = 44100;
    private static final float duration = 0.1f;  //Fast playing
    private static final int sample_size = Math.round(duration * sample_rate);

    private final ToneIterator frequencies;
    private final ToneCallback callback;
    private boolean callback_done = false;

    ToneThread(ToneIterator frequencies, ToneCallback callback) {
        this.frequencies = frequencies;
        this.callback = callback;
        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        final AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sample_rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                2 * sample_size,
                AudioTrack.MODE_STREAM
        );

        final int total_samples = Math.round(frequencies.size() * sample_size);

        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                if (!callback_done) {
                    callback.onDone();
                    callback_done = true;
                }
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                if (!callback_done) {
                    callback.onProgress(track.getPlaybackHeadPosition(), total_samples);
                }
            }
        });
        track.setPositionNotificationPeriod(sample_rate / 10);

        track.play();

        for (int freq : frequencies) {
            short[] samples = generate(freq);
            track.write(samples, 0, samples.length);
        }

        track.setNotificationMarkerPosition(sample_size);
    }

    private static short[] generate(float frequency) {
        final short sample[] = new short[sample_size];
        final double increment = (2 * Math.PI * frequency) / sample_rate;

        double angle = 0;
        System.out.println("Sample length : " + sample.length);
        for (int i = 0; i < sample.length; i++) {
            sample[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
            angle += increment;
//            System.out.println(sample[i]);
        }
//        for (short sampl : sample) {
//            System.out.print(frequency + " " + sampl);
//        }
        return sample;
    }
}
