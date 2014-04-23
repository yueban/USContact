package cn.ncuhome.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;

public class IOHelper {

	private static String SDPATH = Environment.getExternalStorageDirectory() + File.separator;

	public static String getSDPATH() {
		return SDPATH;
	}

	/*
	 * 判断SD卡上目录是否存在
	 */
	public static boolean isDirExist(String dirName) {
		// System.out.println("creatSDDir--->" + SDPATH + dirName);
		File dir = new File(SDPATH + dirName);
		return dir.exists();
	}

	/*
	 * 在SD卡上创建目录
	 */
	public static File creatSDDir(String dirName) {
		// System.out.println("creatSDDir--->" + SDPATH + dirName);
		File dir = new File(SDPATH + dirName);
		dir.mkdirs();
		return dir;
	}

	/*
	 * 在SD卡上创建文件
	 */
	public static File creatSDFile(String dirname, String filename) throws IOException {
		File file = new File(SDPATH + dirname, filename);
		file.createNewFile();
		return file;
	}

	/*
	 * 判断SD卡上的文件是否存在
	 */
	public boolean isFileExist(String fileName) {
		// System.out.println("isFileExist--->" + SDPATH + fileName);
		File file = new File(SDPATH + fileName);
		return file.exists();
	}

	/*
	 * 将一个InputStream写入文件中
	 */
	public static void writeIStoSDCard(InputStream is, String dirname, String filename) {
		if (!isDirExist(dirname)) {
			creatSDDir(dirname);
		}
		try {
			File file = creatSDFile(dirname, filename);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			byte[] b = new byte[1024];
			int ch = -1;
			while (true) {
				ch = is.read(b);
				if (ch <= 0) {
					break;
				}
				fileOutputStream.write(b, 0, ch);
			}
			is.close();
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
