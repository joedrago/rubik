package com.jdrago.rubik;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RubikRenderer implements GLSurfaceView.Renderer
{
    private static String TAG = "RubikRenderer";

    private Context context_;
    private RubikCube cube_;

    private int width_;
    private int height_;

    private int shaderProgram_;
    private int viewProjMatrixHandle_;
    private int posHandle_;
    private int texHandle_;
    private int vertColorHandle_;

    private int spinX_;
    private int spinY_;
    private int spinZ_;

    private int cubieID_;

    private int rots_[] = new int[9];

    private float[] viewProjMatrix_ = new float[16];
    private float[] projMatrix_ = new float[16];
    private float[] modelMatrix_ = new float[16];
    private float[] viewMatrix_ = new float[16];
    private float[] tempRotMatrix_ = new float[16];
    private float[] tempMatrix_ = new float[16];

    private static final float CUBE_POS_Y = -2.0f;
    private static final float CUBE_POS_Z = 8.0f;

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int INT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private final float[][] faceColors_ =
            {
                    {1.0f, 1.0f, 0.0f},
                    {1.0f, 1.0f, 1.0f},
                    {0.0f, 0.0f, 1.0f},
                    {0.0f, 1.0f, 0.0f},
                    {1.0f, 0.5f, 0.0f},
                    {1.0f, 0.0f, 0.0f}

//                    {0.1f, 0.0f, 0.0f},
//                    {0.2f, 0.0f, 0.0f},
//                    {0.3f, 0.0f, 0.0f},
//                    {0.4f, 0.0f, 0.0f},
//                    {0.5f, 0.0f, 0.0f},
//                    {0.6f, 0.0f, 0.0f},
//                    {0.7f, 0.0f, 0.0f},
//                    {0.8f, 0.0f, 0.0f},
//                    {0.9f, 0.0f, 0.0f},
//                    {1.0f, 0.0f, 0.0f}
            };

    private final float[] whiteVertData_ = {
            // X, Y, Z, U, V
            -1.5f, -1.5f, -1.5f, 0, 0,
            -0.5f, -1.5f, -1.5f, 1, 0,
            -0.5f, -0.5f, -1.5f, 1, 1,
            -1.5f, -0.5f, -1.5f, 0, 1};
    private FloatBuffer cubieVerts_;

    private final int[] quadIndicesData_ = {0, 1, 2, 2, 3, 0};
    private IntBuffer quadIndices_;

    private final String vertShader_ =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "uniform vec4 u_color;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    private final String fragShader_ =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "uniform vec4 u_color;\n" +
                    "void main() {\n" +
                    "vec4 t = texture2D(sTexture, vTextureCoord);" +
                    "gl_FragColor.rgba = u_color.rgba * t.rgba;\n" +
                    "}\n";

    private int loadShader(int shaderType, String source)
    {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0)
        {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0)
            {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource)
    {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0)
        {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0)
        {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0)
        {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE)
            {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public int loadPNG(int res)
    {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        int id = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        InputStream is = context_.getResources().openRawResource(res);
        Bitmap bitmap;
        try
        {
            bitmap = BitmapFactory.decodeStream(is);
        } finally
        {
            try
            {
                is.close();
            } catch (IOException e)
            {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return id;
    }

    public RubikRenderer(Context context, RubikCube cube)
    {
        context_ = context;
        cube_ = cube;
        cubieVerts_ = ByteBuffer.allocateDirect(whiteVertData_.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubieVerts_.put(whiteVertData_).position(0);
        quadIndices_ = ByteBuffer.allocateDirect(quadIndicesData_.length * INT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        quadIndices_.put(quadIndicesData_).position(0);

        spinX_ = 45;
        spinY_ = 35;
        spinZ_ = 0;
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        shaderProgram_ = createProgram(vertShader_, fragShader_);
        if (shaderProgram_ == 0)
        {
            return;
        }
        posHandle_ = GLES20.glGetAttribLocation(shaderProgram_, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (posHandle_ == -1)
        {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        texHandle_ = GLES20.glGetAttribLocation(shaderProgram_, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (texHandle_ == -1)
        {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        viewProjMatrixHandle_ = GLES20.glGetUniformLocation(shaderProgram_, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (viewProjMatrixHandle_ == -1)
        {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        vertColorHandle_ = GLES20.glGetUniformLocation(shaderProgram_, "u_color");
        checkGlError("glGetUniformLocation vertColorHandle");
        if (vertColorHandle_ == -1)
        {
            throw new RuntimeException("Could not get attrib location for vertColorHandle");
        }

        cubieID_ = loadPNG(R.raw.cubie);
        Matrix.setLookAtM(viewMatrix_, 0,
                0, 0, 1,         // eye
                0f, 0f, 5f,       // center
                0f, 1.0f, 0.0f);  // up
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        width_ = width;
        height_ = height;

        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(projMatrix_, 0, -ratio, ratio, 1, -1, 1, 20);

        //float left = 0.0f;
        //float right = width;
        //float bottom = height;
        //float top = 0.0f;
        //float near = 0.0f;
        //float far = 20.0f;
        //Matrix.orthoM(projMatrix_, 0, left, right, bottom, top, near, far);
    }

    public int width()
    {
        return width_;
    }

    public int height()
    {
        return height_;
    }

    public void renderBegin(float r, float g, float b)
    {
        cube_.update();

        GLES20.glClearColor(r, g, b, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(shaderProgram_);
        checkGlError("glUseProgram");
    }

    public void renderEnd()
    {
    }

    public void drawCubieFace(int xrot, int yrot, int zrot, int face, int tx, int ty, int color)
    {
        Matrix.setIdentityM(modelMatrix_, 0);
        Matrix.translateM(modelMatrix_, 0, 0, CUBE_POS_Y, CUBE_POS_Z);

        // Overall cube rotation
        Matrix.rotateM(modelMatrix_, 0, spinX_, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(modelMatrix_, 0, spinY_, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(modelMatrix_, 0, spinZ_, 0.0f, 0.0f, 1.0f);

        Matrix.rotateM(modelMatrix_, 0, xrot, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(modelMatrix_, 0, yrot, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(modelMatrix_, 0, zrot, 0.0f, 0.0f, 1.0f);

        // Cubie face location (rotate, then translate)
        switch (face)
        {
            case RubikCube.FACE_U:
                Matrix.rotateM(modelMatrix_, 0, -90, 1.0f, 0.0f, 0.0f);
                break;
            case RubikCube.FACE_D:
                Matrix.rotateM(modelMatrix_, 0, 90, 1.0f, 0.0f, 0.0f);
                break;
            case RubikCube.FACE_L:
                Matrix.rotateM(modelMatrix_, 0, -90, 0.0f, 1.0f, 0.0f);
                break;
            case RubikCube.FACE_R:
                Matrix.rotateM(modelMatrix_, 0, 90, 0.0f, 1.0f, 0.0f);
                break;
            case RubikCube.FACE_B:
                Matrix.rotateM(modelMatrix_, 0, -180, 1.0f, 0.0f, 0.0f);
                break;
            case RubikCube.FACE_F:
                break;
        }

        Matrix.translateM(modelMatrix_, 0, tx, ty, 0);

        Matrix.multiplyMM(tempMatrix_, 0, viewMatrix_, 0, modelMatrix_, 0);
        Matrix.multiplyMM(viewProjMatrix_, 0, projMatrix_, 0, tempMatrix_, 0);

        GLES20.glUniformMatrix4fv(viewProjMatrixHandle_, 1, false, viewProjMatrix_, 0);

        GLES20.glUniform4f(vertColorHandle_, faceColors_[color][0], faceColors_[color][1], faceColors_[color][2], 1.0f);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, quadIndices_);
        checkGlError("glDrawArrays");
    }


    public void onDrawFrame(GL10 glUnused)
    {
        renderBegin(0.1f, 0.1f, 0.1f);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cubieID_);
        cubieVerts_.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(posHandle_, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, cubieVerts_);
        checkGlError("glVertexAttribPointer maPosition");
        cubieVerts_.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(posHandle_);
        checkGlError("glEnableVertexAttribArray posHandle");
        GLES20.glVertexAttribPointer(texHandle_, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, cubieVerts_);
        checkGlError("glVertexAttribPointer texHandle");
        GLES20.glEnableVertexAttribArray(texHandle_);
        checkGlError("glEnableVertexAttribArray texHandle");

        for (int rot = 0; rot < 9; ++rot)
        {
            rots_[rot] = 0;
        }
        if (cube_.angle_ != 0)
        {
            rots_[cube_.rot_] = cube_.angle_;
        }

        // Front
        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 0, 0, cube_.cubies_[RubikCube.FACE_F][2]);
        drawCubieFace( rots_[RubikCube.ROT_X],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 1, 0, cube_.cubies_[RubikCube.FACE_F][1]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 2, 0, cube_.cubies_[RubikCube.FACE_F][0]);

        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_Y],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 0, 1, cube_.cubies_[RubikCube.FACE_F][5]);
        drawCubieFace( rots_[RubikCube.ROT_X],  rots_[RubikCube.ROT_Y],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 1, 1, cube_.cubies_[RubikCube.FACE_F][4]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_Y],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 2, 1, cube_.cubies_[RubikCube.FACE_F][3]);

        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 0, 2, cube_.cubies_[RubikCube.FACE_F][8]);
        drawCubieFace( rots_[RubikCube.ROT_X], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 1, 2, cube_.cubies_[RubikCube.FACE_F][7]);
        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_F, 2, 2, cube_.cubies_[RubikCube.FACE_F][6]);

        // Back
        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 0, 0, cube_.cubies_[RubikCube.FACE_B][6]);
        drawCubieFace( rots_[RubikCube.ROT_X], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 1, 0, cube_.cubies_[RubikCube.FACE_B][7]);
        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 2, 0, cube_.cubies_[RubikCube.FACE_B][8]);

        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_Y], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 0, 1, cube_.cubies_[RubikCube.FACE_B][3]);
        drawCubieFace( rots_[RubikCube.ROT_X],  rots_[RubikCube.ROT_Y], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 1, 1, cube_.cubies_[RubikCube.FACE_B][4]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_Y], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 2, 1, cube_.cubies_[RubikCube.FACE_B][5]);

        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 0, 2, cube_.cubies_[RubikCube.FACE_B][0]);
        drawCubieFace( rots_[RubikCube.ROT_X],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 1, 2, cube_.cubies_[RubikCube.FACE_B][1]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_B, 2, 2, cube_.cubies_[RubikCube.FACE_B][2]);

        // Up
        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_U, 0, 0, cube_.cubies_[RubikCube.FACE_U][2]);
        drawCubieFace( rots_[RubikCube.ROT_X],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_U, 1, 0, cube_.cubies_[RubikCube.FACE_U][1]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_U, 2, 0, cube_.cubies_[RubikCube.FACE_U][0]);

        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_Z], RubikCube.FACE_U, 0, 1, cube_.cubies_[RubikCube.FACE_U][5]);
        drawCubieFace( rots_[RubikCube.ROT_X],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_Z], RubikCube.FACE_U, 1, 1, cube_.cubies_[RubikCube.FACE_U][4]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_Z], RubikCube.FACE_U, 2, 1, cube_.cubies_[RubikCube.FACE_U][3]);

        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_U, 0, 2, cube_.cubies_[RubikCube.FACE_U][8]);
        drawCubieFace( rots_[RubikCube.ROT_X],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_U, 1, 2, cube_.cubies_[RubikCube.FACE_U][7]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_U, 2, 2, cube_.cubies_[RubikCube.FACE_U][6]);

        // Down
        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_D, 0, 0, cube_.cubies_[RubikCube.FACE_D][2]);
        drawCubieFace( rots_[RubikCube.ROT_X], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_D, 1, 0, cube_.cubies_[RubikCube.FACE_D][1]);
        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_D, 2, 0, cube_.cubies_[RubikCube.FACE_D][0]);

        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_Z], RubikCube.FACE_D, 0, 1, cube_.cubies_[RubikCube.FACE_D][5]);
        drawCubieFace( rots_[RubikCube.ROT_X], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_Z], RubikCube.FACE_D, 1, 1, cube_.cubies_[RubikCube.FACE_D][4]);
        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_Z], RubikCube.FACE_D, 2, 1, cube_.cubies_[RubikCube.FACE_D][3]);

        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_D, 0, 2, cube_.cubies_[RubikCube.FACE_D][8]);
        drawCubieFace( rots_[RubikCube.ROT_X], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_D, 1, 2, cube_.cubies_[RubikCube.FACE_D][7]);
        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_D, 2, 2, cube_.cubies_[RubikCube.FACE_D][6]);

        // Left
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_L, 0, 0, cube_.cubies_[RubikCube.FACE_L][2]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_Z], RubikCube.FACE_L, 1, 0, cube_.cubies_[RubikCube.FACE_L][1]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_L, 2, 0, cube_.cubies_[RubikCube.FACE_L][0]);

        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_Y],  rots_[RubikCube.ROT_F], RubikCube.FACE_L, 0, 1, cube_.cubies_[RubikCube.FACE_L][5]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_Y],  rots_[RubikCube.ROT_Z], RubikCube.FACE_L, 1, 1, cube_.cubies_[RubikCube.FACE_L][4]);
        drawCubieFace(-rots_[RubikCube.ROT_L],  rots_[RubikCube.ROT_Y], -rots_[RubikCube.ROT_B], RubikCube.FACE_L, 2, 1, cube_.cubies_[RubikCube.FACE_L][3]);

        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_L, 0, 2, cube_.cubies_[RubikCube.FACE_L][8]);
        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_Z], RubikCube.FACE_L, 1, 2, cube_.cubies_[RubikCube.FACE_L][7]);
        drawCubieFace(-rots_[RubikCube.ROT_L], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_L, 2, 2, cube_.cubies_[RubikCube.FACE_L][6]);

        // Right
        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U], -rots_[RubikCube.ROT_B], RubikCube.FACE_R, 0, 0, cube_.cubies_[RubikCube.FACE_R][2]);
        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_Z], RubikCube.FACE_R, 1, 0, cube_.cubies_[RubikCube.FACE_R][1]);
        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_U],  rots_[RubikCube.ROT_F], RubikCube.FACE_R, 2, 0, cube_.cubies_[RubikCube.FACE_R][0]);

        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_Y], -rots_[RubikCube.ROT_B], RubikCube.FACE_R, 0, 1, cube_.cubies_[RubikCube.FACE_R][5]);
        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_Y],  rots_[RubikCube.ROT_Z], RubikCube.FACE_R, 1, 1, cube_.cubies_[RubikCube.FACE_R][4]);
        drawCubieFace( rots_[RubikCube.ROT_R],  rots_[RubikCube.ROT_Y],  rots_[RubikCube.ROT_F], RubikCube.FACE_R, 2, 1, cube_.cubies_[RubikCube.FACE_R][3]);

        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D], -rots_[RubikCube.ROT_B], RubikCube.FACE_R, 0, 2, cube_.cubies_[RubikCube.FACE_R][8]);
        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_Z], RubikCube.FACE_R, 1, 2, cube_.cubies_[RubikCube.FACE_R][7]);
        drawCubieFace( rots_[RubikCube.ROT_R], -rots_[RubikCube.ROT_D],  rots_[RubikCube.ROT_F], RubikCube.FACE_R, 2, 2, cube_.cubies_[RubikCube.FACE_R][6]);

        renderEnd();
    }

    private void checkGlError(String op)
    {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
        {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
