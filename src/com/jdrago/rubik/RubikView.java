package com.jdrago.rubik;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class RubikView extends GLSurfaceView
{
    private RubikRenderer renderer_;
    private RubikCube cube_;

    public RubikView(Context context)
    {
        super(context);
        setEGLContextClientVersion(2);
        cube_ = new RubikCube();

//        cube_.queue(RubikCube.ROT_U, false);
//        cube_.queue(RubikCube.ROT_U, true);
//        cube_.queue(RubikCube.ROT_D, false);
//        cube_.queue(RubikCube.ROT_D, true);
//        cube_.queue(RubikCube.ROT_L, false);
//        cube_.queue(RubikCube.ROT_L, true);
//        cube_.queue(RubikCube.ROT_R, false);
//        cube_.queue(RubikCube.ROT_R, true);
//        cube_.queue(RubikCube.ROT_F, false);
//        cube_.queue(RubikCube.ROT_F, true);
//        cube_.queue(RubikCube.ROT_B, false);
//        cube_.queue(RubikCube.ROT_B, true);
//        cube_.queue(RubikCube.ROT_X, false);
//        cube_.queue(RubikCube.ROT_X, true);
//        cube_.queue(RubikCube.ROT_Y, false);
//        cube_.queue(RubikCube.ROT_Y, true);
//        cube_.queue(RubikCube.ROT_Z, false);
//        cube_.queue(RubikCube.ROT_Z, true);

//        cube_.queue(RubikCube.ROT_U, true);
//        cube_.queue(RubikCube.ROT_U, true);
//        cube_.queue(RubikCube.ROT_U, true);
//        cube_.queue(RubikCube.ROT_U, true);
//        cube_.queue(RubikCube.ROT_D, true);
//        cube_.queue(RubikCube.ROT_D, true);
//        cube_.queue(RubikCube.ROT_D, true);
//        cube_.queue(RubikCube.ROT_D, true);
//        cube_.queue(RubikCube.ROT_L, true);
//        cube_.queue(RubikCube.ROT_L, true);
//        cube_.queue(RubikCube.ROT_L, true);
//        cube_.queue(RubikCube.ROT_L, true);
//        cube_.queue(RubikCube.ROT_R, true);
//        cube_.queue(RubikCube.ROT_R, true);
//        cube_.queue(RubikCube.ROT_R, true);
//        cube_.queue(RubikCube.ROT_R, true);
//        cube_.queue(RubikCube.ROT_F, true);
//        cube_.queue(RubikCube.ROT_F, true);
//        cube_.queue(RubikCube.ROT_F, true);
//        cube_.queue(RubikCube.ROT_F, true);
//        cube_.queue(RubikCube.ROT_B, true);
//        cube_.queue(RubikCube.ROT_B, true);
//        cube_.queue(RubikCube.ROT_B, true);
//        cube_.queue(RubikCube.ROT_B, true);
//        cube_.queue(RubikCube.ROT_X, true);
//        cube_.queue(RubikCube.ROT_X, true);
//        cube_.queue(RubikCube.ROT_X, true);
//        cube_.queue(RubikCube.ROT_X, true);
//        cube_.queue(RubikCube.ROT_Y, true);
//        cube_.queue(RubikCube.ROT_Y, true);
//        cube_.queue(RubikCube.ROT_Y, true);
//        cube_.queue(RubikCube.ROT_Y, true);
//        cube_.queue(RubikCube.ROT_Z, true);
//        cube_.queue(RubikCube.ROT_Z, true);
//        cube_.queue(RubikCube.ROT_Z, true);
//        cube_.queue(RubikCube.ROT_Z, true);

        renderer_ = new RubikRenderer(context, cube_);
        setRenderer(renderer_);
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        renderer_.click((int)event.getX(0), (int)event.getY(0));
        return true;
    }
}