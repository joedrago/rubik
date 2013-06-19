package com.jdrago.rubik;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RubikCube
{
    public static final int ROTATE_SPEED = 1;

    public static final int FACE_U = 0;
    public static final int FACE_D = 1;
    public static final int FACE_L = 2;
    public static final int FACE_R = 3;
    public static final int FACE_B = 4;
    public static final int FACE_F = 5;

    public static final int ROT_U = 0;
    public static final int ROT_D = 1;
    public static final int ROT_L = 2;
    public static final int ROT_R = 3;
    public static final int ROT_B = 4;
    public static final int ROT_F = 5;
    public static final int ROT_X = 6;
    public static final int ROT_Y = 7;
    public static final int ROT_Z = 8;

    public int cubies_[][] = new int[6][9];
    public int angle_;
    public int rot_;

    private List<Integer> actions_ = new ArrayList<Integer>();

    RubikCube()
    {
        unshuffle();
    }

    void unshuffle()
    {
        for (int face = 0; face < 6; ++face)
        {
            for (int cubie = 0; cubie < 9; ++cubie)
            {
                cubies_[face][cubie] = cubie;//face;
            }
        }

        angle_ = 0;
        rot_ = 0;
    }

    void queue(int rot, boolean clockwise)
    {
        int rawAction = rot * 2;
        if (clockwise)
        {
            rawAction += 1;
        }
        actions_.add(rawAction);
    }

    void update()
    {
        if (angle_ < 0)
        {
            angle_ += ROTATE_SPEED;
            if (angle_ > 0)
                angle_ = 0;
        } else if (angle_ > 0)
        {
            angle_ -= ROTATE_SPEED;
            if (angle_ < 0)
                angle_ = 0;
        }

        if (angle_ == 0)
        {
            if (!actions_.isEmpty())
            {
                int rawAction = actions_.get(0);
                actions_.remove(0);

                Log.e("RubikCube", "Performing action: " + rawAction);

                int rot = rawAction / 2;
                boolean clockwise = (rawAction % 2) > 0;

                move(rot, clockwise);
            }
        }
    }

    void move(int rot, boolean clockwise)
    {
        int numRotations = 1;
        if(!clockwise)
        {
            numRotations = 3;
        }

        for (int i = 0; i < numRotations; ++i)
        {
            switch (rot)
            {
                case ROT_F:
                {
                }
                break;
            }
        }

        rot_ = rot;
        angle_ = -90;
        if (clockwise)
        {
            angle_ *= -1;
        }
    }
}