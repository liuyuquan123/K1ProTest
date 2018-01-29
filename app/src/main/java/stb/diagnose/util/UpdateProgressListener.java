package stb.diagnose.util;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface UpdateProgressListener {

    public void updateBar(int max, int size);
    public void updateHint(String string);
    public void setback(boolean flag);

}
