package com.zeroindexed.piedpiper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.casualcoding.reedsolomon.EncoderDecoder;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity implements ToneThread.ToneCallback {
    private static final int FEC_BYTES = 4;

    private EditText text;
    private View playTone;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text =  findViewById(R.id.text);
        play_tone = findViewById(R.id.play_tone);
        progress =  findViewById(R.id.progress);
        play_tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = text.getText().toString();
                byte[] payload;
                payload = message.getBytes(Charset.forName("UTF-8"));

                EncoderDecoder encoder = new EncoderDecoder();
                final byte[] fec_payload;
                try {
                    fec_payload = encoder.encodeData(payload, FEC_BYTES);
                } catch (EncoderDecoder.DataTooLargeException e) {
                    return;
                }

                ByteArrayInputStream bis = new ByteArrayInputStream(fec_payload);

                play_tone.setEnabled(false);
                ToneThread.ToneIterator tone = new BitStreamToneGenerator(bis, 7);
                Log.e("Stream", tone.toString());
                new ToneThread(tone, MainActivity.this).start();
            }
        });
    }

    @Override
    public void onProgress(int current, int total) {
        progress.setMax(total);
        progress.setProgress(current);
    }

    @Override
    public void onDone() {
        play_tone.setEnabled(true);
        progress.setProgress(0);
    }
}
