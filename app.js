const http = require('http');
const request = require('request');
const DES3 = require('./DES3');

const hostname = '127.0.0.1';
const port = 3000;
const options = {
    'method': 'POST',
  'url': 'https://wenshu.court.gov.cn/website/parse/rest.q4w',
  'headers': {
    'Accept': 'application/json, text/javascript, */*; q=0.01',
    'Accept-Language': 'en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7',
    'Connection': 'keep-alive',
    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
    'Cookie': 'wzws_sessionid=oGOB4gOAMTgzLjE0LjEzNC4yNDCBYTRiZjUxgjZmNjkwMQ==; SESSION=a19f9010-4230-43eb-b7ef-70468b40d978; wzws_reurl=L3dlYnNpdGUvd2Vuc2h1L2ltYWdlcy9mYXZpY29uLmljbw==',
    'Origin': 'https://wenshu.court.gov.cn',
    'Referer': 'https://wenshu.court.gov.cn/website/wenshu/181217BMTKHNT2W0/index.html?pageId=c27bafa639ae039cfbe912cb2b166e19&s21=%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE',
    'Sec-Fetch-Dest': 'empty',
    'Sec-Fetch-Mode': 'cors',
    'Sec-Fetch-Site': 'same-origin',
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36',
    'X-Requested-With': 'XMLHttpRequest',
    'sec-ch-ua': '"Chromium";v="106", "Google Chrome";v="106", "Not;A=Brand";v="99"',
    'sec-ch-ua-mobile': '?0',
    'sec-ch-ua-platform': '"macOS"'
  },
  body: 'pageId=c27bafa639ae039cfbe912cb2b166e19&s21=%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE&sortFields=s50%3Adesc&ciphertext=1000111+1010000+1010111+1101111+1001010+1010001+1000111+111000+1010110+1001011+1001100+111000+1100111+1110101+1010011+1110101+1010000+1000111+1101110+1110010+1100111+1000111+1011001+1001100+110010+110000+110010+110010+110001+110000+110010+110110+1000011+1111000+1011010+1001010+1110010+1100010+1010110+1101000+110111+1000100+1010101+1110001+1110110+1100100+1101101+1101011+1000100+1001111+110010+1101010+1100110+1110111+111101+111101&pageNum=2&queryCondition=%5B%7B%22key%22%3A%22s21%22%2C%22value%22%3A%22%E5%8A%B3%E5%8A%A8%E4%BA%89%E8%AE%AE%22%7D%5D&cfg=com.lawyee.judge.dc.parse.dto.SearchDataDsoDTO%40queryDoc&__RequestVerificationToken=nB0iGU32M5IMT6rgfSEUwO8I&wh=715&ww=907&cs=0'
}

const server = http.createServer((req, res) => {
    res.statusCode = 200;
    res.setHeader('Content-Type', 'text/plain');
    res.end('Hello World');
});

server.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
    request(options, function (error, response) {
        if (error) throw new Error(error);
        const data = JSON.parse(response.body);
        console.log(DES3.decrypt(data.secretKey, data.result));
      });
});
