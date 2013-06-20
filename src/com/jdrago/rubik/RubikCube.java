package com.jdrago.rubik;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RubikCube
{
    public static final int ROTATE_SPEED = 5;
    public static final int FAST_ROTATE_SPEED = 30;

    public int rotateSpeed_ = ROTATE_SPEED;

    public static final int ACTION_ROT = 1;
    public static final int ACTION_LOOK = 2;
    public static final int ACTION_TILT = 3;

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

    public int rots_[] = new int[9];
    public int cubies_[][] = null;
    public int rot_;

    class Action
    {
        public int action;
        public int rot;
        public boolean clockwise;
    };

    private List<Action> actions_ = new ArrayList<Action>();
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
        Log.e("RUBIK", "unshuffle()");

        cubies_ = new int[6][9];
        for (int face = 0; face < 6; ++face)
        {
            for (int cubie = 0; cubie < 9; ++cubie)
            {
                cubies_[face][cubie] = face;
            }
        }
    }

    void shuffle()
    {
        unshuffle();

        for(int i = 0; i < 40; ++i)
        {
            int randomRot = (int)(Math.random() * 9);
            boolean randomCW = ((int)(Math.random() * 2) > 1);
            queue(ACTION_ROT, randomRot, randomCW);
        }

        rotateSpeed_ = FAST_ROTATE_SPEED;
    }

    void queue(int action, int rot, boolean clockwise)
    {
        Action a = new Action();
        a.action = action;
        a.rot = rot;
        a.clockwise = clockwise;
        actions_.add(a);
    }

    void update()
    {
        boolean animating = false;
        for (int rot = 0; rot < 9; ++rot)
        {
            if (rots_[rot] < 0)
            {
                rots_[rot] += rotateSpeed_;
                if (rots_[rot] > 0)
                    rots_[rot] = 0;
            } else if (rots_[rot] > 0)
            {
                rots_[rot] -= rotateSpeed_;
                if (rots_[rot] < 0)
                    rots_[rot] = 0;
            }

            if(rots_[rot] != 0)
            {
                animating = true;
            }
        }

        if (!animating)
        {
            if (actions_.isEmpty())
            {
                rotateSpeed_ = ROTATE_SPEED;
            }
            else
            {
                Action a = actions_.get(0);
                actions_.remove(0);

                if(a.action == ACTION_ROT)
                {
                    move(a.rot, a.clockwise);
                }
                else if(a.action == ACTION_LOOK)
                {
                    switch(a.rot)
                    {
                        case ROT_X:
                            move(ROT_L, !a.clockwise);
                            move(ROT_X, a.clockwise);
                            move(ROT_R, a.clockwise);
                            break;
                        case ROT_Y:
                            move(ROT_U, a.clockwise);
                            move(ROT_Y, a.clockwise);
                            move(ROT_D, !a.clockwise);
                            break;
                        case ROT_Z:
                            move(ROT_B, a.clockwise);
                            move(ROT_Z, !a.clockwise);
                            move(ROT_F, !a.clockwise);
                            break;
                    };
                }
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

        rots_[rot] = -90;
        if (clockwise)
        {
            rots_[rot] *= -1;
        }
    }
}