package com.summer.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.palette.graphics.Palette;

import android.os.Vibrator;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.summer.watchface.data.net.BaseCallBack;
import com.summer.watchface.data.net.ListData;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class MyWatchFace extends CanvasWatchFaceService {

    public static String 现在显示内容 = "";

    public static String 接下来显示内容 = "";

    public static String 电池电量 = "";

    private static final long 更新时间 = TimeUnit.MINUTES.toMillis(1);

    private static final int 刷新通知 = 0;

    ArrayList<Alarm> 闹铃数据 =new ArrayList<>();

    public static Vibrator 震动器;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {

        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case 刷新通知:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private static final float HOUR_STROKE_WIDTH = 2f;
        private static final float MINUTE_STROKE_WIDTH = 3f;
        private static final float SECOND_TICK_STROKE_WIDTH = 2f;
        private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 4f;
        private static final int SHADOW_RADIUS = 6;
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;
        private float mCenterX;
        private float mCenterY;
        private float mSecondHandLength;
        private float sMinuteHandLength;
        private float sHourHandLength;
        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private int mWatchHandColor;
        private int mWatchHandHighlightColor;
        private int mWatchHandShadowColor;

        private Paint mTextPaint;
        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint bgLinePaint;
        private Paint mTickAndCirclePaint;
        private Paint mBackgroundPaint;
        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private float width;
        private float height;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            getNetData();
            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this).setAcceptsTapEvents(true).build());
            mCalendar = Calendar.getInstance();
            //new AlarmDE().setAlarm(getApplicationContext(),2*1000);
            initializeBackground();
            initializeWatchFace();
        }

        public void getNetData(){
            XNet.getInstance().selectAllAlarms().enqueue(new BaseCallBack<ListData<Alarm>>(){
                @Override
                public void onSuccess(ListData<Alarm> alarmListData) {
                    super.onSuccess(alarmListData);
                    LogUtils.e(GsonUtils.toJson(alarmListData));
                    SPUtils.getInstance().put("clock", GsonUtils.toJson(alarmListData.getData()));
                    if(闹铃数据.size()==0){
                        ArrayList<Alarm> list  = new Gson().fromJson(SPUtils.getInstance().getString("clock"),new TypeToken<ArrayList<Alarm>>(){}.getType());
                        if(list!=null){
                            闹铃数据.clear();
                            闹铃数据.addAll(list);
                        }
                    }
                    ToastUtils.showLong(闹铃数据.size()+"--"+SPUtils.getInstance().getString("clock"));
                    Calendar calendar = Calendar.getInstance();
                    for(int i = 0; i< 闹铃数据.size(); i++){
                        calendar.setTime(new Date(闹铃数据.get(i).getStarttime()));
                        闹铃数据.get(i).setStart(calendar.get(Calendar.HOUR_OF_DAY)*60+ calendar.get(Calendar.MINUTE));
                        calendar.setTime(new Date(闹铃数据.get(i).getEndtime()));
                        闹铃数据.get(i).setEnd(calendar.get(Calendar.HOUR_OF_DAY)*60+ calendar.get(Calendar.MINUTE));
                        Log.e("3333", 闹铃数据.get(i).getStart()+"");
                    }
                }

                @Override
                public void onError(int code, String error) {
                    super.onError(code, error);
                    LogUtils.e(error);
                    ToastUtils.showLong(error);
                }
            });
        }

        private void initializeBackground() {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundPaint.setAntiAlias(true);
            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a);

            /* Extracts colors from background image to improve watchface style. */
            Palette.from(mBackgroundBitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    if (palette != null) {
                        mWatchHandHighlightColor = palette.getVibrantColor(Color.RED);
                        mWatchHandColor = palette.getLightVibrantColor(Color.WHITE);
                        mWatchHandShadowColor = palette.getDarkMutedColor(Color.BLACK);
                        updateWatchHandStyle();
                    }
                }
            });
        }

        private void initializeWatchFace() {
            /* Set defaults for colors */
            mWatchHandColor = Color.WHITE;
            mWatchHandHighlightColor =mWatchHandColor;
            mWatchHandShadowColor = Color.BLACK;

            bgLinePaint = new Paint();
            bgLinePaint.setColor(Color.WHITE);
            bgLinePaint.setStrokeWidth(1f);
            bgLinePaint.setAntiAlias(true);
            bgLinePaint.setStyle(Paint.Style.STROKE);
            //bgLinePaint.setStrokeCap(Paint.Cap.ROUND);
            //bgLinePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mHourPaint = new Paint();
            mHourPaint.setColor(mWatchHandColor);
            mHourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
            mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTextPaint = new Paint();
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
            mTextPaint.setTextSize(14);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setStrokeCap(Paint.Cap.ROUND);
            mTextPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            mTextPaint.setTextAlign(Paint.Align.CENTER);

            mMinutePaint = new Paint();
            mMinutePaint.setColor(mWatchHandColor);
            mMinutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mSecondPaint = new Paint();
            mSecondPaint.setColor(mWatchHandHighlightColor);
            mSecondPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
            mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTickAndCirclePaint = new Paint();
            mTickAndCirclePaint.setColor(mWatchHandColor);
            mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickAndCirclePaint.setAntiAlias(true);
            mTickAndCirclePaint.setStyle(Paint.Style.STROKE);
            mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(刷新通知);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

            updateWatchHandStyle();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void updateWatchHandStyle() {
            if (mAmbient) {
                mHourPaint.setColor(Color.WHITE);
                mMinutePaint.setColor(Color.WHITE);
                mSecondPaint.setColor(Color.WHITE);
                mTickAndCirclePaint.setColor(Color.WHITE);

                mHourPaint.setAntiAlias(false);
                mMinutePaint.setAntiAlias(false);
                mSecondPaint.setAntiAlias(false);
                mTickAndCirclePaint.setAntiAlias(false);

                mHourPaint.clearShadowLayer();
                mMinutePaint.clearShadowLayer();
                mSecondPaint.clearShadowLayer();
                mTickAndCirclePaint.clearShadowLayer();

            } else {
                mHourPaint.setColor(mWatchHandColor);
                mMinutePaint.setColor(mWatchHandColor);
                mSecondPaint.setColor(mWatchHandHighlightColor);
                mTickAndCirclePaint.setColor(mWatchHandColor);

                mHourPaint.setAntiAlias(true);
                mMinutePaint.setAntiAlias(true);
                mSecondPaint.setAntiAlias(true);
                mTickAndCirclePaint.setAntiAlias(true);

                mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            this.width = width;
            this.height = height;
            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = (float) (mCenterX * 0.875);
            sMinuteHandLength = (float) (mCenterX * 0.75);
            if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=12){
                sHourHandLength = (float) (mCenterX*0.6);
            }else{
                sHourHandLength = (float) (mCenterX*0.8);
            }

            /* Scale loaded background image (more efficient) if surface dimensions change. */
            float scale = ((float) width) / (float) mBackgroundBitmap.getWidth();

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, (int) (mBackgroundBitmap.getWidth() * scale),(int) (mBackgroundBitmap.getHeight() * scale), true);

            /*
             * Create a gray version of the image only if it will look nice on the device in
             * ambient mode. That means we don't want devices that support burn-in
             * protection (slight movements in pixels, not great for images going all the way to
             * edges) and low ambient mode (degrades image quality).
             *
             * Also, if your watch face will know about all images ahead of time (users aren't
             * selecting their own photos for the watch face), it will be more
             * efficient to create a black/white version (png, etc.) and load that when you need it.
             */
            if (!mBurnInProtection && !mLowBitAmbient) {
                initGrayBackgroundBitmap();
            }
        }

        private void initGrayBackgroundBitmap() {
            mGrayBackgroundBitmap = Bitmap.createBitmap(
                    mBackgroundBitmap.getWidth(),
                    mBackgroundBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mGrayBackgroundBitmap);
            Paint grayPaint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
            grayPaint.setColorFilter(filter);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, grayPaint);
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            LogUtils.e(x+";"+y);
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    if(x>width-50&&y<=50){
                        getNetData();
                    }
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            if(!isVisible()){
                return;
            }
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            drawBackground(canvas);
            drawWatchFace(canvas);
            drawText(canvas);
        }

        private void drawBackground(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);
            } else if (mAmbient) {
                canvas.drawBitmap(mGrayBackgroundBitmap, 0, (height-mBackgroundBitmap.getHeight())/2 , mBackgroundPaint);
            } else {
                canvas.drawBitmap(mBackgroundBitmap, 0,(height-mBackgroundBitmap.getHeight())/2, mBackgroundPaint);
            }

