package com.util;

import dao.AreaStreetDao;
import entity.Area;
import entity.Street;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UrlReader {
    public static final AreaStreetDao areaDao = new AreaStreetDao(SqlSessionUtil.getSessionFactory());
    public static int count = 0;
    public static final int delayTime = 100;
    public static final Map<String,String> xpathExpMap = new HashMap<>();
    public static final String NODE_AREA_A = "NODE_AREA_A";
    public static final String NODE_STREET_A = "NODE_STREET_A";
    public static final String PAGE_INFO_ATTR = "PAGE_INFO_ATTR";
    public static final String NODE_HOUSE_TITLE = "NODE_HOUSE_TITLE";
    public static final String NODE_HOUSE_TOTAL_PRICE = "NODE_HOUSE_TOTAL_PRICE";
    static {
        xpathExpMap.put(NODE_AREA_A,"//div[@data-role=\"ershoufang\"]/div/a");
        xpathExpMap.put(NODE_STREET_A,"//div[@data-role=\"ershoufang\"]/div[2]/a");
        xpathExpMap.put(PAGE_INFO_ATTR,"//*[@class=\"contentBottom clear\"]/div[2]/div[1]/@page-data");
        xpathExpMap.put(NODE_HOUSE_TITLE,"//div[@class=\"sellDetailHeader\"]//div[@class=\"title\"]/h1");
//        xpathExpMap.put(NODE_HOUSE_TOTAL_PRICE,"/html/body/div[5]/div[2]/div[2]/span[1]");
        xpathExpMap.put(NODE_HOUSE_TOTAL_PRICE,"/html/body//div[@class=\"content\"]/div[@class=\"price\"]/span[@class=\"total\"]");
    }

    public static final List<Area> cacheAreaInfos = new ArrayList<>();
    public static final List<Street> cacheStreetInfos = new ArrayList<>();

    public static String baseUrl = "https://tj.lianjia.com";

    /**
     *
     * @param url
     * @param xpathExp 表达式数组
     * @param qName NodeType
     * @return
     */
    public static List<NodeList> getNodeListByUrlAndXpathExpAndQName(String url , List<String> xpathExp, List<QName> qName){
        try {
            List<NodeList> res = new ArrayList<>();
            String html = UrlUtils.read(url).body().html();
            for (int i = 0; i < xpathExp.size(); i++) {
                res.add((NodeList) UrlUtils.xPath.evaluate(xpathExp.get(i), UrlUtils.cleanDom(html),qName.get(i)));
            }
            return res;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

        public static Object getPathResultByUrlAndXpathExpAndQName(String url , String xpathExp, QName qName){
        try {
            String html = null;
            html = UrlUtils.read(url).body().html();
            return UrlUtils.xPath.evaluate(xpathExp, UrlUtils.cleanDom(html),qName);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<Area> getAreaInfos(){
        if(cacheAreaInfos.size() == 0){
            String startUrl = baseUrl + "/ershoufang/";
            String[] xpathExps = new String[]{xpathExpMap.get(NODE_AREA_A)};
            QName[] qnames = new QName[]{XPathConstants.NODESET};
            List<NodeList> nodeLists =getNodeListByUrlAndXpathExpAndQName(startUrl,Arrays.asList(xpathExps),Arrays.asList(qnames));
            cacheAreaInfos.addAll(mapAreaOrStreetLinkNodetoListMap(nodeLists.get(0),"area",null)
                    .stream()
                        .map(item->new Area(item.get("area_code"),item.get("area_name"),item.get("area_link")))
                        .collect(Collectors.toList()));
        }
        return cacheAreaInfos;
    }

    public static List<Map<String,String>>  mapAreaOrStreetLinkNodetoListMap(NodeList nodeList,String categoryName,Map<String,String> param){
        List<Map<String,String>> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Map<String,String> info = new ConcurrentHashMap<>();
            String hrefPath = nodeList.item(i).getAttributes().getNamedItem("href").getNodeValue();
            String name = nodeList.item(i).getTextContent();
            info.put(categoryName + "_link", baseUrl + hrefPath);
            info.put(categoryName + "_name",name);
            info.put(categoryName + "_code",hrefPath.replace("/ershoufang/","").replace("/",""));
            if(param != null){
                for (String key:param.keySet()) {
                    info.put(key,param.get(key));
                }
            }
            result.add(info);
        }
        return result;
    }


    public static List<Map<String,String>>  getStreetInfosByAreaInfo(Area areaLink){
        String[] xpathExps = new String[]{xpathExpMap.get(NODE_STREET_A)};
        QName[] qnames = new QName[]{XPathConstants.NODESET};
        List<NodeList> nodeLists =getNodeListByUrlAndXpathExpAndQName(areaLink.getLink(),Arrays.asList(xpathExps),Arrays.asList(qnames));
        Map<String,String> param = new HashMap<>();
        param.put("area_link",areaLink.getLink());
        param.put("area_name",areaLink.getName());
        param.put("area_code",areaLink.getCode());
        return mapAreaOrStreetLinkNodetoListMap(nodeLists.get(0),"street",param);
    }

    public static String getStreetPageInfoUrl(String areaUrl)  {
        Node pathResult = (Node)getPathResultByUrlAndXpathExpAndQName(areaUrl,xpathExpMap.get(PAGE_INFO_ATTR),XPathConstants.NODE);
        boolean strictMode = true;
        if(pathResult!=null){
            // 把 转义的字符 还原
            String unescapedString = org.jsoup.parser.Parser.unescapeEntities(pathResult.getNodeValue(), strictMode);
            return unescapedString;
        }else{
            return "{\"totalPage\":0,\"curPage\":0}";
        }

    }

    public static class WorkThread extends Thread {
        CountDownLatch countDownLatch;
        String url;
        String type;

        public WorkThread(CountDownLatch countDownLatch,  String  url,String type) {
            this.countDownLatch = countDownLatch;
            this.url = url;
            this.type = type;
        }

        @Override
        public void run() {
            System.out.println(getName() +"--------" + url  + ", type=" + type);
            workAction(url, type);
            System.out.println(getName() +"-------- finished");
            countDownLatch.countDown();
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

    public static List<String> getUrlListByNodeList(NodeList nodeList){
        List<String> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            result.add(node.getNodeValue());
        }
        return result;
    }

    public static List<String> getHouseUrl(String streetPageUrl) {
        Object pathResult = getPathResultByUrlAndXpathExpAndQName(streetPageUrl,"//div[@class=\"leftContent\"]/ul/li/a[@data-el=\"ershoufang\"]/@href",XPathConstants.NODESET);
        return getUrlListByNodeList((NodeList) pathResult);
    }

    private static void  workAction(String url, String type) {
        if(type.equals("area")){
            // url是 区链接 找到它下面的所有街道
            Area areaLink = cacheAreaInfos.stream()
                    .filter(item->item.getLink().equals(url))
                    .collect(Collectors.toList())
                    .get(0);
            // 每个区对应多个街道
            List<Street> streetLinks = getStreetInfosByAreaInfo(areaLink).stream().map(item->{
                Area p_area = new Area(
                                        item.get("area_code"),
                                        item.get("area_name"),
                                        item.get("area_link")
                                    );
                Street streetLink = new Street(
                        item.get("street_code"),
                        item.get("street_name"),
                        item.get("street_link"),
                        p_area
                );
                return streetLink;
            }).collect(Collectors.toList());
            cacheStreetInfos.addAll(streetLinks);
//            System.out.println(areaLink.getName() +" 区的街道信息读取完毕" + count);
        }else if(type.equals("street")){
            String pageInfoStr = getStreetPageInfoUrl(url);
            List<String> urls = convertPageToUrls(url,pageInfoStr);
            MyFileUtils.writeLinesToFile(urls,"test.txt",true);
//            System.out.println("街道所有页码链接转换完毕");
        }else if(type.equals("street_page")){
            // 读取某个街道 某页 的所有house
            String street_code = getCode(url);
            System.out.println(street_code);

            List<String> urls = getHouseUrl(url)
                                    .stream()
                                    .map(item->item + "__" + street_code)
                                    .collect(Collectors.toList());
            // 拼接街道 code 方便后续处理
            MyFileUtils.writeLinesToFile(urls,"house_url.txt",true);
        }else if(type.equals("house")){
            // 获取房屋详情 map
            String[] s = url.split("__");
            String link_url = s[0];
            String street_code = s[1];

            System.out.println(link_url);
            Map<String,Object> infos = getHouseDetailByUrl(link_url);
            System.out.println("----------1");
            System.out.println("----------2");
            System.out.println("----------3");
            System.out.println(infos);
            infos.put("street_code",street_code);
            infos.put("link",link_url);
            // 把房屋信息做映射  由中文--》 英文字段
            mapFieldContent(infos);
            // 存入数据库
            String json = MyFileUtils.mapToJsonString(infos);
            List<String> a = new ArrayList<>();
            a.add(json);
            MyFileUtils.writeLinesToFile(a,"house_detail.txt",true);
        }
    }

    public static Map<String,Object> mapFieldContent(Map<String,Object> infos){
        Map<String,String> baseKeyMap = new HashMap<>();
        baseKeyMap.put("房屋户型","house_type");
        baseKeyMap.put("建筑面积","build_area"); // 非公寓为此
        baseKeyMap.put("计租面积","rental_area"); // 公寓为此


        baseKeyMap.put("套内面积","inner_area");
        baseKeyMap.put("房屋朝向","aspect");
        baseKeyMap.put("装修情况","decorated");
        baseKeyMap.put("供暖方式","heating_mode");
        baseKeyMap.put("产权年限","property");
        baseKeyMap.put("用电类型","electricity_type");
        baseKeyMap.put("所在楼层","floor");
        baseKeyMap.put("户型结构","house_struct");
        baseKeyMap.put("建筑类型","build_type");
        baseKeyMap.put("建筑结构","build_struct");
        baseKeyMap.put("梯户比例","ladder_house_ratio");
        baseKeyMap.put("配备电梯","elevator");
        baseKeyMap.put("用水类型","water_type");
        baseKeyMap.put("燃气价格","gas_price");

        Map<String,String> tradeKeyMap = new HashMap<>();
        tradeKeyMap.put("挂牌时间","listing_date");
        tradeKeyMap.put("上次交易","last_trade");
        tradeKeyMap.put("房屋年限","house_years");
        tradeKeyMap.put("抵押信息","mortgage");
        tradeKeyMap.put("交易权属","trade_right");

        tradeKeyMap.put("房屋用途","house_use");
        tradeKeyMap.put("产权所属","property_owner");
        tradeKeyMap.put("房本备件","spare_parts");

        Map<String,String> baseInfo = (Map<String, String>) infos.get("baseInfo");
        Map<String,String> tradeInfo = (Map<String, String>) infos.get("tradeInfo");
        transCNtoColumnName(baseKeyMap,baseInfo);
        transCNtoColumnName(tradeKeyMap,tradeInfo);

        if(baseInfo.get("build_area")!=null && !baseInfo.get("build_area").equals("-")){
            baseInfo.put("build_area",filterNum(baseInfo.get("build_area")));
        }

        if(baseInfo.get("rental_area")!=null && !baseInfo.get("rental_area").equals("-")){
            baseInfo.put("rental_area",filterNum(baseInfo.get("rental_area")));
        }
        return infos;
    }

    public static void transCNtoColumnName(Map<String,String> transMap,Map<String,String> resMap){

        for (String key:transMap.keySet()) {
            String column_name = transMap.get(key);
            String value = "-";
            if(resMap.get(key)!=null){
                value = resMap.get(key);
                resMap.remove(key);
            }
            resMap.put(column_name,value);
        }
    }


    public static Object getNodeOrValue(String html,String xpath,String type) throws XPathExpressionException {
        if(type.equals("node")){
            Node node = (Node)UrlUtils.xPath.evaluate(xpath, UrlUtils.cleanDom(html),XPathConstants.NODE);
            String value = node.getTextContent();
            return value;
        }else if(type.equals("nodelist")){
            NodeList nodeList = (NodeList) UrlUtils.xPath.evaluate(xpath, UrlUtils.cleanDom(html),XPathConstants.NODESET);
            return nodeList;
        }else{
            return null;
        }
    }

    public static Map<String,Object> getHouseDetailByUrl(String url) {
        try {
            Map<String,Object> result = new HashMap<>();
            String html = null;
            html = UrlUtils.read(url).body().html();

            String title = (String) getNodeOrValue(html,xpathExpMap.get(NODE_HOUSE_TITLE),"node");

            String total_price = (String) getNodeOrValue(html,xpathExpMap.get(NODE_HOUSE_TOTAL_PRICE),"node");
            System.out.println(total_price);

            String square_metre_price = (String) getNodeOrValue(html,"/html/body//div[@class=\"content\"]/div[@class=\"price\"]//div[@class=\"unitPrice\"]/span","node");
            // 房屋图片
            NodeList nodeList = (NodeList) getNodeOrValue(html,"//div[@id=\"thumbnail2\"]/ul/li/img/@src","nodelist");
            List<String> previewImages = new ArrayList<>();
            assert nodeList != null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                previewImages.add(node.getNodeValue());
            }

            Map<String,String> baseInfos = new HashMap<>();

            //*[@id="introduction"]/div/div/div[1]/div[2]/ul/li[1]/span
            String baseInfoExp = "//*[@id=\"introduction\"]/div/div/div[1]/div[2]/ul/li/span";
            String baseInfoExp2 = "//*[@id=\"introduction\"]/div/div/div[1]/div[2]/ul/li";
            NodeList infoLabels= (NodeList) getNodeOrValue(html,baseInfoExp,"nodelist");
            NodeList infoValues = (NodeList)getNodeOrValue(html,baseInfoExp2,"nodelist");
            assert infoLabels != null;
            for (int i = 0; i < infoLabels.getLength(); i++) {
                Node node = infoLabels.item(i);
                assert infoValues != null;
                Node node2 = infoValues.item(i);
                String key = node.getTextContent();
                String cacheValue = node2.getTextContent();
                String value = cacheValue.substring(cacheValue.lastIndexOf(key) + key.length() );
                baseInfos.put(key,value);
            }


            Map<String,String> tradeInfos = new HashMap<>();
            String tradeInfoExp = "//*[@id=\"introduction\"]/div/div/div[2]/div[2]/ul/li/span[1]";
            String tradeInfoExp2 = "//*[@id=\"introduction\"]/div/div/div[2]/div[2]/ul/li/span[2]";
            NodeList tradeLabels= (NodeList)getNodeOrValue(html,tradeInfoExp,"nodelist");
            NodeList tradeValues = (NodeList)getNodeOrValue(html,tradeInfoExp2,"nodelist");
            assert tradeLabels != null;
            for (int i = 0; i < tradeLabels.getLength(); i++) {
                Node node = tradeLabels.item(i);
                Node node2 = tradeValues.item(i);
                String key = node.getTextContent();
                String value = node2.getTextContent();
                tradeInfos.put(key,value);
            }

            result.put("title",title);
            result.put("total_price",total_price);
            result.put("square_metre_price",filterNum(square_metre_price));
            result.put("link",url);
            result.put("previewImages",previewImages);
            result.put("baseInfo",baseInfos);
            result.put("tradeInfo",tradeInfos);
            return result;

        } catch (XPathExpressionException e) {
            // 把解析错误的url存入 根据差异格式化 信息
            throw new RuntimeException(e);
        }
    }

    public static String getCode(String url){
        String tmp = url.substring(url.indexOf("/ershoufang/")+"/ershoufang/".length());
        return tmp.substring(0,tmp.indexOf("/"));
    }

    public static void task(List<String> list,String type) {
        try {
//            if(type.equals("house")){
//                list = list.subList(0,3);
//            }else{
//                list = list.subList(0,1);
//            }
            CountDownLatch countDownLatch = new CountDownLatch(list.size());
            for (int i = 0; i < list.size() ; i++) {
                Thread.sleep(new Random().nextInt(3) * delayTime);
                WorkThread thread = new WorkThread(countDownLatch,list.get(i),type);
                thread.start();
            }
            //调用await方法阻塞当前线程，等待子线程完成后在继续执行
            countDownLatch.await();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Street> getStreetInfosByThread() {
        List<Area> areas = getAreaInfos();
        if(areas.size() > 0){
            //区——任务 找到每个区的街道 并缓存
            task(areas.stream()
                    .map(Area::getLink)
                    .collect(Collectors.toList()), "area");
        }
        return cacheStreetInfos;
    }


    public static List<String> getStreetUrlByThread() {
        MyFileUtils.removeFile("test.txt");
        MyFileUtils.removeFile("house_url.txt");

        List<Area> areas = getAreaInfos();
        if(areas.size() > 0){
            //区——任务 找到每个区的街道 并缓存
            task(areas.stream()
                    .map(Area::getLink)
                    .collect(Collectors.toList()), "area");

            // 街道--任务 找到每个街道有多少页码
            task(cacheStreetInfos.stream()
                    .map(Street::getLink)
                    .collect(Collectors.toList()), "street");
            // 读取文件
            List<String> streetPageUrls2 = MyFileUtils.readFile("test.txt");
            task(streetPageUrls2,"street_page");
            List<String> houseUrls2 = MyFileUtils.readFile("house_url.txt");
            task(houseUrls2,"house");
        }
        return null;
    }

    public static String filterNum(String str){
        Pattern compile = Pattern.compile("(\\d+\\.\\d+)|(\\d+)");
        Matcher matcher = compile.matcher(str);
        matcher.find();
        return matcher.group();
    }

    public static void main(String[] args) {

//        step01_writeStreetUrlToFile();
        step02_WriteHouseDetailToFile();
//        test_step02_WriteHouseDetailToFile();
    }

    public static void step01_writeStreetUrlToFile(){
        getStreetUrlByThread();
    }

    public static void step02_WriteHouseDetailToFile(){
        MyFileUtils.removeFile("house_detail.txt");
        List<String> houseUrls2 = MyFileUtils.readFile("house_url.txt");
        task(houseUrls2,"house");
    }

    public static void step03_insesrtHouseDetailToDB(){
        List<String> houseUrls2 = MyFileUtils.readFile("house_detail.txt");
        // 批量插入 以及问题 https://blog.csdn.net/sunyanchun/article/details/89187552

        /*
        1 数据分析 图表 echart /  highcharts
        https://cloud.tencent.com/developer/article/1477265

        位置信息根据街道小区 可视化

        */
    }

    public static void test_step02_WriteHouseDetailToFile(){
        MyFileUtils.removeFile("house_detail.txt");
        List<String> aa = new ArrayList<>();
        aa.add("https://tj.lianjia.com/ershoufang/101106268680.html__quanyechang");
        task(aa,"house");
        // 一些报错 node不存在有情况是 一种房源下架了
    }

}
