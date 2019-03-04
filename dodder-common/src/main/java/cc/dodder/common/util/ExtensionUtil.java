package cc.dodder.common.util;

import java.util.HashMap;
import java.util.Map;

/***
 * 文件扩展名工具类
 *
 * @author Mr.Xu
 * @date 2019-02-22 15:11
 **/
public class ExtensionUtil {

	private static final Map<String, String> EXT;

	static {
		EXT = new HashMap<>();
		EXT.put(".aif", "音频");
		EXT.put(".aifc", "音频");
		EXT.put(".aiff", "音频");
		EXT.put(".mid", "音频");
		EXT.put(".mp3", "音频");
		EXT.put(".wav", "音频");
		EXT.put(".wma", "音频");
		EXT.put(".amr", "音频");
		EXT.put(".aac", "音频");
		EXT.put(".flac", "音频");


		EXT.put(".asf", "视频");
		EXT.put(".mpg", "视频");
		EXT.put(".rm", "视频");
		EXT.put(".avi", "视频");
		EXT.put(".rmvb", "视频");
		EXT.put(".mp4", "视频");
		EXT.put(".wmv", "视频");
		EXT.put(".mkv", "视频");
		EXT.put(".m2ts", "视频");
		EXT.put(".flv", "视频");
		EXT.put(".qmv", "视频");
		EXT.put(".mov", "视频");
		EXT.put(".vob", "视频");
		EXT.put(".3gp", "视频");
		EXT.put(".mpg", "视频");
		EXT.put(".mpeg", "视频");
		EXT.put(".m4v", "视频");
		EXT.put(".f4v", "视频");

		EXT.put(".jpg", "图片");
		EXT.put(".bmp", "图片");
		EXT.put(".jpeg", "图片");
		EXT.put(".png", "图片");
		EXT.put(".gif", "图片");
		EXT.put(".tiff", "图片");

		EXT.put(".pdf", "文档");
		EXT.put(".isz", "文档");
		EXT.put(".chm", "文档");
		EXT.put(".txt", "文档");
		EXT.put(".epub", "文档");
		EXT.put(".bc!", "文档");
		EXT.put(".doc", "文档");
		EXT.put(".ppt", "文档");
		EXT.put(".xls", "文档");

		EXT.put(".rar", "压缩文件");
		EXT.put(".zip", "压缩文件");
		EXT.put(".7z", "压缩文件");
		EXT.put(".gz", "压缩文件");
		EXT.put(".war", "压缩文件");
		EXT.put(".z", "压缩文件");

		EXT.put(".iso", "镜像文件");

		EXT.put(".exe", "软件");
		EXT.put(".app", "软件");
		EXT.put(".msi", "软件");
		EXT.put(".apk", "软件");

	}

	public static String getExtensionType(String name) {

		String ext = getExt(name);
		if (ext == null)
			return null;

		if (ext.endsWith("\\") || ext.endsWith("/"))
			ext = ext.substring(0, ext.length() - 1);

		String type = EXT.get(ext);

		return type;
	}

	private static String getExt(String name) {
		int pos = name.lastIndexOf(".");
		if (pos == -1)
			return null;
		String ext = name.substring(pos);
		return ext;
	}
}
