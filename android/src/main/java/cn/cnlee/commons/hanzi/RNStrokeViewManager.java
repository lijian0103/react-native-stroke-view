package cn.cnlee.commons.hanzi;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Map;

import javax.annotation.Nullable;

public class RNStrokeViewManager extends SimpleViewManager implements StrokeView.StrokeListener {

    private static final String TAG = "StrokeView";
    private FrameLayout mContainer;
    private RCTEventEmitter mEventEmitter;
    private ThemedReactContext mThemedReactContext;
    private StrokeView mStrokeView;

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    protected View createViewInstance(ThemedReactContext reactContext) {
        mThemedReactContext = reactContext;
        mEventEmitter = reactContext.getJSModule(RCTEventEmitter.class);
        mStrokeView = new StrokeView(mThemedReactContext.getCurrentActivity(), this);
        return mStrokeView;
    }

    @ReactProp(name = "data")
    public void setData(StrokeView view, String json) {
        view.setJson(json);
    }

    @ReactProp(name = "play")
    public void setPlay(StrokeView view, boolean play) {
        Log.e(TAG, "setPlay: " + play);
        if (play) {
            view.playStrokeAnim();
        }else {
            view.pause();
        }
    }

    /***
     * reset 大于0 执行
     * @param view
     * @param reset
     */
    @ReactProp(name = "reset")
    public void setReset(StrokeView view, int reset) {
        Log.e(TAG, "======setReset======" + reset);
        if (reset > 0) {
            view.reset();
        }
    }

    @ReactProp(name = "current")
    public void setCurrent(StrokeView view, int current) {
        if (current > -1) {
            view.setCurrentStroke(current);
        }
    }

    @ReactProp(name = "speed")
    public void setSpeed(StrokeView view, int speed) {
        if (speed > -1) {
            view.seekSpeed(speed);
        }
    }

    @ReactProp(name = "scale")
    public void setScale(StrokeView view, float scale) {
        if (scale > 0) {
            view.setScale(scale);
        }
    }

    @ReactProp(name = "loop")
    public void setLoop(StrokeView view, boolean loop) {
        if (loop) {
            view.setLoop(true);
        }
    }

    @Override
    public void onStrokeStop() {
        Log.e(TAG,"====onStrokeStop====" + mStrokeView.getId());
        mEventEmitter.receiveEvent(mStrokeView.getId(), Events.EVENT_NOT_RUNNING.toString(), null);
    }

    public enum Events {

        EVENT_NOT_RUNNING("onStrokeStop");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }


    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (Events event : Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }
}