import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import utils.ParamsUtils;

import java.net.HttpCookie;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import utils.TripleDES;

import static java.util.stream.Collectors.joining;

public class DocumentService {

    private AtomicInteger days = new AtomicInteger(0);
    private Map<String, String> docTypeMap = new HashMap<>();
    private LocalDate date = LocalDate.now();
    private Integer min = 10;
    private Integer max = 30;
    private String sessionCookie = "wzws_sessionid=gWE4MThhYqBjrv9UgDE4My4xNC4xMzUuMTkzgjZmNjkwMQ==; SESSION=3219f5b9-18ec-4770-b773-a91d6014c748; wzws_cid=78534f65143035fd3dba75cde6035616a2e69fbc92b2cb9c246d6fd97c6fed20c2ccc32642064effce6363b4fca8560f6b749cc02023e24ddfdf5c0a62ac2a71e52bdcbd148603f9399b475562faf1db";
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";
    private String requestVerificationToken = ParamsUtils.random(24);
    private AtomicInteger intervalDays = new AtomicInteger(0);

    public void page(Integer pageNum, Integer pageSize) {
        if (pageNum == null) {
            pageNum = 0;
        }
        if (pageSize == null) {
            pageSize = 5;
        }
        if (date.minusDays(days.get()).getYear() < 1990) {
            return;
        }
        intervalDays.set(RandomUtil.randomInt(5, 10));
        list(pageNum, pageSize);
        days.getAndAdd(intervalDays.get());
        try {
            TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
//        page(pageNum, pageSize);
    }

    public void list(Integer pageNum, Integer pageSize) {
        String url = "https://wenshu.court.gov.cn/website/parse/rest.q4w";
        String pageId = ParamsUtils.getPageId();
        Map<String, Object> params = new HashMap<>();
        String start = date.minusDays(days.get() + intervalDays.get()).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String end = date.minusDays(days.get()).format(DateTimeFormatter.ISO_LOCAL_DATE);
        params.put("pageId", pageId);
        params.put("s8", "02");
        params.put("sortFields", "s51:desc");
        params.put("ciphertext", ParamsUtils.cipher());
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        Pair datePair = new Pair();
        datePair.setKey("cprq");
        datePair.setValue(start + " TO " + end);
        List<Pair> array = new ArrayList<>();
        array.add(datePair);
        String pairs = JSON.toJSONString(array);
        params.put("queryCondition", pairs);
        params.put("cfg", "com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO@queryDoc");
        params.put("__RequestVerificationToken", requestVerificationToken);
        params.put("wh", 699);
        params.put("ww", 1280);
        params.put("cs", 0);


        HttpCookie cookie = new HttpCookie("SESSION", sessionCookie);
        cookie.setDomain("wenshu.court.gov.cn");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //   cookie.setSecure(false);
        HttpResponse response = null;
        try {
            response = HttpRequest.post(url)
                    .form(params)
                    .timeout(-1)
                    .cookie(cookie)
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    //  .header("X-Real-IP", IpUtils.getIp())
                    //   .header("X-Forwarded-For", IpUtils.getIp())
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Length", params.toString().length() + "")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Host", "wenshu.court.gov.cn")
                    .header("Origin", "https://wenshu.court.gov.cn")
                    .header("Pragma", "no-cache")
                    .header("Referer", "https://wenshu.court.gov.cn/website/wenshu/181217BMTKHNT2W0/index.html?pageId=" + pageId + "&s8=" + params.get("s8"))
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", userAgent)
                    .header("sec-ch-ua", "\"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"108\", \"Google Chrome\";v=\"108\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "Windows")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .execute();

        } catch (Exception e) {
            try {
                TimeUnit.MINUTES.sleep(RandomUtil.randomInt(min, max));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            list(pageNum, pageSize);
        }
        try {
            System.out.println("Get list body:" + response.body());
            Result result = JSON.parseObject(response.body(), Result.class);
            if (result.getCode() == -12) {
                TimeUnit.HOURS.sleep(6);
                return;
            }
            int count = 0;
            if (result.getSuccess()) {
                String iv = DateUtil.format(new Date(), "yyyyMMdd");
                String decrypt = TripleDES.decrypt(result.getSecretKey(), result.getResult(), iv);
                JSONObject object = JSON.parseObject(decrypt);
                JSONArray jsonArray = object.getJSONObject("queryResult").getJSONArray("resultList");
                count = jsonArray.size();
                if (jsonArray.size() == 0) {
                    TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
                    return;
                }
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String caseNo = obj.getString("7");
                    String docId = obj.getString("rowkey");
                    detail(docId);
                }
            }
            TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
            if (count >= pageSize) {
                list(pageNum + 1, pageSize);
            }
        } catch (Exception e) {
            try {
                if (response != null) {
//                    log.error("body={}", response.body());
                    if (response.body().contains("307 Temporary Redirec")) {
                        TimeUnit.MINUTES.sleep(RandomUtil.randomInt(min, max));
                    }
                }
//                log.error("列表获取出错", e);

                TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            list(pageNum, pageSize);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void detail(String docId) {
        HttpResponse response = null;
        try {
            TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
            String url = "https://wenshu.court.gov.cn/website/parse/rest.q4w";
            Map<String, Object> params = new HashMap<>();
            params.put("docId", docId);
            params.put("ciphertext", ParamsUtils.cipher());
            params.put("cfg", "com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO@docInfoSearch");
            params.put("__RequestVerificationToken", ParamsUtils.random(24));
            params.put("wh", 150);
            params.put("ww", 1275);
            params.put("cs", 0);
            HttpCookie cookie = new HttpCookie("SESSION", sessionCookie);
            cookie.setDomain("wenshu.court.gov.cn");
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            response = HttpRequest.post(url)
                    .form(params)
                    .timeout(-1)
                    .cookie(cookie)
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    //   .header("X-Real-IP", IpUtils.getIp())
                    //   .header("X-Forwarded-For", IpUtils.getIp())
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Content-Length", params.toString().length() + "")
                    .header("Host", "wenshu.court.gov.cn")
                    .header("Origin", "https://wenshu.court.gov.cn")
                    .header("Referer", "https://wenshu.court.gov.cn/website/wenshu/181107ANFZ0BXSK4/index.html?docId=" + docId)
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", userAgent)
                    .header("sec-ch-ua", "\"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"108\", \"Google Chrome\";v=\"108\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "Windows")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .execute();
            Result result = JSON.parseObject(response.body(), Result.class);
            if (result.getSuccess()) {
                String iv = DateUtil.format(new Date(), "yyyyMMdd");
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
            }
        } catch (Exception e) {
            try {
                if (response != null) {
//                    log.error("body={}", response.body());
                    if (response.body().contains("307 Temporary Redirect")) {
                        TimeUnit.MINUTES.sleep(RandomUtil.randomInt(min, max));
                    }
                }
//                log.error("详情获取出错", e);
                TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            detail(docId);
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }


//    public List<Dict> getCourt(String code) {
//        String url = "https://wenshu.court.gov.cn/website/parse/rest.q4w";
//        String pageId = UUID.randomUUID().toString().replace("-", "");
//        Map<String, Object> params = new HashMap<>();
//        params.put("pageId", pageId);
//        params.put("s8", "02");
//        params.put("parentCode", code);
//        params.put("cfg", "com.lawyee.judge.dc.parse.dto.LoadDicDsoDTO@loadFyByCode");
//        params.put("__RequestVerificationToken", ParamsUtils.random(24));
//        params.put("wh", 470);
//        params.put("ww", 1680);
//        HttpResponse response = null;
//        try {
//            TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
//            response = HttpRequest.post(url)
//                    .form(params)
//                    .timeout(-1)
//                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
//                    .header("X-Real-IP", IpUtils.getIp())
//                    .header("X-Forwarded-For", IpUtils.getIp())
//                    .header("Accept-Encoding", "gzip, deflate, br")
//                    .header("Accept-Language", "zh-CN,zh;q=0.9")
//                    .header("Connection", "keep-alive")
//                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                    .header("Host", "wenshu.court.gov.cn")
//                    .header("Origin", "https://wenshu.court.gov.cn")
//                    .header("Referer", "https://wenshu.court.gov.cn/website/wenshu/181217BMTKHNT2W0/index.html?pageId=" + pageId + "&s8=02")
//                    .header("Sec-Fetch-Dest", "empty")
//                    .header("Sec-Fetch-Mode", "cors")
//                    .header("Sec-Fetch-Site", "same-origin")
//                    //  .header("User-Agent", configList.get(index).getAgent())
//                    //  .header("sec-ch-ua", configList.get(index).getChua())
//                    .header("sec-ch-ua-mobile", "?0")
//                    .header("sec-ch-ua-platform", "Windows")
//                    .header("X-Requested-With", "XMLHttpRequest")
//                    .execute();
//
//            Result result = JSON.parseObject(response.body(), Result.class);
//            JSONObject object = JSON.parseObject(result.getResult());
//            List<Dict> courts = JSON.parseArray(object.getJSONArray("fy").toJSONString(), Dict.class);
//            List<Dict> countList = new CopyOnWriteArrayList<>();
//            if (courts.size() == 0) {
//                return countList;
//            }
//            countList.addAll(courts);
//            for (Dict court : courts) {
//                countList.addAll(getCourt(court.getCode()));
//            }
//            return countList;
//        } catch (Exception e) {
//            if (response != null) {
//                log.error("body={}", response.body());
//            }
//            log.error("发送列表请求出错", e);
//            try {
//                TimeUnit.SECONDS.sleep(RandomUtil.randomInt(min, max));
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return new ArrayList<Dict>();
//    }

}
