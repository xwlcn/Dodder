package cc.dodder.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mr.Xu on 2017/4/21.
 */
public class FileTypeUtil {

    private static Map<String, String> map = new HashMap<>();

    static {
        map.put("jpg", "image");
        map.put("jpeg", "image");
        map.put("gif", "image");
        map.put("png", "image");
        map.put("bmp", "image");

        map.put("mp4", "video");
        map.put("mkv", "video");
        map.put("ts", "video");
        map.put("rmvb", "video");
        map.put("avi", "video");
        map.put("rm", "video");
        map.put("asf", "video");
        map.put("divx", "video");
        map.put("mpeg", "video");
        map.put("mpe", "video");
        map.put("wmv", "video");
        map.put("vob", "video");
        map.put("flv", "video");
        map.put("3gp", "video");

        map.put("srt", "subtitle");
        map.put("ass", "subtitle");
        map.put("sub", "subtitle");
        map.put("ssa", "subtitle");

        map.put("nfo", "info");

        map.put("xlsx", "excel");
        map.put("xls", "excel");
        map.put("doc", "word");
        map.put("docx", "word");
    }

    public static String getFileType(String fileName) {
        if (fileName == null || !fileName.contains("."))
            return "file";
        String type = "file";
        try {
            if (fileName.contains("<small>")) {
                fileName = fileName.substring(0, fileName.indexOf("<small>"));
            }
            String sufix = fileName.substring(fileName.lastIndexOf(".") + 1);
            type = map.get(sufix);
            if (type == null)
                type = "file";
        } catch (Exception e) {
            return "file";
        }
        return type;
    }
}
