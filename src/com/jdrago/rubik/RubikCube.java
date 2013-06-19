package com.jdrago.rubik;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RubikCube
{
    public static final int ROTATE_SPEED = 5;

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
    private int stripe_[] = new int[3];

    private int SPIN_INDICES[][] =
    {
            {FACE_F,0,1,2, FACE_L,0,1,2, FACE_B,0,1,2, FACE_R,0,1,2}, // ROT_U
            {FACE_F,6,7,8, FACE_R,6,7,8, FACE_B,6,7,8, FACE_L,6,7,8}, // ROT_D
            {FACE_U,0,3,6, FACE_F,0,3,6, FACE_D,0,3,6, FACE_B,8,5,2}, // ROT_L
            {FACE_U,8,5,2, FACE_B,0,3,6, FACE_D,8,5,2, FACE_F,8,5,2}, // ROT_R
            {FACE_U,2,1,0, FACE_L,0,3,6, FACE_D,6,7,8, FACE_R,8,5,2}, // ROT_B
            {FACE_U,6,7,8, FACE_R,0,3,6, FACE_D,2,1,0, FACE_L,8,5,2}, // ROT_F

            {FACE_U,7,4,1, FACE_B,1,4,7, FACE_D,7,4,1, FACE_F,7,4,1}, // ROT_X
            {FACE_F,3,4,5, FACE_L,3,4,5, FACE_B,3,4,5, FACE_R,3,4,5}, // ROT_Y
            {FACE_U,3,4,5, FACE_R,1,4,7, FACE_D,5,4,3, FACE_L,7,4,1}  // ROT_Z
    };

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
                cubies_[face][cubie] = face;
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

    void rotateFace(int face)
    {
        int t = cubies_[face][0];
        cubies_[face][0] = cubies_[face][6];
        cubies_[face][6] = cubies_[face][8];
        cubies_[face][8] = cubies_[face][2];
        cubies_[face][2] = t;

        t = cubies_[face][1];
        cubies_[face][1] = cubies_[face][3];
        cubies_[face][3] = cubies_[face][7];
        cubies_[face][7] = cubies_[face][5];
        cubies_[face][5] = t;
    }

    void spin(int indices[])
    {
        int face0 = indices[0];
        int face1 = indices[4];
        int face2 = indices[8];
        int face3 = indices[12];

        stripe_[0] = cubies_[face3][indices[13]];
        stripe_[1] = cubies_[face3][indices[14]];
        stripe_[2] = cubies_[face3][indices[15]];

        cubies_[face3][indices[13]] = cubies_[face2][indices[9]];
        cubies_[face3][indices[14]] = cubies_[face2][indices[10]];
        cubies_[face3][indices[15]] = cubies_[face2][indices[11]];

        cubies_[face2][indices[9]] = cubies_[face1][indices[5]];
        cubies_[face2][indices[10]] = cubies_[face1][indices[6]];
        cubies_[face2][indices[11]] = cubies_[face1][indices[7]];

        cubies_[face1][indices[5]] = cubies_[face0][indices[1]];
        cubies_[face1][indices[6]] = cubies_[face0][indices[2]];
        cubies_[face1][indices[7]] = cubies_[face0][indices[3]];

        cubies_[face0][indices[1]] = stripe_[0];
        cubies_[face0][indices[2]] = stripe_[1];
        cubies_[face0][indices[3]] = stripe_[2];
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
            if(rot < 6)
            {
                rotateFace(rot);
            }
            spin(SPIN_INDICES[rot]);
        }

        rot_ = rot;
        angle_ = -90;
        if (clockwise)
        {
            angle_ *= -1;
        }
    }
}