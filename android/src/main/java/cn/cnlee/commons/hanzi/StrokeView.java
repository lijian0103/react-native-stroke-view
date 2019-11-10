package cn.cnlee.commons.hanzi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StrokeView extends View {

    private final static String TAG = StrokeView.class.getSimpleName();
    private Paint mPaint;
    private Path[] xPaths;
    private Context context;

    private int currentStroke = 0;
    private Float currentStrokePhase = 2047.0f;
    private Float currentStrokeWidth = 0.0f;
    private Float currentStrokeLength = 0.0f;

    private SvgPath svgPath;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isRunning = false;
    private int speed = 50;//写字的速度设置
    private Float scale = 1.0f;//view宽度占屏幕宽度的比值
    private boolean loop = false;//是否循环播放
    private StrokeListener mListener;


    public StrokeView(Context context, StrokeListener listener) {
        super(context);
        this.context = context;
        this.mListener = listener;
        init();
    }

    public StrokeView(Context context, AttributeSet attrs, StrokeListener listener) {
        super(context, attrs);
        this.context = context;
        this.mListener = listener;
        init();
    }

    public StrokeView(Context context, AttributeSet attrs, int defStyleAttr, StrokeListener listener) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.mListener = listener;
        init();
    }

    /***
     * 初始化笔划数据
     * @param json
     */
    public void setJson(String json) {
        this.svgPath = new SvgPath(json);
        initData();
        initPaths();
        postInvalidate();
    }

    /***
     * 设置缩放比例
     * @param scale
     */
    public void setScale(Float scale) {
        this.scale = scale;
        Log.e(TAG, "scale: " + scale);
    }

    /***
     * 设置是否循环播放
     * @param loop
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
        Log.e(TAG, "loop: " + loop);
    }

    /***
     * 设置当前笔划是第几划
     * @param currentStroke
     */
    public void setCurrentStroke(int currentStroke) {
        this.currentStroke = currentStroke;
    }

    private void init() {
        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mTimer = new Timer(true);
    }

    /***
     *暂停
     */
    public void pause() {
        if (!isRunning)
            return;
        if (mTimerTask != null) {
            mTimerTask.cancel();//将原任务从队列中移除
            mTimerTask = null;
        }
        isRunning = false;
        if (this.mListener != null) {
            this.mListener.onStrokeStop();
        }
    }

    /***
     * 重置笔画，从头开始
     */
    public void reset() {
        currentStroke = 0;
        initData();
        postInvalidate();
    }

    /***
     * 改变播放速度
     * @param seek
     */
    public void seekSpeed(int seek) {
        this.speed = seek;
        Log.e(TAG, "speed: " + speed);
        if (isRunning) {
            playStrokeAnim();
        }
    }

    /**
     * 是否正在播放写字动画
     *
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    public void playStrokeAnim() {
        if (!isRunning) {
            if (mTimer != null) {
                pause();//先清理当前任务
                Log.d(TAG, "重新创建新任务");
            }else {
                mTimer = new Timer(true);
            }
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    //run方法中执行需要间隔执行的代码
                    drawStrokes();
                }
            };
            //0s后开始执行，间隔为50毫秒
            long period = 50;
//            Log.e(TAG, "间隔：" + period);
            mTimer.schedule(mTimerTask, 0, period);
            isRunning = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        pause();
        reset();
        super.onDetachedFromWindow();
    }

    public Float getStrokeRatio() {
        DisplayMetrics dm = this.context.getResources().getDisplayMetrics();
        return dm.widthPixels / 1024.0f * scale;
    }

    public Float getOffset() {
        return 900 * getStrokeRatio();
    }

    private Paint getFramePaint() {
        Paint xPaint = new Paint();
        xPaint.setAntiAlias(true);
        xPaint.setColor(Color.LTGRAY);
        long paintWidth = Math.round(6 * scale + 0.5);
//        Log.e(TAG, "边框画笔：" + paintWidth);
        xPaint.setStrokeWidth(paintWidth);
        xPaint.setStyle(Paint.Style.STROKE);
        return xPaint;
    }

    private void makeFrame(Canvas canvas) {
        Float ratio = getStrokeRatio();
        Path path = new Path();
        path.moveTo(1 * ratio, 1 * ratio);
        path.lineTo(1 * ratio, 1023 * ratio);
        path.lineTo(1023 * ratio, 1023 * ratio);
        path.lineTo(1023 * ratio, 1 * ratio);
        path.lineTo(1 * ratio, 1 * ratio);
        path.close();

        canvas.drawPath(path, getFramePaint());
    }

    private void makeCross1(Canvas canvas) {
        Float ratio = getStrokeRatio();
        Path path = new Path();
        path.moveTo(0, 512 * ratio);
        path.lineTo(1024 * ratio, 512 * ratio);
        path.moveTo(512 * ratio, 0);
        path.lineTo(512 * ratio, 1024 * ratio);

        Paint xPaint = getFramePaint();
        xPaint.setPathEffect(new DashPathEffect(new float[]{30.0f * ratio, 30.0f * ratio}, 0));
        canvas.drawPath(path, xPaint);
    }

    private void makeCross2(Canvas canvas) {
        Float ratio = getStrokeRatio();
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(1024 * ratio, 1024 * ratio);
        path.moveTo(0, 1024 * ratio);
        path.lineTo(1024 * ratio, 0);

        Paint xPaint = getFramePaint();
        xPaint.setPathEffect(new DashPathEffect(new float[]{30.0f * ratio, 30.0f * ratio}, 0));
        canvas.drawPath(path, xPaint);
    }

    /***
     * 笔画中线
     * @param index
     * @return
     */
    private Path makeMedian(int index) {
        Path path = new Path();
        if (this.svgPath.svgMedians == null) {
            return path;
        }
        List<List<List>> list = this.svgPath.svgMedians;
        List<List> finalPathArr = list.get(index);
        Float ratio = getStrokeRatio();
        Float offset = getOffset();

        for (int i = 0; i < finalPathArr.size(); i++) {
            List<Integer> arr = finalPathArr.get(i);
            Float x = arr.get(0) * ratio;
            Float y = arr.get(1) * ratio;
            if (i == 0) {
                path.moveTo(x, offset - y);
            } else {
                path.lineTo(x, offset - y);
            }
        }
        return path;
    }

    /***
     * 笔画数
     * @return
     */
    private int getStrokeCount() {
        return this.svgPath.strokeLength;
    }

    /***
     *每个笔画的长度
     * @param index
     * @return
     */
    private Float getStrokeLength(int index) {
        if (this.svgPath.svgMedians == null) {
            return 0f;
        }
        List<List<List>> list = this.svgPath.svgMedians;
        List<List> finalPathArr = list.get(index);
        Double distance = 0.0d;
        Float saveX = 0.0f;
        Float saveY = 0.0f;
        for (int i = 0; i < finalPathArr.size(); i++) {
            List<Integer> arr = finalPathArr.get(i);
            Float x = arr.get(0) * 1.0f;
            Float y = arr.get(1) * 1.0f;
            if (i > 0) {
                distance += Math.sqrt((x - saveX) * (x - saveX) + (y - saveY) * (y - saveY));
            }
            saveX = x;
            saveY = y;
        }
        return Float.parseFloat(distance + "");
    }

    /***
     * 当前笔画是否结束
     * @return
     */
    private boolean currentStrokeCompleted() {
        return 2047.0f - currentStrokePhase >= currentStrokeLength - 10.0f;
    }

    public void drawStrokes() {
        boolean isCompleted = currentStrokeCompleted();
        if (isCompleted) {
            if (currentStroke == this.svgPath.strokeLength - 1) {
                reset();
                if (!this.loop) {
                    pause();
                }
                return;
            }
            currentStroke++;
            initData();
            return;
        }
        Float ratio = this.getStrokeRatio();
        Float distance = this.speed * ratio;
        if (currentStrokeWidth < 150.0f) {
            currentStrokeWidth += 2.0f * distance / ratio;
        } else if (!currentStrokeCompleted()) {
            currentStrokePhase -= distance / ratio;
        }
        postInvalidate();
    }

    /***
     * 初始化一些数据
     */
    private void initData() {
        currentStrokeWidth = 0.0f;
        currentStrokePhase = 2047.0f;
        currentStrokeLength = getStrokeLength(currentStroke);
    }

    private void initPaths() {
        int size = this.svgPath.strokeLength;
        xPaths = new Path[size];
        try {
            SvgPathUtils svgPathUtils = new SvgPathUtils(getStrokeRatio(), getOffset());
            List<String> svgPaths = this.svgPath.svgStrokes;
            for (int i = 0; i < size; i++) {
                String svgPath2 = svgPaths.get(i);
                Path path = svgPathUtils.parse(svgPath2);
                xPaths[i] = path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Path makePath(int index) {
        try {
            return xPaths[index];
        } catch (Exception e) {
            e.printStackTrace();
            return new Path();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        makeFrame(canvas);
        makeCross1(canvas);
        makeCross2(canvas);

        if (xPaths == null) return;
        //绘制路径
        for (int i = xPaths.length - 1; i >= 0; i--) {
            mPaint.setStyle(Paint.Style.FILL);
            if (i < currentStroke) {
                mPaint.setColor(Color.BLACK);
            } else if (i == currentStroke) {
                mPaint.setColor(Color.RED);
            } else {
                mPaint.setColor(Color.LTGRAY);
            }

            canvas.drawPath(xPaths[i], mPaint);
        }

        if (currentStroke < getStrokeCount()) {
            Path path = makePath(currentStroke);

            Float ratio = getStrokeRatio();
            Paint xPaint2 = new Paint();
            xPaint2.setAntiAlias(true);
            xPaint2.setColor(Color.BLUE);
            xPaint2.setStrokeCap(Paint.Cap.ROUND);
            xPaint2.setStrokeWidth(currentStrokeWidth * ratio);
            xPaint2.setStyle(Paint.Style.STROKE);
            xPaint2.setPathEffect(new DashPathEffect(new float[]{2048.0f * ratio, 2048.0f * ratio}, currentStrokePhase * ratio));
            Path path2 = makeMedian(currentStroke);
            canvas.clipPath(path);
            canvas.drawPath(path2, xPaint2);
        }
    }

    //宽高相等，正方形
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取宽度的模式和尺寸
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //根据宽高比ratio和模式创建一个测量值
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        //必须调用下面的两个方法之一完成onMeasure方法的重写，否则会报错
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.round(getStrokeRatio() * 1024.0f);
        setMeasuredDimension(size, size);
    }

    class SvgPath {
        public List<String> svgStrokes;
        public List<List<List>> svgMedians;
        public int strokeLength;

        public SvgPath(String json) {
            Map<String, Object> jsonObject = JSON.parseObject(json);
            this.svgStrokes = (List<String>) jsonObject.get("strokes");
            this.svgMedians = (List<List<List>>) jsonObject.get("medians");
            this.strokeLength = this.svgStrokes != null ? this.svgStrokes.size() : 0;
        }
    }

    interface StrokeListener {
        void onStrokeStop();
    }
}

