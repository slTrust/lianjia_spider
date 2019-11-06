package com.util;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class UrlReader {
    public static final Map<String,String> xpathExpMap = new HashMap<>();
    public static final String NODE_AREA_A = "node_area_a";
    static {
        xpathExpMap.put(NODE_AREA_A,"//div[@data-role=\"ershoufang\"]/div/a");

    }

    public static String baseUrl = "https://tj.lianjia.com";
    public static XPath xPath = XPathFactory.newInstance().newXPath();
    public static int delayTime = 10;


    public static final List<String> streetUrls = new ArrayList<>();
    public static final List<String> streetPageNoUrls = new ArrayList<>();
    public static final List<String> houseUrls = new ArrayList<>();
    public static final Map<String,List<String>> houseMapper = new HashMap<>();
    public static final List<Object> houseInfos = new ArrayList<>();

    public static Document read(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .timeout(5_000)
                .get();
    }

    public static org.w3c.dom.Document cleanDom(String html) throws ParserConfigurationException {
        HtmlCleaner hc = new HtmlCleaner();
        TagNode tn = hc.clean(html);
        org.w3c.dom.Document dom = new DomSerializer(new CleanerProperties()).createDOM(tn);
        return dom;
    }


    public static Object getPathResultByUrlAndXpathExpAndQName(String url , String xpathExp, QName qName){
        try {
            String html = null;
            html = UrlReader.read(url).body().html();
            return xPath.evaluate(xpathExp, cleanDom(html),qName);
        } catch (IOException | ParserConfigurationException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<NodeList> getNodeListByUrlAndXpathExpAndQName(String url , String[] xpathExp, QName[] qName){
        try {
            List<NodeList> res = new ArrayList<>();
            String html = null;
            html = UrlReader.read(url).body().html();
            for (int i = 0; i < xpathExp.length; i++) {
                res.add((NodeList) xPath.evaluate(xpathExp[i], cleanDom(html),qName[i]));
            }
            return res;
        } catch (IOException | ParserConfigurationException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<String> getUrlListByNodeList(NodeList nodeList){
        List<String> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            result.add(node.getNodeValue());
        }
        return result;
    }

    public static List<String>  getAreaUrls(){
        return  getAreaInfos().stream().map(item->item.get("href")).collect(Collectors.toList());
    }

    public static List<Map<String,String>>  getAreaInfos(){
        List<Map<String,String>> result = new ArrayList<>();
        String startUrl = baseUrl + "/ershoufang/";
        String[] xpathExps = new String[]{xpathExpMap.get(NODE_AREA_A)};
        QName[] qnames = new QName[]{XPathConstants.NODESET};
        List<NodeList> nodeLists =getNodeListByUrlAndXpathExpAndQName(startUrl,xpathExps,qnames);
        for (int i = 0; i < nodeLists.get(0).getLength(); i++) {
            Map<String,String> info = new HashMap<>();
            String hrefPath = nodeLists.get(0).item(i).getAttributes().getNamedItem("href").getNodeValue();
            String name = nodeLists.get(0).item(i).getTextContent();
            info.put("href", baseUrl + hrefPath);
            info.put("name",name);
            info.put("code",hrefPath.replace("/ershoufang/","").replace("/","").toString());
            result.add(info);
        }
        return result;
    }




    public static List<String> getStreetUrls(String areaUrl){
        String xpathExp = "//div[@data-role=\"ershoufang\"]/div[2]/a/@href";
        Object pathResult = getPathResultByUrlAndXpathExpAndQName(areaUrl,xpathExp,XPathConstants.NODESET);
        List<String> streetUrl = getUrlListByNodeList((NodeList) pathResult).stream().map(item-> baseUrl + item).collect(Collectors.toList());
        return streetUrl;
    }

    public static String getStreetPageInfoUrl(String areaUrl)  {
        String xpathExp = "//*[@class=\"contentBottom clear\"]/div[2]/div[1]/@page-data";
        Node pathResult = (Node)getPathResultByUrlAndXpathExpAndQName(areaUrl,xpathExp,XPathConstants.NODE);
        boolean strictMode = true;
        if(pathResult!=null){
            // 把 转义的字符 还原
            String unescapedString = org.jsoup.parser.Parser.unescapeEntities(pathResult.getNodeValue(), strictMode);
            return unescapedString;
        }else{
            return "{\"totalPage\":0,\"curPage\":0}";
        }

    }

    public static List<String> convertPageToUrls(String url,String pageJson){
        List<String> result = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(pageJson);
        int totalPage = jsonObject.getInt("totalPage");
        int count = 1;
        while(count <= totalPage){
            String page = count==1?"":"pg"+count;
            result.add(url + page);
            count++;
        }
        return result;
    }

    public static List<String> getHouseUrl(String streetPageUrl) {
        Object pathResult = getPathResultByUrlAndXpathExpAndQName(streetPageUrl,"//div[@class=\"leftContent\"]/ul/li/a[@data-el=\"ershoufang\"]/@href",XPathConstants.NODESET);
        return getUrlListByNodeList((NodeList) pathResult);
    }

    public static Map<String,Object> getHouseDetailByUrl(String url) {
        try {
            Map<String,Object> result = new HashMap<>();
            String html = null;
            html = UrlReader.read(url).body().html();
            String title = "";
            String titlexPath = "//div[@class=\"sellDetailHeader\"]//div[@class=\"title\"]/h1";
            Node titleNode = (Node)xPath.evaluate(titlexPath, cleanDom(html),XPathConstants.NODE);
            title = titleNode.getTextContent();

            String imgurlPath = "//div[@id=\"thumbnail2\"]/ul/li/img/@src";
            NodeList nodeList = (NodeList) xPath.evaluate(imgurlPath, cleanDom(html),XPathConstants.NODESET);
            // 房屋图片
            List<String> previewImages = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                previewImages.add(node.getNodeValue());
            }
            Map<String,String> baseInfos = new HashMap<>();
            String baseInfoExp = "//*[@id=\"introduction\"]/div/div/div[1]/div[2]/ul/li/span";
            String baseInfoExp2 = "//*[@id=\"introduction\"]/div/div/div[1]/div[2]/ul/li";
            NodeList infoLabels= (NodeList) xPath.evaluate(baseInfoExp, cleanDom(html),XPathConstants.NODESET);
            NodeList infoValues = (NodeList) xPath.evaluate(baseInfoExp2, cleanDom(html),XPathConstants.NODESET);
            for (int i = 0; i < infoLabels.getLength(); i++) {
                Node node = infoLabels.item(i);
                Node node2 = infoValues.item(i);
                String key = node.getTextContent();
                String cacheValue = node2.getTextContent();
                String value = cacheValue.substring(cacheValue.lastIndexOf(key) + key.length() );
                baseInfos.put(key,value);
            }

            Map<String,String> tradeInfos = new HashMap<>();
            String tradeInfoExp = "//*[@id=\"introduction\"]/div/div/div[2]/div[2]/ul/li/span[1]";
            String tradeInfoExp2 = "//*[@id=\"introduction\"]/div/div/div[2]/div[2]/ul/li/span[2]";
            NodeList tradeLabels= (NodeList) xPath.evaluate(tradeInfoExp, cleanDom(html),XPathConstants.NODESET);
            NodeList tradeValues = (NodeList) xPath.evaluate(tradeInfoExp2, cleanDom(html),XPathConstants.NODESET);
            for (int i = 0; i < tradeLabels.getLength(); i++) {
                Node node = tradeLabels.item(i);
                Node node2 = tradeValues.item(i);
                String key = node.getTextContent();
                String value = node2.getTextContent();
                tradeInfos.put(key,value);
            }

            result.put("title",title);
            result.put("link",url);
            result.put("previewImages",previewImages);
            result.put("baseInfo",baseInfos);
            result.put("tradeInfo",tradeInfos);

            return result;



        } catch (IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getStreetUrlByThread() {
        List<String> areas = getAreaUrls();
        if(areas.size() > 0){
            System.out.println(areas.size());
            task(areas,WorkThread.class);

            System.out.println("run next process 所有的街道链接读取完毕. 开始解析页码～～～～");
            System.out.println(streetUrls);
            System.out.println(streetUrls.size());
            task(streetUrls,WorkThread2.class);

            System.out.println("run next process 所有的街道的所有页码链接读取完毕. 开始解析house～～～～");
            System.out.println(streetPageNoUrls.size());
            System.out.println(streetPageNoUrls);


            task(streetPageNoUrls,WorkThread3.class);
            System.out.println("run next process houseUrl 读取完毕. 开始解析house～～～～");
            System.out.println(houseUrls.size());
            System.out.println(houseUrls);
//            System.out.println(houseMapper);
            System.out.println(houseMapper.keySet());
            task(houseUrls,WorkThread4.class);
            System.out.println(houseInfos);
            System.out.println(houseInfos.size());

        }
        return null;
    }


    public static class WorkThread extends Thread {
        CountDownLatch countDownLatch;
        String url;

        public WorkThread(CountDownLatch countDownLatch, String url) {
            this.countDownLatch = countDownLatch;
            this.url = url;
        }

        @Override
        public void run() {
            System.out.println(getName() +"----a----" + url );

            streetUrls.addAll(getStreetUrls(url));
            //执行子任务完毕之后，countDown减少一个点
            System.out.println(getName() +"-------- finished");
            countDownLatch.countDown();
        }
    }

    public static class WorkThread2 extends WorkThread {
        public WorkThread2(CountDownLatch countDownLatch, String url) {
            super(countDownLatch, url);
        }

        @Override
        public void run() {
//            System.out.println(getName() + "run 解析pageSize start. url=" + url);
            String aaa = getStreetPageInfoUrl(url);
            System.out.println(getName() +"--------" + url + aaa);

            List<String> urls = convertPageToUrls(url,aaa);
            streetPageNoUrls.addAll(urls);
            countDownLatch.countDown();
            System.out.println(getName() + "run finished.");
        }
    }

    public static class WorkThread3 extends WorkThread {
        public WorkThread3(CountDownLatch countDownLatch, String url) {
            super(countDownLatch, url);
        }

        @Override
        public void run() {
//                System.out.println(getName() + "run 解析pageSize start. url=" + url);
            System.out.println(getName() +"---c-----" + url);

            String tmp = url.substring(url.indexOf("/ershoufang/")+"/ershoufang/".length());
            String streetCode = tmp.substring(0,tmp.indexOf("/"));
            System.out.println(streetCode);
            List<String> urls = getHouseUrl(url);
            houseUrls.addAll(urls);
            if(houseMapper.get(streetCode)!=null){
                houseMapper.get(streetCode).addAll(urls);
            }else{
                List<String> houseUrls = new ArrayList<>();
                houseUrls.addAll(urls);
                houseMapper.put(streetCode,houseUrls);
            }
            countDownLatch.countDown();
            System.out.println(getName() + "run finished.");
        }
    }

    public static class WorkThread4 extends WorkThread {
        public WorkThread4(CountDownLatch countDownLatch, String url) {
            super(countDownLatch, url);
        }

        @Override
        public void run() {
            System.out.println(getName() + "run 解析 房源详情 start. url=" + url);
            System.out.println(getName() +"---d-----" + url);
            Map<String,Object> infos = getHouseDetailByUrl(url);
            houseInfos.add(infos);
            countDownLatch.countDown();
            System.out.println(getName() + "run finished.");
        }
    }

    //
    public static void task(List<String> list,Class klass) {
//        list = list.subList(0,3);
        CountDownLatch countDownLatch = new CountDownLatch(list.size());
        for (int i = 0; i < list.size() ; i++) {
            try {
                Thread.sleep(new Random().nextInt(3) * delayTime);
                Constructor constructor = klass.getConstructor(CountDownLatch.class,String.class);
                WorkThread thread = (WorkThread)constructor.newInstance(countDownLatch,list.get(i));
                thread.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        try {
            //调用await方法阻塞当前线程，等待子线程完成后在继续执行
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        getAreaInfos();
//        List<String> areaUrls = getAreaUrls(); // https://tj.lianjia.com/ershoufang/heping/
//        System.out.println(areaUrls);
        // 获取某个区的所有街道url
//        List<String> streetUrls = getStreetUrls(areaUrls.get(0));
//        System.out.println(streetUrls);
//        // 获取某个街道所有页码url
//        List<String> streetPageUrls = convertPageToUrls(streetUrls.get(0),getStreetPageInfoUrl(streetUrls.get(0)));
//        System.out.println(streetPageUrls);
//
//        // 获取某页 所有房子url
//        List<String> onePagehouseUrls = getHouseUrl(streetPageUrls.get(0));
//        System.out.println(onePagehouseUrls);

        // 获取一个房源信息的 详情
//        System.out.println(getHouseDetailByUrl("https://tj.lianjia.com/ershoufang/101105795103.html"));
        // lv1 area 和平区
        // lv2 street 鞍山道
        // lv3 street pg1 鞍山道
        // lv4 street pg1 house 01
        // lv5 house info

    }

}
