package com.example.gxc.howold;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int PICK_CODE =  0X110;
    private ImageView mPhoto;
    private Button  mGetImage;
    private Button mDetect;
    private TextView mTip;
    private View mWating;

    private  Bitmap mPhotoImage;
    private String mCurrentPhotoStr;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        InitEvents();

    }

    private void InitEvents() {
        mGetImage.setOnClickListener(this);
        mDetect.setOnClickListener(this);
        ;
    }



    private void initViews() {
        mPhoto = (ImageView) findViewById(R.id.id_photo);
        mGetImage = (Button) findViewById(R.id.id_getImage);
        mDetect = (Button) findViewById(R.id.id_detect);
        mTip = (TextView) findViewById(R.id.id_tip);
    }

    @Override
    public void onClick(View v) {

        switch ( v.getId())
        {
            case R.id.id_getImage:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_CODE);

                break;

            case R.id.id_detect:

                break;



        }
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if(requestCode == PICK_CODE)
        {
            if(intent != null)
            {
                 Uri uri  =   intent.getData();
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

        double ratio = Math.max(options.outWidth *1.0d/ 1024f,  options.outHeight*1.0d/ 1024f);

        options.inSampleSize =  (int)Math.ceil(ratio);

        options.inJustDecodeBounds = false;

        mPhotoImage =   BitmapFactory.decodeFile(mCurrentPhotoStr, options);
    }
 }
