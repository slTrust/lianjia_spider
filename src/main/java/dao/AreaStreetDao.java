package dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.util.CommonUtils;
import com.util.MyFileUtils;
import com.util.UrlReader;
import entity.*;
import mapper.AreaMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class AreaStreetDao {

    private final SqlSessionFactory sqlSessionFactory;

    public AreaStreetDao(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public  List<Area> getAreas(){
        try (SqlSession session = sqlSessionFactory.openSession()) {
            AreaMapper mapper = session.getMapper(AreaMapper.class);
            return mapper.getAreas();
        }
    }

    public void batchInsertAreas(){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Map<String, Object> param = new HashMap<>();
            List<Area> res = UrlReader.getAreaInfos();
            param.put("areas", res);
            session.insert("MyMapper.batchInsertAreas", param);
        }
    }

    public void clearAreas(){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.update("MyMapper.clearAreas");
        }
    }

    public List<Street> getStreet(){
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("MyMapper.selectStreets");
        }
    }


    private <T> List<List<T>> spliceArrays(List<T> datas, int splitSize) {
        if (datas == null || splitSize < 1) {
            return  null;
        }
        int totalSize = datas.size();
        int count = (totalSize % splitSize == 0) ?
                (totalSize / splitSize) : (totalSize/splitSize+1);
        List<List<T>> rows = new ArrayList<>();

        for (int i = 0; i < count;i++) {

            List<T> cols = datas.subList(i * splitSize,
                    (i == count - 1) ? totalSize : splitSize * (i + 1));
            rows.add(cols);
        }
        return rows;
    }

    public void batchInsertStreet(){
        Map<String,Integer> codeMapId = new HashMap<>();
        getAreas().forEach(item->codeMapId.put(item.getCode(),item.getId()));
        List<Street> res = UrlReader
                .getStreetInfosByThread()
                .stream()
                .map(item->{
                    Area area = item.getArea();
                    Integer id = codeMapId.get(area.getCode());
                    area.setId(id);
                    return item;
                })
                .collect(Collectors.toList());
        List<List<Street>> res2 = spliceArrays(res,20);
        for (int i = 0; i <res2.size() ; i++) {
            System.out.println(res2.get(i));
            Map<String, Object> param = new HashMap<>();
            param.put("streets", res2.get(i));
            spliceInsertBatch("MyMapper.batchInsertStreets",param);
        }

    }

    public void spliceInsertBatch(String stmt,Map param){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert(stmt, param);
        }
    }

    public void insertHouse(House house){

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            // 根据 street_code 拿到街道id
            int id = house.getStreet().getId();

            // 录入 house数据
            String json1 = JSON.toJSONString(house);
            Map<String,Object> param1 = JSONObject.parseObject(json1);
            param1.put("street_id",house.getStreet().getId());
            session.insert("MyMapper.insertHouse", param1);
            session.commit();
            // 查询当前录入数据的 house id
            String link = house.getLink();
            // 遗留问题 url不是唯一的房屋信息 同一网址存在挂牌多次的情况，这里不处理了，因为这样的数据很少
            int house_id  = session.selectOne("MyMapper.selectHouseByLink",link);
            // 录入 房屋详情
            house.setId(house_id);
            String json = JSON.toJSONString(house.getHouseDetail());
            Map<String,Object> param = JSONObject.parseObject(json);
            param.put("house_id",house_id);

            session.insert("MyMapper.insertHouseDetail",param);

            Map<String,List<Map<String,String>>> param2 = new HashMap<>();
            List<HouseImage> images = house.getHouseImages();
            param2.put("images",images.stream().map(image->{
                Map<String,String> imgMap = new HashMap();
                imgMap.put("house_id",house_id + "");
                imgMap.put("link",image.getLink());
                return imgMap;
            }).collect(Collectors.toList()));
            if (images.size()!=0){
                session.insert("MyMapper.batchInsertHouseImages",param2);
            }
        }
    }

    public void clearStreets(){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.update("MyMapper.clearStreets");
        }
    }

    public void readBigFileInsertBatch(String fileName,List<Street> streets) {
        try {
            File file = new File(MyFileUtils.projectDir,"target/"+fileName);
            long count = 0;
            List<String> result = new ArrayList<>();
            LineIterator lineIterator = FileUtils.lineIterator(file);
            while (lineIterator.hasNext()) {
                result.add(lineIterator.next());
                if(count %100 == 0 && count != 0){
                    setListHouseByListJSONString(result,streets).forEach(this::insertHouse);
                    result = new ArrayList<>();
                }
                count ++;
            }
            setListHouseByListJSONString(result,streets).forEach(this::insertHouse);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<House> setListHouseByListJSONString(List<String> list,List<Street> streets){
        return list.stream().map(item->{
            Map<String,Object> map = MyFileUtils.JsonToMap(item);
            House house = new House();
            house.setTitle(CommonUtils.filterString((String) map.get("title")));
            house.setLink((String) map.get("link"));
            house.setNeighbourhoods((String)map.get("neighbourhoods"));
            house.setTotal_price(FormatStringToDouble(map,"total_price"));
            house.setSquare_metre_price(FormatStringToDouble(map,"square_metre_price"));

            // 先找到 streetId
            Street street = new Street();
            street.setCode((String) map.get("street_code"));
            long streetId = streets.stream()
                    .filter(item2->item2.getCode().equals((String) map.get("street_code")))
                    .collect(Collectors.toList()).get(0).getId();
            street.setId((int) streetId);

            house.setStreet(street);

            Map<String,Object> mapHouseDetail = MyFileUtils.mergeMap((Map<String,Object>)map.get("baseInfo"),(Map<String,Object>)map.get("tradeInfo"));
            mapHouseDetail.put("build_area",FormatStringToDouble(mapHouseDetail,"build_area"));
            mapHouseDetail.put("inner_area",FormatStringToDouble(mapHouseDetail,"inner_area"));
            mapHouseDetail.put("gas_price",FormatStringToDouble(mapHouseDetail,"gas_price"));
            mapHouseDetail.put("last_trade", CommonUtils.formatDate((String) mapHouseDetail.get("last_trade")));
            mapHouseDetail.put("listing_date",CommonUtils.formatDate((String) mapHouseDetail.get("listing_date")));
            HouseDetail houseDetail = getHouseDetailInstanceByMapData(mapHouseDetail);

            house.setHouseDetail(houseDetail);

            // images
            List<HouseImage> images;
            List<String> imageLinks = (List<String>) map.get("previewImages");
            images = imageLinks.stream().map(img->{
                HouseImage imgObj = new HouseImage();
                imgObj.setLink(img);
                return imgObj;
            }).collect(Collectors.toList());
            house.setHouseImages(images);

            return house;
        }).collect(Collectors.toList());
    }

    private  static Double  FormatStringToDouble(Map map,String key){
        String value = (String) map.get(key);
        if((value == null) ||"-".equals(value) || "暂无数据".equals(value)){
            value = "-1";
        }else if(value.contains("㎡")){
            int index = value.indexOf("㎡");
            value = value.substring(0,index);
        }else if(value.contains("元/m")){
            int index = value.indexOf("元/m");
            value = value.substring(0,index);
        }
        return Double.parseDouble(value);
    }

    private static HouseDetail getHouseDetailInstanceByMapData(Map<String, Object> mapHouseDetail) {
        HouseDetail houseDetail = new HouseDetail();
        try {
            BeanUtils.populate(houseDetail,mapHouseDetail);
            return houseDetail;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
