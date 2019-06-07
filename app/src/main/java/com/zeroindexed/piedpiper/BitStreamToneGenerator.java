package com.zeroindexed.piedpiper;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.InputStream;
import java.util.Iterator;

public class BitStreamToneGenerator implements ToneThread.ToneIterator {
    private final static int START_HZ = 16500;
    private final static int STEP_HZ = 20;
    private final static int BITS = 4;

    private final static int HANDSHAKE_START_HZ = 17000;
    private final static int HANDSHAKE_END_HZ = HANDSHAKE_START_HZ + 256;

    private final int file_size;
    private final InputStream stream;

    BitStreamToneGenerator(InputStream stream, int file_size) {
        this.stream = stream;
        this.file_size = file_size;
    }

    @NonNull
    @Override
    public Iterator<Integer> iterator() {
        final Iterator<Integer> bits_iterator = new BitStreamIterator(stream, BITS).iterator();
        return new Iterator<Integer>() {
            boolean yield_start = false;
            boolean yield_end = false;

            @Override
            public boolean hasNext() {
                if (!yield_start || !yield_end) {
                    return true;
                }

                return bits_iterator.hasNext();
            }

            @Override
            public Integer next() {
                if (!yield_start) {
                    yield_start = true;
                    Log.e("HERE", HANDSHAKE_START_HZ + "<<--");
                    return HANDSHAKE_START_HZ;
                }

                if (!yield_end && !bits_iterator.hasNext()) {
                    yield_end = true;
                    Log.e("HERE", HANDSHAKE_END_HZ + "<<--");
                    return HANDSHAKE_END_HZ;
                }

                Integer step = bits_iterator.next();
                Log.e("STEP", step.toString());
                Log.e("DEBUG", "chunk: " + step + ", hz: " + (START_HZ + step * STEP_HZ));
                return START_HZ + step * STEP_HZ;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public int size() {
        // +2 for handshake
        return Math.round(file_size * ((float) Byte.SIZE) / BITS) + 2;
    }
}
