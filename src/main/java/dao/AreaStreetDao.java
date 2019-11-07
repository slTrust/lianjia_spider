package dao;

import com.util.UrlReader;
import entity.Area;
import entity.Street;
import mapper.AreaMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
}
