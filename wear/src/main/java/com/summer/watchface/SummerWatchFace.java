package com.summer.watchface;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import com.blankj.utilcode.util.LogUtils;
import com.summer.watchface.data.net.BaseCallBack;
import com.summer.watchface.data.net.ListData;

import java.util.concurrent.TimeUnit;

public class SummerWatchFace extends CanvasWatchFaceService {

    int width;
    int height;

    Handler handler = new Handler();

    private static final long UPDATE_TIME = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new summerEngine();
    }

    public class summerEngine extends CanvasWatchFaceService.Engine{


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            LogUtils.e("onCreate");
            setWatchFaceStyle(new WatchFaceStyle.Builder(SummerWatchFace.this).setAcceptsTapEvents(true).build());

        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            LogUtils.e("onSurfaceCreated");
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            LogUtils.e("onSurfaceChanged");
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            LogUtils.e("onDraw");
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            LogUtils.e("onDestroy");
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            super.onTapCommand(tapType, x, y, eventTime);
            LogUtils.e("onTapCommand");
        }

        private void update(){
            invalidate();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    update();
                }
            },UPDATE_TIME-(System.currentTimeMillis() % UPDATE_TIME));
        }
    }
}
