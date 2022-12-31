package utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

/**
 * @author: W.Z
 * @date: 2022/8/23 17:00
 * @desc:
 */
public class ParamsUtils {

    private static String[] attr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};


    public static String getPageId() {
        String pageId = "";
        for (int i = 1; i <= 32; i++) {
            String n = Long.toHexString(Math.round(Math.floor(Math.random() * 16)));
            pageId += n;
        }
        return pageId;
    }

    public static String cipher() {
        DateTime date = DateUtil.date();
        long timestamp = date.getTime();
        String salt = random(24);
        int year = date.year();
        String month = date.getField(DateField.MONTH) < 10 ? "0" + date.getField(DateField.MONTH) : date.getField(DateField.MONTH) + "";
        String day = date.getField(DateField.DAY_OF_MONTH) < 10 ? "0" + date.getField(DateField.DAY_OF_MONTH) : date.getField(DateField.DAY_OF_MONTH) + "";
        String iv = year + month + day;
        String enc = TripleDES.encrypt(salt, timestamp + "", iv);
        String str = salt + iv + enc;
        return strTobinary(str);

    }

    public static String random(int size) {
        StringBuilder str = new StringBuilder();
        String[] arr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        for (int i = 0; i < size; i++) {
            //  long index = Math.round(Math.random() * (attr.length - 1));
            str.append(arr[(int) Math.round(Math.random() * (arr.length - 1))]);
        }
        return str.toString();
    }

    private static String strTobinary(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = str.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                sb.append(" ");
            }
            sb.append(Integer.toBinaryString(bytes[i]));
        }
        return sb.toString();
    }

    public static String getCaseNo(String caseNo) {
        String[] split = new String[2];
        if (caseNo.contains("〔")) {
            split = caseNo.split("〔");
        }
        if (caseNo.contains("（")) {
            split = caseNo.split("（");
        }
        if (caseNo.contains("【")) {
            split = caseNo.split("【");
        }
        if (caseNo.contains("[")) {
            split = caseNo.split("\\[");
        }
        return split[0];
    }
}