//            canvas.drawCircle(width/2,height/2,width/2,bgLinePaint);//画圆框
//            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
//                float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
//                float innerX = (float) Math.sin(tickRot) * (mCenterX - 10);
//                float innerY = (float) -Math.cos(tickRot) * (mCenterX - 10);
//                float outerX = (float) Math.sin(tickRot) * mCenterX;
//                float outerY = (float) -Math.cos(tickRot) * mCenterX;
//                canvas.drawLine(mCenterX + innerX, mCenterY + innerY,mCenterX + outerX, mCenterY + outerY, mTickAndCirclePaint);
//            }

        }

        private void drawWatchFace(Canvas canvas) {


            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = seconds * 6f;

            final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f;

            final float hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f;
            final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + hourHandOffset;

            /*
             * Save the canvas state before we can begin to rotate it.
             */
            canvas.save();

            canvas.rotate(hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(mCenterX,mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,mCenterX,mCenterY - sHourHandLength,mHourPaint);
            //canvas.drawCircle(mCenterX,mCenterY - sHourHandLength,2f,mMinutePaint);

//            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
//            canvas.drawLine( mCenterX,mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,mCenterX,mCenterY - sMinuteHandLength,mMinutePaint);

            /*
             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise, we only update the watch face once a minute.
             */
//            if (!mAmbient) {
//                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
//                canvas.drawLine(
//                        mCenterX,
//                        mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
//                        mCenterX,
//                        mCenterY - mSecondHandLength,
//                        mSecondPaint);
//
//            }

            canvas.drawCircle(mCenterX,mCenterY,CENTER_GAP_AND_CIRCLE_RADIUS,mTickAndCirclePaint);

            /* Restore the canvas' original orientation. */
            canvas.restore();
        }

        private void drawText(Canvas canvas){
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(现在显示内容,width/2,(height-mBackgroundBitmap.getHeight())/2-10,mTextPaint);
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(电池电量,width-30,(height-mBackgroundBitmap.getHeight())/2-10,mTextPaint);
            canvas.drawText(接下来显示内容,width/2,height-10,mTextPaint);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH  mm");
            canvas.drawText(simpleDateFormat.format(new Date()),width/2,height/2+30,mTextPaint);
            int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            String weektext = "";
            switch (week){
                case 0:
                    weektext = "星期天";
                    break;
                case 1:
                    weektext = "星期一";
                    break;
                case 2:
                    weektext = "星期二";
                    break;
                case 3:
                    weektext = "星期三";
                    break;
                case 4:
                    weektext = "星期四";
                    break;
                case 5:
                    weektext = "星期五";
                    break;
                case 6:
                    weektext = "星期六";
                    break;
            }
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(weektext,width-30,height-10,mTextPaint);
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText((Calendar.getInstance().get(Calendar.DAY_OF_MONTH))+"日",30,height-10,mTextPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(刷新通知);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(刷新通知);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            int b = checked(闹铃数据);
            if (b != -1) {
                if (震动器 == null) {
                    震动器 = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                }
                震动器.vibrate(new long[]{1000l, 1000l, 1000l, 1000l}, -1);
            }
            if (shouldTimerBeRunning()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                int c = checkedArea(闹铃数据);
                if (c != -1) {
                    simpleDateFormat.setTimeZone(TimeZone.getDefault());
                    //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    MyWatchFace.现在显示内容 = "" + 闹铃数据.get(c).getText() + "(" + simpleDateFormat.format(new Date(闹铃数据.get(c).getStarttime())) + "--" + simpleDateFormat.format(new Date(闹铃数据.get(c).getEndtime())) + ")";
                }else{
                    MyWatchFace.现在显示内容 = "空闲";
                }
                int next = checkedNext(闹铃数据);
                if(next!=-1){
                    MyWatchFace.接下来显示内容 = 闹铃数据.get(next).getText() + "(" + simpleDateFormat.format(new Date(闹铃数据.get(next).getStarttime())) + "--" + simpleDateFormat.format(new Date(闹铃数据.get(next).getEndtime())) + ")";
                }else{
                    MyWatchFace.接下来显示内容 = "空闲";
                }

                BatteryManager batteryManager = (BatteryManager)getSystemService(BATTERY_SERVICE);
                int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                电池电量 = battery+"%";
            }
            invalidate();
            long timeMs = System.currentTimeMillis();
            long delayMs = 更新时间- (timeMs % 更新时间);
            mUpdateTimeHandler.sendEmptyMessageDelayed(刷新通知, delayMs);



        }


        public int checked(ArrayList<Alarm> datas){
            int  b= -1;
            Calendar calendar = Calendar.getInstance();
            int nowmin = calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
            for(int i=0;datas!=null&&i<datas.size();i++){
                if(nowmin==datas.get(i).getStart()){
                    b= i;
                }
            }
            return b;
        }

        public int checkedArea(ArrayList<Alarm> datas){
            int  b= -1;
            Calendar calendar = Calendar.getInstance();
            int nowmin = calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
            for(int i=0;datas!=null&&i<datas.size();i++){
                if(nowmin>=datas.get(i).getStart()&&nowmin<=datas.get(i).getEnd()){
                    b= i;
                    return b;
                }
            }
            return b;
        }

        public int checkedNext(ArrayList<Alarm> datas){
            int  b= -1;
            Calendar calendar = Calendar.getInstance();
            int nowmin = calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
            for(int i=0;datas!=null&&i<datas.size();i++){
                LogUtils.e(datas.get(i).getText());
                if(nowmin<=datas.get(i).getStart()){
                    b= i;
                    return b;
                }
            }
            return b;
        }
    }
}
