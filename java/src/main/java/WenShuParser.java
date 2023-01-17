import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class WenShuParser {
    private final Map<String, String[]> cityMap = new HashMap<>();
    private final Map<String, String[]> provMap = new HashMap<>();
    private final List<String> cityList = new ArrayList<>();

    public WenShuParser() {
        try {
            String path = "D:\\work\\wenshu\\citcode.csv";
            String charset = "gbk";
            FileInputStream fileInputStream = new FileInputStream(path);
            Reader reader = new InputStreamReader(fileInputStream, charset);
            CSVReader csvReader = new CSVReader(reader);
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                cityList.add(line[3]);
                provMap.put(line[0], line);
                cityMap.put(line[3], line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] parseLine(List<String> line) {
        String caseName = line.get(1);
        String caseNo = line.get(4);
        String courtName = line.get(2);
        String refereeDate = line.get(7);
        String caseType = line.get(20);
        String htmlContent = line.get(19);
        String judgmentResult = line.get(30);
        List<String> location = JSONArray.parseArray(line.get(32), String.class);
        String htmlPath = "D:\\work\\wenshu\\result\\detail\\" + caseNo + ".html";
        String provName = location.size() >= 1 ? getProvChn(location.get(0)) : "TBD";
        String cityName = location.size() >= 2 ? location.get(1) : "TBD";
        List<String> plaintiffList = JSONArray.parseArray(line.get(15), String.class);
        String defendant = parseDefendant(plaintiffList);
        String defendantType = parsePlaintiffType(defendant);
        String plaintiff = parsePlaintiff(plaintiffList);
        String plaintiffType = parsePlaintiffType(plaintiff);
        String judge = parseJudge(line.get(29));
        Date date = DateUtil.parse(refereeDate, "yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTime(date);
        String refereeYear = String.valueOf(calendar.get(Calendar.YEAR));
        String refereeMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String refereeDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(htmlPath));
            writer.write(htmlContent);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{caseNo, caseName, plaintiff, plaintiffType, defendant, defendantType,
                courtName, cityName, getCityEng(cityName), getCityCode(cityName), provName, getProvEng(provName),
                getProvCode(provName), refereeYear, refereeMonth, refereeDay, caseType,
                parseWin(plaintiff, plaintiffType, defendant, defendantType, judgmentResult), judgmentResult, judge};
    }

    private String parseJudge(String tail) {
        String[] judgeArray = tail.split("\n");
        List<String> resultList = new ArrayList<>();
        for (String s : judgeArray) {
            if (s.contains("代理")) {
                s = s.replace("代理", "");
            }
            if (s.contains("审判员")) {
                resultList.add(s.replace("审判员", ""));
            }
            if (s.contains("审判长")) {
                resultList.add(s.replace("审判长", ""));
            }
        }
        return String.join(",", resultList);
    }

    private String getProvChn(String provName) {
        if (provName.endsWith("省")) {
            return provName.substring(0, provName.length() - 1);
        }
        return provName;
    }

    private String getProvEng(String provName) {
        String[] provLine = provMap.get(provName);
        if (provLine != null) {
            return provLine[1];
        }
        return "TBD";
    }

    private String getProvCode(String provName) {
        String[] provLine = provMap.get(provName);
        if (provLine != null) {
            return provLine[2];
        }
        return "TBD";
    }

    private String getCityEng(String city) {
        String[] cityLine = cityMap.get(city);
        if (cityLine != null) {
            return cityLine[4];
        }
        return "TBD";
    }

    private String getCityCode(String city) {
        String[] cityLine = cityMap.get(city);
        if (cityLine != null) {
            return cityLine[5];
        }
        return "TBD";
    }

    private String parseWin(String plaintiff, String plaintiffType, String defendant, String defendantType, String winString) {
        if (plaintiff.length() == 0 || defendant.length() == 0 || winString.length() == 0) {
            return "TBD: defendant or plaintiff is not clear";
        }
        int plaintiffScore = 0;
        int defendantScore = 0;
        String[] judgementResults = winString.split("\n");
        for (String judgementResult : judgementResults) {
            if (judgementResult.contains("驳回上诉") ||
                    judgementResult.contains("驳回申请人") ||
                    judgementResult.contains("驳回原告") ||
                    judgementResult.contains("驳回" + plaintiff) ||
                    judgementResult.contains("驳" + plaintiff) ||
                    judgementResult.contains("向" + defendant + "支付") ||
                    judgementResult.contains("支付给被告") ||
                    judgementResult.contains("支付给" + defendant) ||
                    judgementResult.contains("支付" + defendant) ||
                    judgementResult.contains("支付被告")) {
                defendantScore++;
            }
            if (judgementResult.contains("由原告负担") ||
                    judgementResult.contains("由原告承担") ||
                    judgementResult.contains("由" + plaintiff + "负担") ||
                    judgementResult.contains("由" + plaintiff + "承担") ||
                    judgementResult.contains("由原告" + plaintiff + "负担") ||
                    judgementResult.contains("由原告" + plaintiff + "承担")) {
                defendantScore = defendantScore + 1000;
            }
            if (judgementResult.contains("驳回被告") ||
                    judgementResult.contains("驳回" + defendant) ||
                    judgementResult.contains("驳" + defendant) ||
                    judgementResult.contains("向" + plaintiff + "支付") ||
                    judgementResult.contains("支付给原告") ||
                    judgementResult.contains("支付给" + plaintiff) ||
                    judgementResult.contains("支付" + plaintiff) ||
                    judgementResult.contains("支付原告")) {
                plaintiffScore++;
            }
            if (judgementResult.contains("由被告负担") ||
                    judgementResult.contains("由被告承担") ||
                    judgementResult.contains("由" + defendant + "负担") ||
                    judgementResult.contains("由" + defendant + "承担") ||
                    judgementResult.contains("由被告" + defendant + "负担") ||
                    judgementResult.contains("由被告" + defendant + "承担")) {
                plaintiffScore = plaintiffScore + 1000;
            }
        }
        if (plaintiffScore > defendantScore) {
            return plaintiffType;
        } else if (plaintiffScore < defendantScore) {
            return defendantType;
        } else {
            return "TBD";
        }
    }

    private String parsePlaintiff(List<String> plaintiffList) {
        if (plaintiffList == null || plaintiffList.size() == 0) {
            return "";
        }
        StringBuilder plaintiff = new StringBuilder(plaintiffList.get(0));
        String plaintiffType = parsePlaintiffType(plaintiff.toString());
        if (plaintiffList.size() > 1) {
            for (int i = 1; i < plaintiffList.size(); i++) {
                String type = parsePlaintiffType(plaintiffList.get(i));
                if (plaintiffType.equals(type)) {
                    plaintiff.append("、").append(plaintiffList.get(i));
                }
            }
        }
        return plaintiff.toString();
    }

    private String parseDefendant(List<String> defendantList) {
        if (defendantList == null || defendantList.size() == 0) {
            return "";
        }
        String plaintiffType = parsePlaintiffType(defendantList.get(0));
        ArrayList<String> defendants = new ArrayList<>();
        if (defendantList.size() > 1) {
            for (int i = 1; i < defendantList.size(); i++) {
                String type = parsePlaintiffType(defendantList.get(i));
                if (!plaintiffType.equals(type)) {
                    defendants.add(defendantList.get(i));
                }
            }
        }
        return String.join("、", defendants);
    }

    private String parsePlaintiffType(String plaintiff) {
        for (String city : cityList) {
            if (plaintiff.contains(city)) {
                return "firm";
            }
        }
        if (plaintiff.contains("公司") || plaintiff.contains("×")) {
            return "firm";
        } else if (plaintiff.length() > 6) {
            return "firm";
        } else {
            return "worker";
        }

    }
}
