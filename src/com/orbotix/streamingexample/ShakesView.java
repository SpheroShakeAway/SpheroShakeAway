package com.orbotix.streamingexample;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Orbotix Inc.
 * Date: 4/30/12
 *
 * @author Adam Williams
 */
public class ShakesView extends RelativeLayout {

    private TextView shakesCount;
    
    public ShakesView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.shakes_view, this);

        shakesCount = (TextView)findViewById(R.id.shakescount_text);

        if(attrs != null){

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShakesView);

            if(a.hasValue(R.styleable.ShakesView_android_text)){
                setShakesCount(a.getString(R.styleable.ShakesView_android_text));
            }
        }
    }

    public void setShakesCount(String text){

        shakesCount.setText(text);
    }
}
