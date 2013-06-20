package com.jdrago.rubik;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class RubikView extends GLSurfaceView
{
    private RubikRenderer renderer_;
    public RubikCube cube_;

    public RubikView(Context context)
    {
        super(context);
        setEGLContextClientVersion(2);
        cube_ = new RubikCube();
        renderer_ = new RubikRenderer(context, cube_);
        setRenderer(renderer_);
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            renderer_.click((int)event.getX(0), (int)event.getY(0));
        }
        return true;
    }
}