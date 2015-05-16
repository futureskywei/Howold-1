package com.example.gxc.howold;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int PICK_CODE = 0X110;
    private ImageView mPhoto;
    private Button mGetImage;
    private Button mDetect;
    private TextView mTip;
    private View mWating;

    private Bitmap mPhotoImage;
    private String mCurrentPhotoStr;
    private  Paint mPaint;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        InitEvents();
        mPaint = new Paint();

    }

    private void InitEvents() {
        mGetImage.setOnClickListener(this);
        mDetect.setOnClickListener(this);

    }


    private void initViews() {
        mPhoto = (ImageView) findViewById(R.id.id_photo);
        mGetImage = (Button) findViewById(R.id.id_getImage);
        mDetect = (Button) findViewById(R.id.id_detect);
        mTip = (TextView) findViewById(R.id.id_tip);
    }

    private static final int MSG_SUCCESS = 0x111;
    private static final int MSG_ERROR = 0x112;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:
                    mWating.setVisibility(View.GONE);
                    JSONObject rs = (JSONObject) msg.obj;
                    prepareRsBitmap(rs);
                    mPhoto.setImageBitmap(mPhotoImage);

                    break;
                case MSG_ERROR:
                    mWating.setVisibility(View.GONE);
                    String errorMsg = (String) msg.obj;
                    if(TextUtils.isEmpty(errorMsg))
                    {
                        mTip.setText("Error");
                    }else{
                        mTip.setText(errorMsg);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void prepareRsBitmap(JSONObject rs) {

        Bitmap bitmap = Bitmap.createBitmap(mPhotoImage.getWidth(), mPhotoImage.getHeight(), mPhotoImage.getConfig());
        Canvas canvas = new Canvas(bitmap);


        try {
            JSONArray  faces =  rs.getJSONArray("face");
            int faceCount = faces.length();
            mTip.setText("find" + faceCount);
            for(int i=0; i<faceCount; i++){
                //拿到单独的face对象
                JSONObject face = faces.getJSONObject(i);
                JSONObject posObj = face.getJSONObject("");

                float  x = (float) posObj.getJSONObject("center").getDouble("x");
                float  y = (float) posObj.getJSONObject("center").getDouble("y");

                float  w = (float) posObj.getDouble("width");
                float  h = (float) posObj.getDouble("height");

                x = x /100 * bitmap.getWidth();
                y = y /100 * bitmap.getHeight();

                w = w/ 100 * bitmap.getWidth();
                h = h/ 100 * bitmap.getHeight();

                mPaint.setColor(0xffffffff);
                mPaint.setStrokeWidth(3);

                //画box
                canvas.drawLine(x - w/2, y - h / 2, x - w/2, y + h /2, mPaint);
                canvas.drawLine(x - w/2, y - h / 2, x + w/2, y - h /2, mPaint);
                canvas.drawLine(x + w/2, y - h / 2, x + w/2, y + h /2, mPaint);
                canvas.drawLine(x - w/2, y + h / 2, x + w/2, y + h /2, mPaint);



            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.id_getImage:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_CODE);

                break;

            case R.id.id_detect:

                mWating.setVisibility(View.VISIBLE);
                FaceppDetect.detect(mPhotoImage, new FaceppDetect.Callback() {

                    @Override
                    public void success(JSONObject result) {
                        Message msg = Message.obtain();
                        msg.what = MSG_SUCCESS;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg = Message.obtain();
                        msg.what = MSG_ERROR;
                        msg.obj = exception.getErrorMessage();
                        mHandler.sendMessage(msg);
                    }
                });

                break;


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PICK_CODE) {
            if (intent != null) {
                Uri uri = intent.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();

                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                cursor.close();

                resizePhoto();

                mPhoto.setImageBitmap(mPhotoImage);
                mTip.setText("Click Detect ==>");
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void resizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mCurrentPhotoStr, options);

        double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);

        options.inSampleSize = (int) Math.ceil(ratio);

        options.inJustDecodeBounds = false;

        mPhotoImage = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
    }
}
