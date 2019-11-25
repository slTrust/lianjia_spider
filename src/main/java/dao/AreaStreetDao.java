package dao;

import com.util.MyFileUtils;
import com.util.UrlReader;
import entity.Area;
import entity.House;
import entity.HouseDetail;
import entity.Street;
import mapper.AreaMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public void insertHouse(Map param){

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            // 根据 street_code 拿到街道id
            String code = (String) param.get("street_code");
            int id = session.selectOne("MyMapper.selectStreetIdByCode", code);

            param.put("street_id",id);
            // 录入 house数据
            System.out.println(param);
            session.insert("MyMapper.insertHouse", param);
            session.commit();
            // 查询当前录入数据的 house id
            String link = (String) param.get("link");
            int house_id = session.selectOne("MyMapper.selectHouseByLink",link);
            // 录入 房屋详情
            param.put("house_id",house_id);
            System.out.println("insert insert");
            System.out.println(xxx(param));
            System.out.println("insert insert");
            session.insert("MyMapper.insertHouseDetail", xxx(param));
        }
    }

    public Map<String, Object> xxx(Map<String,Object> map){
        Map<String,Object> tmp = new HashMap<>();
        Map<String,String> baseInfo = (Map<String, String>) map.get("baseInfo");
        Map<String,String> tradeInfo = (Map<String, String>) map.get("tradeInfo");

        for (String key:map.keySet() ) {
            tmp.put(key,map.get(key));
        }
        for (String key:baseInfo.keySet() ) {
            tmp.put(key,baseInfo.get(key));
        }
        for (String key:tradeInfo.keySet() ) {
            tmp.put(key,tradeInfo.get(key));
        }
        tmp.remove("baseInfo");
        tmp.remove("tradeInfo");
        return tmp;
    }

    public void clearStreets(){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.update("MyMapper.clearStreets");
        }
    }

    public void readBigFileInsertBatch(String fileName) {
        try {
            File file = new File(MyFileUtils.projectDir,"target/"+fileName);
            long count = 0;
            List<String> result = new ArrayList<>();
            LineIterator lineIterator = FileUtils.lineIterator(file);
            while (lineIterator.hasNext()) {
                result.add(lineIterator.next());
                if(count %100 == 0 && count != 0){
                    xxx(result);
                    result = new ArrayList<>();
                }
                count ++;
            }
            xxx(result);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void xxx(List<String> list){
        list.stream().forEach(item->{
            Map<String,Object> map = MyFileUtils.JsonToMap(item);
            // previewImages
            System.out.println(map.get("title"));
            System.out.println(map.get("link"));
            System.out.println(map.get("street_code"));
            System.out.println(map.get("previewImages"));

            House house = new House();
            house.setTitle((String) map.get("title"));
            house.setLink((String) map.get("link"));
            house.setTotal_price(FormatStringToDouble(map,"total_price"));
            house.setSquare_metre_price(FormatStringToDouble(map,"square_metre_price"));

            System.out.println(map.get("tradeInfo"));
            Map<String,Object> mapHouseDetail = MyFileUtils.mergeMap((Map<String,Object>)map.get("baseInfo"),(Map<String,Object>)map.get("tradeInfo"));
            System.out.println(mapHouseDetail);
            mapHouseDetail.put("build_area",FormatStringToDouble(mapHouseDetail,"build_area"));
            mapHouseDetail.put("inner_area",FormatStringToDouble(mapHouseDetail,"inner_area"));
            mapHouseDetail.put("gas_price",FormatStringToDouble(mapHouseDetail,"gas_price"));
            mapHouseDetail.put("last_trade",formatDate((String) mapHouseDetail.get("last_trade")));
            mapHouseDetail.put("listing_date",formatDate((String) mapHouseDetail.get("listing_date")));
            HouseDetail houseDetail = getHouseDetailInstanceByMapData(mapHouseDetail);

            System.out.println(houseDetail);

            house.setHouseDetail(houseDetail);

            Street street = new Street();
            street.setCode((String) map.get("street_code"));
            street.setId(getStreet()
                            .stream()
                            .filter(item2->item2.getCode().equals((String) map.get("street_code")))
                            .collect(Collectors.toList()).get(0).getId());
            house.setStreet(street);

        });
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

    public static Date formatDate(String strDate){
        if((strDate == null) ||"-".equals(strDate) || "暂无数据".equals(strDate)){
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(strDate);
            return date;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
