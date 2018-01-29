package stb.diagnose.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/2/10.
 */

public class ProgressBufferInputStream extends BufferedInputStream {


    public interface UpdateProgressListener{
        void onProgress(int len);
    }


    private UpdateProgressListener listener;
    private int progress;
    private long lastUpdate;




    public ProgressBufferInputStream(InputStream in) {
        super(in);
// TODO Auto-generated constructor stub
    }


    public ProgressBufferInputStream(InputStream in, UpdateProgressListener listener) {
        super(in);
        progress = 0;
        lastUpdate = 0;
        this.listener = listener;


// TODO Auto-generated constructor stub
    }


    @Override
    public synchronized int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
// TODO Auto-generated method stub
        int count = super.read(buffer, byteOffset, byteCount);
        if(listener!=null){
            progress += count;
            this.listener.onProgress(progress);
        }
        return count;
    }


}

