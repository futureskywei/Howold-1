package com.example.gxc.howold;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by gxc on 2015/5/16.
 */
public class FaceppDetect {

    public interface Callback {
        void success(JSONObject result);

        void error(FaceppParseException exception);
    }

    public static void detect(final Bitmap bm, final Callback callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //request
                    HttpRequests requests = new HttpRequests(Constant.KEY, Constant.SECRET, true, true);
                    Bitmap bmSmall = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    byte[] arrays = stream.toByteArray();

                    PostParameters params = new PostParameters();
                    params.setImg(arrays);
                    JSONObject jsonObject =  requests.detectionDetect(params);

                    Log.e("TAG", jsonObject.toString());

                    if(callBack!=null)
                    {
                        callBack.success(jsonObject);
                    }


                } catch (FaceppParseException e) {
                    e.printStackTrace();

                    if(callBack != null){
                        callBack.error(e);
                    }
                }
            }

        }).start();

    }
}
