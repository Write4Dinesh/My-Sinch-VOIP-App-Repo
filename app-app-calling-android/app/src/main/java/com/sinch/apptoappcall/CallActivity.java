package com.sinch.apptoappcall;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class CallActivity extends AppCompatActivity {

    private static final String APP_KEY = "b3e96712-5ff9-4e21-9b1b-561d51331933";
    private static final String APP_SECRET = "9YZeS6yLzkKlkI4ATsogAA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private Call call;
    private TextView callState;
    private SinchClient sinchClient;
    private Button button;
    private String callerId;
    private String recipientId;

    private View mIncomingCallLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call);

        Intent intent = getIntent();
        callerId = intent.getStringExtra("callerId");
        recipientId = intent.getStringExtra("recipientId");
        mIncomingCallLayout = findViewById(R.id.incoming_call_layout);
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(callerId)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        button = (Button) findViewById(R.id.button);
        callState = (TextView) findViewById(R.id.callState);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (call == null) {
                    call = sinchClient.getCallClient().callUser(recipientId);
                    call.addCallListener(new SinchCallListener());
                    button.setText("Hang Up");
                } else {
                    call.hangup();
                }
            }
        });
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            SinchError a = endedCall.getDetails().getError();
            button.setText("Call");
            callState.setText("");
            Toast.makeText(CallActivity.this, "Call Ended", Toast.LENGTH_LONG).show();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            callState.setText("connected");
            Toast.makeText(CallActivity.this, "connected", Toast.LENGTH_LONG).show();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            callState.setText("ringing");
            Toast.makeText(CallActivity.this, "ringing", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            Toast.makeText(CallActivity.this, "should send push notification", Toast.LENGTH_LONG).show();
        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            Toast.makeText(CallActivity.this, "incoming call", Toast.LENGTH_SHORT).show();
            mIncomingCallLayout.setVisibility(View.VISIBLE);
            mIncomingCallLayout.findViewById(R.id.button_accept_call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    call.answer();
                    mIncomingCallLayout.setVisibility(View.INVISIBLE);
                }
            });
            mIncomingCallLayout.findViewById(R.id.button_decline_call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    call.hangup();
                    mIncomingCallLayout.setVisibility(View.INVISIBLE);
                }
            });
            call.answer();
            call.addCallListener(new SinchCallListener());
            button.setText("Hang Up");
        }
    }
}

