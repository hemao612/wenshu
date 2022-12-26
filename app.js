const http = require('http');
const request = require('request');
const DES3 = require('./DES3');

const hostname = '127.0.0.1';
const port = 3000;

const server = http.createServer((req, res) => {
    res.statusCode = 200;
    res.setHeader('Content-Type', 'text/plain');
    res.end('Hello World');
});

server.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
    let cookie = 'wzws_sessionid=gDE4My4xNC4xMzUuMTkzgTg2ZWE4MoI2ZjY5MDGgY6lzVw==; SESSION=c695747c-16b5-47d5-9e6f-5ffa28a3dd1e; wzws_cid=acd33c4e4d5d81ac0ce5ccbaa7f2033fdda3eea64932202bbd3e949c021bb1dd24c37fb0b3f3ac6854a69a2cddab04ff402771af48e88ae9863560fbe5aabbdfa4065db3d9ec6747b35e63c909c688ba';
    let pageId = 'G2ettwQ8Ozihc1YHH4Sj7PAL';
    // let pageId = '185537294c08cb6a6711c0a0e6fee4ad';
    // let ciphertext = '1110010+1101001+110010+1001111+1101111+1010001+1011000+1101001+1000100+110011+1101011+1100011+110111+110110+1100101+1011001+1110101+1001011+1011001+1110101+1001111+1110110+110110+1010001+110010+110000+110010+110010+110001+110001+110000+110100+1100010+1000101+110111+110101+1100100+1110111+1010011+1001011+1001001+1000110+1101110+1011001+1110100+1110110+101011+1100010+1001101+1110000+111000+1001110+1100110+1010001+111101+111101';
    let ciphertext = cipher();
    let requestToken = 'fTtEVEwNF7RGwqJgCxJrfpSe';
    getList(cookie, pageId, ciphertext, requestToken);
});

function getDetail(cookie, docId, ciphertext, requestToken) {
    console.log('reading docId:' + docId);
    let options = {
        'method': 'POST',
        'url': 'https://wenshu.court.gov.cn/website/parse/rest.q4w',
        'headers': {
            'Accept': 'application/json, text/javascript, */*; q=0.01',
            'Accept-Language': 'en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7',
            'Connection': 'keep-alive',
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'Cookie': cookie,
            'Origin': 'https://wenshu.court.gov.cn',
            'Referer': 'https://wenshu.court.gov.cn/website/wenshu/181107ANFZ0BXSK4/index.html?docId='+docId,
            'Sec-Fetch-Dest': 'empty',
            'Sec-Fetch-Mode': 'cors',
            'Sec-Fetch-Site': 'same-origin',
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36',
            'X-Requested-With': 'XMLHttpRequest',
            'sec-ch-ua': '"Not?A_Brand";v="8", "Chromium";v="108", "Google Chrome";v="108"',
            'sec-ch-ua-mobile': '?0',
            'sec-ch-ua-platform': '"macOS"'
        },
        body: 'docId='+docId+'&ciphertext='+ciphertext+'&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40docInfoSearch&__RequestVerificationToken='+requestToken+'&wh=722&ww=864&cs=0'
    }

    request(options, function (error, response) {
        if (error) throw new Error(error);
        const data = JSON.parse(response.body);
        console.log(data);
        if (data.success) {
            let rawContent = DES3.decrypt(data.secretKey, data.result);
            console.log(rawContent);
        } else {
            console.log(data.description);
        }
    });
}

function parseContent(content) {
    content = content.substring(2, content.length - 1);
    content = content.replace('"1"', '"title"');
    content = content.replace('"2"', '"court"');
    content = content.replace('"7"', '"version"');
    content = content.replace('"9"', '"number"');
    content = content.replace('"10"', '"keya"');
    content = content.replace('"26"', '"breif"');
    content = content.replace('"31"', '"date"');
    content = content.replace('"32"', '"keyb"');
    content = content.replace('"43"', '"key43"');
    content = content.replace('"44"', '"key44"');
    const jsonContent = JSON.parse(content);
    let resultList = jsonContent.resultList;
    return resultList;
}

