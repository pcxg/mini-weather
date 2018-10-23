package com.example.administrator.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/*
* 将汉字转换为拼音的工具类
*
* */
public class PinYinUtil {
    HanyuPinyinOutputFormat format = null;
    public static enum PinYinType{
        UPPERCASE,
        LOWERCASE,
        FIRSTUPPER
    }
    //默认为大写无声调
    public PinYinUtil(){
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public String toPinYin(String string) throws BadHanyuPinyinOutputFormatCombination {
        return toPinYin(string,"",PinYinType.UPPERCASE);
    }

    public String toPinYin(String string,String spera) throws BadHanyuPinyinOutputFormatCombination {
        return toPinYin(string,spera,PinYinType.UPPERCASE);
    }

    public String toPinYin(String string,String spera,PinYinType type) throws BadHanyuPinyinOutputFormatCombination {
        if(string == null || string.trim().length() == 0)
            return "";
        if(type == PinYinType.UPPERCASE)
            format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        else
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);

        String pinyin = "";
        String temp = "";
        String[] t;
        for(int i = 0;i<string.length();i++){
            char c = string.charAt(i);
            if((int)c <= 128){
                pinyin += c;
            }
            else{
                t = PinyinHelper.toHanyuPinyinStringArray(c,format);
                if(t == null)
                    pinyin += c;
                else{
                    temp = t[0];
                    if(type == PinYinType.FIRSTUPPER)
                        temp = t[0].toUpperCase().charAt(0)+temp.substring(1);
                    pinyin += temp+(i == string.length()-1?"":spera);
                }
            }
        }
        return pinyin.trim();
    }

}
