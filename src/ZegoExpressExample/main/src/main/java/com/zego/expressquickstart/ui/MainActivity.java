package com.zego.expressquickstart.ui;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.common.GetAppIdConfig;
import com.zego.common.util.AppLogger;
import com.zego.common.widgets.log.FloatingView;
import com.zego.expressquickstart.R;
import com.zego.expressquickstart.databinding.ActivityMainBinding;
import com.zego.zegoexpress.ZegoExpressEngine;
import com.zego.zegoexpress.callback.IZegoEventHandler;
import com.zego.zegoexpress.constants.ZegoLanguage;
import com.zego.zegoexpress.constants.ZegoPlayerState;
import com.zego.zegoexpress.constants.ZegoPublisherState;
import com.zego.zegoexpress.constants.ZegoRoomState;
import com.zego.zegoexpress.constants.ZegoScenario;
import com.zego.zegoexpress.constants.ZegoUpdateType;
import com.zego.zegoexpress.constants.ZegoViewMode;
import com.zego.zegoexpress.entity.ZegoCanvas;
import com.zego.zegoexpress.entity.ZegoStream;
import com.zego.zegoexpress.entity.ZegoUser;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ImageButton ib_local_mic;
    ImageButton ib_remote_stream_audio;
    boolean publishMicEnable = true;
    boolean playStreamMute = true;

    public static ZegoExpressEngine engine = null;
    private String userID;
    private String userName;
    String roomID;
    String publishStreamID;
    String playStreamID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        /** 申请权限 */
        checkOrRequestPermission();

        /** 添加悬浮日志视图 */
        FloatingView.get().add();
        /** 记录SDK版本号 */
        AppLogger.getInstance().i("SDK version : %s", ZegoExpressEngine.getVersion());

        TextView tvAppID = findViewById(R.id.tv_appid);
        tvAppID.setText("AppID: " + GetAppIdConfig.appId);

        /** 示例Demo使用一个固定的房间ID */
        roomID = "room123";
        TextView tvRoomID = findViewById(R.id.tv_roomid);
        tvRoomID.setText("房间ID: " + roomID);

        /** 生成随机的用户ID，避免不同手机使用时用户ID冲突，相互影响 */
        String randomSuffix = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));
        userID = "user" + randomSuffix;
        userName = "user" + randomSuffix;
        TextView tvUserID = findViewById(R.id.tv_userid);
        tvUserID.setText("用户ID: " + userID);

        /** 生成随机的流ID, 默认推流ID和拉流ID一致，可以拉自己的流，也可以在界面上修改流ID */
        publishStreamID = "s" + randomSuffix;
        playStreamID = "s" + randomSuffix;
        EditText et = findViewById(R.id.ed_publish_stream_id);
        et.setText(publishStreamID);
        et = findViewById(R.id.ed_play_stream_id);
        et.setText(playStreamID);

        /** 本地音频输入开关 */
        ib_local_mic = findViewById(R.id.ib_local_mic);
        /** 从远端拉取的流，音频是否静音的开关 */
        ib_remote_stream_audio = findViewById(R.id.ib_remote_mic);
    }

    @Override
    protected void onDestroy() {
        // Release SDK resources
        ZegoExpressEngine.destroyEngine();
        super.onDestroy();
    }

    /** Click Init Button */
    public void ClickInit(View view) {
        Button button = (Button)view;
        if (button.getText().equals("初始化SDK")) {
            /** Initialize SDK, use test environment, access to general scenarios */
            engine = ZegoExpressEngine.createEngine(GetAppIdConfig.appId, GetAppIdConfig.appSign, true, ZegoScenario.SCENARIO_GENERAL, getApplication(), null);
            AppLogger.getInstance().i("初始化SDK成功");
            Toast.makeText(this, "初始化SDK成功", Toast.LENGTH_SHORT).show();
            button.setText("释放SDK");
            engine.setDebugVerbose(true, ZegoLanguage.LANGUAGE_CHINESE);
            engine.addEventHandler(new IZegoEventHandler() {
                /** The following are callbacks frequently used */
                @Override
                public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode) {
                    /** Room status update callback: after logging into the room, when the room connection status changes
                     * (such as room disconnection, login authentication failure, etc.), the SDK will notify through the callback
                     */
                    AppLogger.getInstance().i("onRoomStateUpdate: roomID = " + roomID + ", state = " + state + ", errorCode = " + errorCode);
                }

                @Override
                public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                    /** User status is updated. After logging into the room, when a user is added or deleted in the room,
                     * the SDK will notify through this callback
                     */
                    AppLogger.getInstance().i("onRoomUserUpdate: roomID = " + roomID + ", updateType = " + updateType);
                    for (int i = 0; i < userList.size(); i++) {
                        AppLogger.getInstance().i("userID = " + userList.get(i).userID + ", userName = " + userList.get(i).userName);
                    }
                }

                @Override
                public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
                    /** The stream status is updated. After logging into the room, when there is a new push or delete of audio and video stream,
                     * the SDK will notify through this callback */
                    AppLogger.getInstance().i("onRoomStreamUpdate: roomID = " + roomID + ", updateType = " + updateType);
                    for (int i = 0; i < streamList.size(); i++) {
                        AppLogger.getInstance().i("streamID = " + streamList.get(i).streamId);
                    }
                }

                @Override
                public void onDebugError(int errorCode, String funcName, String info) {
                    /** Printing debugging error information */
                    AppLogger.getInstance().i("onDebugError: errorCode = " + errorCode + ", funcName = " + funcName + ", info = " + info);
                }

                @Override
                public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode) {
                    /** After calling the stream publishing interface successfully, when the status of the stream changes,
                     * such as the exception of streaming caused by network interruption, the SDK will notify through this callback
                     */
                    AppLogger.getInstance().i("onPublisherStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
                }


                @Override
                public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode) {
                    /** After calling the streaming interface successfully, when the status of the stream changes,
                     * such as network interruption leading to abnormal situation, the SDK will notify through
                     * this callback */
                    AppLogger.getInstance().i("onPlayerStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
                }
            });
        }
        else {
            /** Destroy Engine */
            ZegoExpressEngine.destroyEngine();
            engine = null;
            AppLogger.getInstance().i("释放SDK成功");
            Toast.makeText(this, "释放SDK成功", Toast.LENGTH_SHORT).show();
            button.setText("初始化SDK");
        }

    }

    /** Click Login Button */
    public void ClickLogin(View view) {
        if (engine == null) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
            return;
        }
        Button button = (Button)view;
        if (button.getText().equals("进入房间")) {
            /** Create user */
            ZegoUser user = new ZegoUser(userID, userName);

            /** Begin to login room */
            engine.loginRoom(roomID, user, null);
            AppLogger.getInstance().i("调用进入房间接口成功, 用户ID = " + userID + " , 用户名 = " + userName);
            Toast.makeText(this, "调用进入房间接口成功", Toast.LENGTH_SHORT).show();
            button.setText("退出房间");
        }
        else {
            /** Begin to logout room */
            engine.logoutRoom(roomID);
            AppLogger.getInstance().i("调用退出房间接口成功, 用户ID = " + userID + " , 用户名 = " + userName);
            Toast.makeText(this, "调用退出房间接口成功", Toast.LENGTH_SHORT).show();
            button.setText("进入房间");
        }
    }

    /** Click Publish Button */
    public void ClickPublish(View view) {
        if (engine == null) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
            return;
        }

        Button button = (Button)view;
        if (button.getText().equals("推流")) {
            EditText et = findViewById(R.id.ed_publish_stream_id);
            String streamID = et.getText().toString();
            publishStreamID = streamID;

            /** Begin to publish stream */
            engine.startPublishing(streamID);
            AppLogger.getInstance().i("调用推流接口成功, 流ID = " + streamID);
            View local_view = findViewById(R.id.local_view);
            Toast.makeText(this, "调用推流接口成功", Toast.LENGTH_SHORT).show();

            /** Start preview and set the local preview view. */
            engine.startPreview(new ZegoCanvas(local_view, ZegoViewMode.VIEW_MODE_ASPECT_FILL));

            AppLogger.getInstance().i("调用启动预览成功");
            Toast.makeText(this, "调用启动预览成功", Toast.LENGTH_SHORT).show();
            button.setText("停止推流");
        }
        else {
            /** Begin to stop publish stream */
            engine.stopPublishing();

            /** Start stop preview and set the local preview view. */
            engine.stopPreview();
            AppLogger.getInstance().i("调用停止推流接口成功");
            Toast.makeText(this, "调用停止推流接口成功", Toast.LENGTH_SHORT).show();
            button.setText("推流");
        }
    }

    /** Click Play Button */
    public void ClickPlay(View view) {
        if (engine == null) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
            return;
        }

        Button button = (Button)view;
        if (button.getText().equals("拉流")) {
            EditText et = findViewById(R.id.ed_play_stream_id);
            String streamID = et.getText().toString();
            playStreamID = streamID;
            View play_view = findViewById(R.id.remote_view);

            /** Begin to play stream */
            engine.startPlayingStream(playStreamID, new ZegoCanvas(play_view, ZegoViewMode.VIEW_MODE_ASPECT_FILL));
            engine.muteAudioOutput(playStreamMute);
            AppLogger.getInstance().i("调用拉流接口成功, 流ID = " + playStreamID);
            Toast.makeText(this, "调用拉流接口成功", Toast.LENGTH_SHORT).show();
            button.setText("停止拉流");
        }
        else {
            /** Begin to stop play stream */
            EditText et = findViewById(R.id.ed_play_stream_id);
            String streamID = et.getText().toString();
            engine.stopPlayingStream(streamID);
            //engine.stopPlayingStream(playStreamID);
            AppLogger.getInstance().i("调用停止拉流接口成功, 流ID = " + streamID);
            Toast.makeText(this, "调用停止拉流接口成功", Toast.LENGTH_SHORT).show();
            button.setText("拉流");
        }
    }

    public void enableLocalMic(View view) {
        if (engine == null) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
            return;
        }

        publishMicEnable = !publishMicEnable;

        if (publishMicEnable) {
            ib_local_mic.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        } else {
            ib_local_mic.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        }

        /* Enable Mic*/
        engine.enableMicrophone(publishMicEnable);
    }

    public void enableRemoteMic(View view) {
        if (engine == null) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
            return;
        }

        playStreamMute = !playStreamMute;

        if (playStreamMute) {
            ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        } else {
            ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        }

        /* Enable Mic*/
        engine.muteAudioOutput(playStreamMute);
    }

    /**
     * 校验并请求权限
     */
    public boolean checkOrRequestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 在应用内实现悬浮窗，需要依附Activity生命周期
        FloatingView.get().attach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // // 在应用内实现悬浮窗，需要依附Activity生命周期
        FloatingView.get().detach(this);
    }
}
