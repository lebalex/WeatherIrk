package xyz.lebalex.weatherirk;

import android.os.Binder;

/**
 * Created by ivc_lebedevav on 30.01.2017.
 */

public class ObjectWrapperForBinder extends Binder {

    private final Object mData;

    public ObjectWrapperForBinder(Object data) {
        mData = data;
    }

    public Object getData() {
        return mData;
    }
}
