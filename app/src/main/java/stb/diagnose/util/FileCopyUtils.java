package stb.diagnose.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FileCopyUtils {
	
	private static String TAG = "FileCopyUtils";
	
	public FileCopyUtils() {
	}
	
	public static void copyAssets(String src, String destPath, Context context) {
		
		Log.i(TAG, "copyAssets start!");
		try {
			File dest = new File(destPath);
			InputStream in = context.getResources().getAssets().open(src);
			
			if(dest.exists() && dest.length() == in.available()) {
				Log.i(TAG, dest + " exists and exits");
				return;
			}
			
			if (!dest.exists())
				dest.createNewFile();
			FileOutputStream fos = new FileOutputStream(dest);
			
			byte[] buffer = new byte[2048];
			int len=-1;
			while ((len = in.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				fos.flush();
			}

			in.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "copyAssets complete!");

	}

}
