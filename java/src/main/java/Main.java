import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.*;
import utils.ParamsUtils;
import utils.TripleDES;

import java.io.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Main {

    private final String cookie = "wzws_sessionid=gWE4MThhYqBjrv9UgDE4My4xNC4xMzUuMTkzgjZmNjkwMQ==; SESSION=3219f5b9-18ec-4770-b773-a91d6014c748; wzws_cid=78534f65143035fd3dba75cde6035616a2e69fbc92b2cb9c246d6fd97c6fed20c2ccc32642064effce6363b4fca8560f6b749cc02023e24ddfdf5c0a62ac2a71e52bdcbd148603f9399b475562faf1db";

    public static void main(String[] args) {
//        new DocumentService().page(1, 5);
        new Main().fetchList();
    }

    private void fetchList() {
        OkHttpClient client = new OkHttpClient();
        String pageId = ParamsUtils.getPageId();
        String cipherText = ParamsUtils.cipher();
        String requestToken = ParamsUtils.random(24);
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, "pageId=" + pageId +
                "&s21=%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE&sortFields=s50%3Adesc" +
                "&ciphertext=" + cipherText +
                "&pageNum=1&queryCondition=%5B%7B%22key%22%3A%22s21%22%2C%22value%22%3A%22%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE%22%7D%5D&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40queryDoc" +
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
            Result result = JSON.parseObject(responseBody, Result.class);
            if (result.getSuccess()) {
                String iv = DateUtil.format(new Date(), "yyyyMMdd");
                String decrypt = TripleDES.decrypt(result.getSecretKey(), result.getResult(), iv);
                JSONObject object = JSON.parseObject(decrypt);
                JSONArray jsonArray = object.getJSONObject("queryResult").getJSONArray("resultList");
                if (jsonArray.size() == 0) {
                    TimeUnit.SECONDS.sleep(RandomUtil.randomInt(5, 10));
                    return;
                }
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String caseNo = obj.getString("7");
                    String docId = obj.getString("rowkey");
                    fetchDetail(docId);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fetchDetail(String docId) throws IOException, InterruptedException {
        System.out.println(docId);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String cipherText = ParamsUtils.cipher();
        String requestToken = ParamsUtils.random(24);
        RequestBody body = RequestBody.create(mediaType, "docId=" + docId + "&ciphertext=" + cipherText +
                "&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40docInfoSearch&__RequestVerificationToken="+requestToken+"&wh=969&ww=937&cs=0");
        Request request = new Request.Builder()
                .url("https://wenshu.court.gov.cn/website/parse/rest.q4w")
                .method("POST", body)
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Cookie", "wzws_sessionid=gWE4MThhYqBjrv9UgDE4My4xNC4xMzUuMTkzgjZmNjkwMQ==; SESSION=3219f5b9-18ec-4770-b773-a91d6014c748")
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
        Result result = JSON.parseObject(response.body().string(), Result.class);
        if (result.getSuccess()) {
            String iv = DateUtil.format(new Date(), "yyyyMMdd");
            if(result.getResult() == null) {
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
            jsonObject.remove("qwContent");
            String jsonContent = jsonObject.toJSONString();
            System.out.println(name);
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
}
