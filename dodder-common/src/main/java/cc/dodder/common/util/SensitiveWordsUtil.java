package cc.dodder.common.util;

import toolgood.words.WordsSearch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/***
 * 敏感词检测工具
 */
public class SensitiveWordsUtil {

    private static volatile SensitiveWordsUtil instance;

    private WordsSearch ws = new WordsSearch();

    public static SensitiveWordsUtil getInstance() {
        if (instance  == null) {
            synchronized (SensitiveWordsUtil.class) {
                if (instance == null) {
                    instance = new SensitiveWordsUtil();
                    List<String> list = instance.loadKeywords("sensi_words.txt");
                    instance.ws.SetKeywords(list);
                }
            }
        }
        return instance;
    }

    /**
     * 检测是否包含敏感词
     * @param text
     * @return
     */
    public boolean containsAny(String text) {
        return instance.ws.ContainsAny(text);
    }

    /**
     * 替换敏感词为 *
     * @param text
     * @return
     */
    public String replace(String text) {
        return instance.ws.Replace(text);
    }

    private List<String> loadKeywords(String resourceName){
        List<String> keyArray=new ArrayList<String>();
        try{
            InputStream u1 = WordsSearch.class.getClassLoader().getResourceAsStream(resourceName);
            BufferedReader br = new BufferedReader(new InputStreamReader(u1));
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                keyArray.add(s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return keyArray;
    }

    public static void main(String[] args) {
        System.out.println(SensitiveWordsUtil.getInstance().replace("操逼 强奸多斯拉克  class所担负的顺丰速递所担AV电影负的顺丰速递"));
    }

}