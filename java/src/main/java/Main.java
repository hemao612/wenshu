import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.squareup.okhttp.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.ParamsUtils;
import utils.TripleDES;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Main {

    private final String cookie = "wzws_sessionid=gWE4MThhYoAxODMuMTQuMjguMTQygjZmNjkwMaBjs8Bv; SESSION=7d4c22bc-788f-4347-bbf6-ad3acb8a4379; wzws_cid=e0fec3a985f4c49339fbcf0a3271cff1940bb6a596142fd4aa197819c27500aca953e540b0d4f2ceac3cc2f490405c5dfdf81bb9bce6c911bb07700af0943996e915e8818910057b63762d3a51bfc1cb";
    private int count = 0;
    private CSVWriter csvWriter = null;
    private List<String> cityList = new ArrayList<>();
    private Map<String, String[]> cityMap = new HashMap<>();
    private String year = "2011";


    public static void main(String[] args) {
//        new DocumentService().page(1, 5);
        Main main = new Main();
        main.readCityCode();
        main.parseFile();
//        main.fetchList();
//        try {
//            new Main().detailTest();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void fetchList() {
        try {
            Path path = Paths.get("D:\\work\\wenshu\\result", "2010-1.csv");
            csvWriter = new CSVWriter(new FileWriter(path.toFile()));
            String[] line = {"id", "title", "plaintiff", "plaintiff_type", "defendant", "defendant_type", "court",
                    "city_chn", "city_eng", "city_code", "prov_chn", "prov_eng", "prov_code", "year", "month", "day", "type",
                    "win", "result"};
            csvWriter.writeNext(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        int pageNum = 1;
        while (true) {
            String pageId = ParamsUtils.getPageId();
            String cipherText = ParamsUtils.cipher();
            String requestToken = ParamsUtils.random(24);
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
            RequestBody body = RequestBody.create(mediaType, "pageId=" + pageId +
                    "&s21=%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE&sortFields=s51%3Aasc" +
                    "&ciphertext=" + cipherText +
                    "&pageNum=" + pageNum + "&queryCondition=%5B%7B%22key%22%3A%22s21%22%2C%22value%22%3A%22%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE%22%7D%2C%7B%22key%22%3A%22cprq%22%2C%22value%22%3A%222010-01-01+TO+2010-02-01%22%7D%5D" +
                    "&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40queryDoc" +
                    "&__RequestVerificationToken=" + requestToken + "&wh=964&ww=937&cs=0");
            Request request = new Request.Builder()
                    .url("https://wenshu.court.gov.cn/website/parse/rest.q4w")
                    .method("POST", body)
                    .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                    .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("Cookie", cookie)
                    .addHeader("Origin", "https://wenshu.court.gov.cn")
                    .addHeader("Referer", "https://wenshu.court.gov.cn/website/wenshu/181217BMTKHNT2W0/index.html?pageId=808314baeded68bfda55ff7067aaf9f7&s21=%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("sec-ch-ua", "\"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"108\", \"Google Chrome\";v=\"108\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                if (responseBody.contains("307 Temporary Redirect")) {
                    System.out.println("Temporary Redirect, please wait 5 mintues");
                    TimeUnit.MINUTES.sleep(RandomUtil.randomInt(5, 10));
                    continue;
                }
                Result result = JSON.parseObject(responseBody, Result.class);
                if (result.getSuccess()) {
                    String iv = DateUtil.format(new Date(), "yyyyMMdd");
                    String decrypt = TripleDES.decrypt(result.getSecretKey(), result.getResult(), iv);
                    JSONObject object = JSON.parseObject(decrypt);
                    JSONArray jsonArray = object.getJSONObject("queryResult").getJSONArray("resultList");
                    int total = object.getJSONObject("queryResult").getInteger("resultCount");
                    if (jsonArray.size() == 0) {
                        TimeUnit.SECONDS.sleep(RandomUtil.randomInt(5, 10));
                        continue;
                    }
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String caseNo = obj.getString("7");
                        String docId = obj.getString("rowkey");
                        System.out.println(count + ":" + caseNo);
                        fetchDetail(docId);
                    }
                    if (count >= 100) {
                        break;
                    }
                    pageNum++;
                } else {
                    System.out.println(result.getDescription());
                    break;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchDetail(String docId) throws IOException, InterruptedException {
        count++;
        String encodeDocId = URLEncoder.encode(docId, "UTF-8");
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String cipherText = ParamsUtils.cipher();
        String requestToken = ParamsUtils.random(24);
        RequestBody body = RequestBody.create(mediaType, "docId=" + encodeDocId + "&ciphertext=" + cipherText +
                "&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40docInfoSearch&__RequestVerificationToken=" + requestToken + "&wh=969&ww=937&cs=0");
        Request request = new Request.Builder()
                .url("https://wenshu.court.gov.cn/website/parse/rest.q4w")
                .method("POST", body)
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Cookie", cookie)
                .addHeader("Origin", "https://wenshu.court.gov.cn")
                .addHeader("Referer", "https://wenshu.court.gov.cn/website/wenshu/181107ANFZ0BXSK4/index.html?docId=" + docId)
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("sec-ch-ua", "\"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"108\", \"Google Chrome\";v=\"108\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .build();
        Response response = client.newCall(request).execute();
        String bodyString = response.body().string();
        if (bodyString.contains("307 Temporary Redirect")) {
            System.out.println("Temporary Redirect, please wait 5 mintues");
            TimeUnit.MINUTES.sleep(RandomUtil.randomInt(5, 10));
            fetchDetail(docId);
            return;
        }
        Result result = JSON.parseObject(bodyString, Result.class);
        if (result.getSuccess()) {
            String iv = DateUtil.format(new Date(), "yyyyMMdd");
            if (result.getResult() == null) {
                System.out.println(result.getDescription());
                TimeUnit.SECONDS.sleep(RandomUtil.randomInt(5, 10));
                return;
            }
            String decrypt = TripleDES.decrypt(result.getSecretKey(), result.getResult(), iv);
            JSONObject jsonObject = JSON.parseObject(decrypt);
            String id = jsonObject.getString("s5");
            String name = jsonObject.getString("s1");
            String caseNo = jsonObject.getString("s7");
            String courtName = jsonObject.getString("s2");
            String cityName = parseCity(courtName);
            List<String> plaintiffList = jsonObject.getJSONArray("s17").toJavaList(String.class);
            String defendant = parseDefendant(plaintiffList);
            String plaintiff = parsePlaintiff(plaintiffList);
            String refereeDate = jsonObject.getString("s31");
            Date date = DateUtil.parse(refereeDate, "yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(date);
            String refereeYear = String.valueOf(calendar.get(Calendar.YEAR));
            String refereeMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
            String refereeDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String caseType = jsonObject.getString("s8");
            String trialProceedings = jsonObject.getString("s9");
            String docType = jsonObject.getString("s6");
            JSONArray causes = jsonObject.getJSONArray("s11");
            String cause = null;
//                if (causes != null) {
//                    cause = causes.stream().map(Object::toString).collect(joining(","));
//                }
            JSONArray partys = jsonObject.getJSONArray("s17");
            String party = null;
//                if (partys != null) {
//                    party = partys.stream().map(Object::toString).collect(joining(","));
//                }
            JSONArray keywords = jsonObject.getJSONArray("s45");
            String keyword = null;
//                if (keywords != null) {
//                    keyword = keywords.stream().map(Object::toString).collect(joining(","));
//                }
            String courtConsidered = jsonObject.getString("s26");
            String judgmentResult = jsonObject.getString("s27");
            String htmlContent = jsonObject.getString("qwContent");

            jsonObject.remove("qwContent");
            String jsonContent = jsonObject.toJSONString();
            String[] line = {caseNo, name, plaintiff, parsePlaintiffType(plaintiff), defendant, parsePlaintiffType(defendant),
                    courtName, cityName, getCityEng(cityName), getCityCode(cityName), getProvChn(cityName), getProvEng(cityName),
                    getProvCode(cityName), refereeYear, refereeMonth, refereeDay, caseType,
                    parseWin(judgmentResult), judgmentResult};
            csvWriter.writeNext(line);
            TimeUnit.SECONDS.sleep(RandomUtil.randomInt(5, 10));
        }
    }

    private void parseText() {
        File file = new File("doc/raw_text");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.replace("x schP``shu\":", "");
                tempString = tempString.replaceAll("</span>", "");
                tempString = tempString.replace("<span style=\\\"color:red\\\">", "");
                tempString = tempString.split("queryResult", 2)[1];
                tempString = tempString.replaceFirst("\":", "");
                JSONObject resultJson = JSONObject.parseObject(tempString);
                JSONArray jsonArray = resultJson.getJSONArray("resultList");
                for (JSONObject jsonObject : jsonArray.toJavaList(JSONObject.class)) {
                    String court = jsonObject.getString("2");
                    String title = jsonObject.getString("1");
                    String content = jsonObject.getString("26");
                    String date = jsonObject.getString("31");
                }
                System.out.println(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void csvTest() {
        Path path = Paths.get("D:\\work\\wenshu\\result", "test.csv");
        try {
            String[] line = {"id", "title", "plaintiff", "plaintiff_type", "defendant", "defendant_type", "court",
                    "city_chn", "city_eng", "city_code", "prov_chn", "prov_eng", "prov_code", "year", "month", "day", "type",
                    "win", "result"};
            csvWriter = new CSVWriter(new FileWriter(path.toFile()));
            csvWriter.writeNext(line);
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void detailTest() throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String cipherText = ParamsUtils.cipher();
        String requestToken = ParamsUtils.random(24);
        RequestBody body = RequestBody.create(mediaType, "docId=Xo%2FiK6Op8trlGbKuM%2BhDwpExoytmSNNKByi7O7rVbXyCH7WhsjY%2FnZO3qNaLMqsJzkInSyKSXYa6Jnjrf1SqCp0mZ3bXOFIQ%2BF3TVW0NyjlZMa%2Ffj%2B3GAEeSJ%2Btay003&ciphertext=" + cipherText + "&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40docInfoSearch&__RequestVerificationToken=" + requestToken + "&wh=969&ww=924&cs=0");
        Request request = new Request.Builder()
                .url("https://wenshu.court.gov.cn/website/parse/rest.q4w")
                .method("POST", body)
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Cookie", cookie)
                .addHeader("Origin", "https://wenshu.court.gov.cn")
                .addHeader("Referer", "https://wenshu.court.gov.cn/website/wenshu/181107ANFZ0BXSK4/index.html?docId=Xo/iK6Op8trlGbKuM+hDwpExoytmSNNKByi7O7rVbXyCH7WhsjY/nZO3qNaLMqsJzkInSyKSXYa6Jnjrf1SqCp0mZ3bXOFIQ+F3TVW0NyjlZMa/fj+3GAEeSJ+tay003")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("sec-ch-ua", "\"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"108\", \"Google Chrome\";v=\"108\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .build();
        Response response = client.newCall(request).execute();
        Result result = JSON.parseObject(response.body().string(), Result.class);
        if (result.getSuccess()) {
            String iv = DateUtil.format(new Date(), "yyyyMMdd");
            if (result.getResult() == null) {
                System.out.println(result.getDescription());
                return;
            }
            String decrypt = TripleDES.decrypt(result.getSecretKey(), result.getResult(), iv);
            JSONObject jsonObject = JSON.parseObject(decrypt);
            String id = jsonObject.getString("s5");
            String name = jsonObject.getString("s1");
            String caseNo = jsonObject.getString("s7");
            String courtName = jsonObject.getString("s2");
            String refereeDate = jsonObject.getString("s31");
            String caseType = jsonObject.getString("s8");
            String trialProceedings = jsonObject.getString("s9");
            String docType = jsonObject.getString("s6");
            JSONArray causes = jsonObject.getJSONArray("s11");
            String cause = null;
//                if (causes != null) {
//                    cause = causes.stream().map(Object::toString).collect(joining(","));
//                }
            JSONArray partys = jsonObject.getJSONArray("s17");
            String party = null;
//                if (partys != null) {
//                    party = partys.stream().map(Object::toString).collect(joining(","));
//                }
            JSONArray keywords = jsonObject.getJSONArray("s45");
            String keyword = null;
//                if (keywords != null) {
//                    keyword = keywords.stream().map(Object::toString).collect(joining(","));
//                }
            String courtConsidered = jsonObject.getString("s26");
            String judgmentResult = jsonObject.getString("s27");
            String htmlContent = jsonObject.getString("qwContent");
            String fileName = "D:\\work\\wenshu\\result\\" + caseNo + ".html";
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                writer.write(htmlContent);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            jsonObject.remove("qwContent");
            String jsonContent = jsonObject.toJSONString();
            System.out.println(name);
        }
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

    private String parseCity(String courtName) {
        String regex = "(?<city>[^市]+自治州|.*?地区|.*?行政单位|.+盟|市辖区|.*?市|.*?县)(?<county>[^县]+县|.+区|.+市|.+旗|.+海域|.+岛)?(?<town>[^区]+区|.+镇)?(?<village>.*)";
        Matcher matcher = Pattern.compile(regex).matcher(courtName);
        String city = "TBD";
        List<Map<String, String>> table = new ArrayList<Map<String, String>>();
        Map<String, String> row = null;
        if (matcher.find()) {
            city = matcher.group("city");
            city = city == null ? "TBD" : city.trim();
            regex = "(?<province>[^省]+自治区|.*?省|.*?行政区|.*?市)(?<city>[^市]+自治州|.*?地区|.*?行政单位|.+盟|市辖区|.*?市|.*?县)";
            matcher = Pattern.compile(regex).matcher(city);
            if (matcher.find()) {
                city = matcher.group("city");
                city = city == null ? "TBD" : city.trim();
            }
            return city;
        }
        return city;
    }

    private String getProvChn(String city) {
        String[] cityLine = cityMap.get(city);
        if (cityLine != null) {
            return cityLine[0];
        }
        return "TBD";
    }

    private String getProvEng(String city) {
        String[] cityLine = cityMap.get(city);
        if (cityLine != null) {
            return cityLine[1];
        }
        return "TBD";
    }

    private String getProvCode(String city) {
        String[] cityLine = cityMap.get(city);
        if (cityLine != null) {
            return cityLine[2];
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

    private String parseProv(String city) {
        return "TBD";
    }

    private String parseWin(String winString) {
        return "TBD";
    }

    private String parsePlaintiff(List<String> plaintiffList) {
        StringBuilder plaintiff = new StringBuilder(plaintiffList.get(0));
        String plaintiffType = parsePlaintiffType(plaintiff.toString());
        if (plaintiffList.size() > 1) {
            for (int i = 1; i < plaintiffList.size(); i++) {
                String type = parsePlaintiffType(plaintiffList.get(i));
                if (plaintiffType.equals(type)) {
                    plaintiff.append(",").append(plaintiffList.get(i));
                }
            }
        }
        return plaintiff.toString();
    }

    private String parseDefendant(List<String> defendantList) {
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
        return String.join(",", defendants);
    }

    private void readCityCode() {
        try {
            String path = "D:\\work\\wenshu\\citcode.csv";
            String charset = "gbk";
            FileInputStream fileInputStream = new FileInputStream(path);
            Reader reader = new InputStreamReader(fileInputStream, charset);
            CSVReader csvReader = new CSVReader(reader);
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                cityList.add(line[3]);
                cityMap.put(line[3], line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFileTest() {
        String fileName = "D:\\work\\wenshu\\result\\test.html";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write("test");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseFile() {
        String fileName = "D:\\work\\wenshu\\result\\民事劳动争议-" + year + ".xlsx";
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(fileName));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Workbook writeWorkBook = new XSSFWorkbook();
            Sheet writeSheet = writeWorkBook.createSheet("sheet1");
            Row header = writeSheet.createRow(0);
            Cell headerCell;
            String[] line = {"id", "title", "plaintiff", "plaintiff_type", "defendant", "defendant_type", "court",
                    "city_chn", "city_eng", "city_code", "prov_chn", "prov_eng", "prov_code", "year", "month", "day", "type",
                    "win", "result"};
            for (int i = 0; i < line.length; i++) {
                headerCell = header.createCell(i);
                headerCell.setCellValue(line[i]);
            }

            int count = 0;
            for (Row row : sheet) {
                if (count == 0) {
                    count++;
                    continue;
                }
                String caseName = row.getCell(1).getStringCellValue();
                String caseNo = row.getCell(4).getStringCellValue();
                String courtName = row.getCell(2).getStringCellValue();
                String refereeDate = row.getCell(7).getStringCellValue();
                String caseType = row.getCell(20).getStringCellValue();
                String htmlContent = row.getCell(19).getStringCellValue();
                String judgmentResult = row.getCell(30).getStringCellValue();
                String htmlPath = "D:\\work\\wenshu\\result\\" + year + "\\" + caseNo + ".html";
                String cityName = parseCity(courtName);
                List<String> plaintiffList = JSONArray.parseArray(row.getCell(15).getStringCellValue(), String.class);
                String defendant = parseDefendant(plaintiffList);
                String plaintiff = parsePlaintiff(plaintiffList);
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
                    System.out.println("write file " + htmlPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Row writeRow = writeSheet.createRow(count);
                String[] parseLine = {caseNo, caseName, plaintiff, parsePlaintiffType(plaintiff), defendant, parsePlaintiffType(defendant),
                        courtName, cityName, getCityEng(cityName), getCityCode(cityName), getProvChn(cityName), getProvEng(cityName),
                        getProvCode(cityName), refereeYear, refereeMonth, refereeDay, caseType,
                        parseWin(judgmentResult), judgmentResult};
                for (int i = 0; i < parseLine.length; i++) {
                    Cell cell = writeRow.createCell(i);
                    cell.setCellValue(parseLine[i]);
                }
                count++;
            }
            workbook.close();
            String path = "D:\\work\\wenshu\\result\\" + year + "\\" + year + ".xlsx";
            FileOutputStream outputStream = new FileOutputStream(path);
            writeWorkBook.write(outputStream);
            writeWorkBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