function login() {
    const options = {
        'method': 'POST',
        'url': 'https://account.court.gov.cn/api/login',
        'headers': {
            'Accept': '*/*',
            'Accept-Language': 'en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7',
            'Connection': 'keep-alive',
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'Cookie': '_bl_uid=3nlayag7q4I42R9dadwtcw36jmne; ncCookie=NIC0REr5ul99Ft5AvojmEILp7T40_9xGO2gfbop_sVkPL9_Ztce29l3nKjKSCy3VQSUWb0gnkHEhC0fFn-YpLQXPP2Y4GafBsPFtKO4d4F5V2bnFwXFOcvcPt51qqcfo; wzws_sessionid=oGOW5EqCZDI5NDdjgDE4My4xNC4zMS4xMjeBODZlYTgy; HOLDONKEY=NDNlNWRiZDEtYjk1Ni00NDEzLTgxY2EtZjNhNDFlYTQ3ZWM3; HOLDONKEY=ODBmYjc1YjEtZmVhOS00NmFiLTg3YjgtOWM3NzgyZTA2NjE3',
            'Origin': 'https://account.court.gov.cn',
            'Referer': 'https://account.court.gov.cn/app?back_url=https%3A%2F%2Faccount.court.gov.cn%2Foauth%2Fauthorize%3Fresponse_type%3Dcode%26client_id%3Dzgcpwsw%26redirect_uri%3Dhttps%253A%252F%252Fwenshu.court.gov.cn%252FCallBackController%252FauthorizeCallBack%26state%3D2e680322-b170-4099-8258-79f9eea3c268%26timestamp%3D1670833226573%26signature%3D64DB8CB82874F9258FFE75AB3362A6FDB2F20EE59C06DA6773A53CAFB5409863%26scope%3Duserinfo',
            'Sec-Fetch-Dest': 'empty',
            'Sec-Fetch-Mode': 'cors',
            'Sec-Fetch-Site': 'same-origin',
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36',
            'X-Requested-With': 'XMLHttpRequest',
            'sec-ch-ua': '"Not?A_Brand";v="8", "Chromium";v="108", "Google Chrome";v="108"',
            'sec-ch-ua-mobile': '?0',
            'sec-ch-ua-platform': '"macOS"'
        },
        body: 'username=18817551681&password=1mexeytEc0%252FNRXK8rCoxgNslOEoujH0hFfSqC%252Bf1l4ZcL%252FFPc4Aeo%252FE%252FP5V%252FXXyKlhRx46teHaUF5Q0D2DSE9LLTeVgRC0LczgcNogqwbEDYYABLzrXTBopB%252BLFgpt%252FaXjvbb1e6Vel46Ks6uJJi7xPN7U2cwCYtoBdCItpJ0apaarO1b7ArFbJ6e86LybvMUJ4jSNLiMIQredI%252F9pd9fAZlwGl%252BQ16JqAB4Nu5jCjub7aAFE6hEGWjCmMk1rdd5s3lqRMLO3Prs3DqFZ1LMDie%252Fn3U%252Fo%252Fid7kGwkO5iyCL97PF%252FWoWH2iCBPYrIqXW002r7uCWaNShkGhsoFCkk8g%253D%253D&appDomain=wenshu.court.gov.cn'
    };

    request(options, function (error, response) {
        if (error) throw new Error(error);
        let responseCookies = response.headers['set-cookie'];
        let oneCookie = responseCookies[0];
        oneCookie = oneCookie.split(';');
        oneCookie = oneCookie[0];
        console.log(oneCookie);
        return oneCookie;
    });
}

function getList(cookie, pageId, ciphertext, requestToken) {
    const options = {
        'method': 'POST',
        'url': 'https://wenshu.court.gov.cn/website/parse/rest.q4w',
        'headers': {
            'Accept': 'application/json, text/javascript, */*; q=0.01',
            'Accept-Language': 'en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7',
            'Connection': 'keep-alive',
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'Cookie': cookie,
            'Origin': 'https://wenshu.court.gov.cn',
            'Referer': 'https://wenshu.court.gov.cn/website/wenshu/181217BMTKHNT2W0/index.html?pageId=ed0164e8db1836a4c0c769d9c9944d45&s21=%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE',
            'Sec-Fetch-Dest': 'empty',
            'Sec-Fetch-Mode': 'cors',
            'Sec-Fetch-Site': 'same-origin',
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36',
            'X-Requested-With': 'XMLHttpRequest',
            'sec-ch-ua': '"Not?A_Brand";v="8", "Chromium";v="108", "Google Chrome";v="108"',
            'sec-ch-ua-mobile': '?0',
            'sec-ch-ua-platform': '"macOS"'
        },
        body: 'pageId=' + pageId + '&s21=%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE&sortFields=s50%3Adesc&' +
            'ciphertext=' + ciphertext + '&pageNum=1&queryCondition=%5B%7B%22key%22%3A%22s21%22%2C%22value%22%3A%22%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE%22%7D%5D&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40queryDoc&' +
            '__RequestVerificationToken=' + requestToken + '&wh=715&ww=871&cs=0'
    }

    request(options, function (error, response) {
        if (error) throw new Error(error);
        const data = JSON.parse(response.body);
        if (data.success) {
            let rawContent = DES3.decrypt(data.secretKey, data.result);
            let content = rawContent.split("queryResult", 2)[1];
            resultList = parseContent(content);
            rowKey = resultList[0].rowkey;
            getDetail(cookie, rowKey, ciphertext, requestToken);
        } else {
            console.log(data.description);
        }
    });
}

function cipher() {
    var date = new Date();
    var timestamp = date.getTime().toString();
    var salt = 'fTtEVEwNF7RGwqJgCxJrfpSe';
    var year = date.getFullYear().toString();
    var month = (date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date
        .getMonth()).toString();
    var day = (date.getDate() < 10 ? "0" + date.getDate() : date.getDate())
        .toString();
    var iv = year + month + day;
    var enc = DES3.encrypt(timestamp, salt, iv).toString();
    var str = salt + iv + enc;
    var ciphertext = strTobinary(str);
    return ciphertext;
}
function strTobinary(str) {
    var result = [];
    var list = str.split("");
    for (var i = 0; i < list.length; i++) {
        if (i != 0) {
            result.push(" ");
        }
        var item = list[i];
        var binaryStr = item.charCodeAt().toString(2);
        result.push(binaryStr);
    };
    return result.join("");
}

