package dao;

import com.util.UrlReader;
import entity.Area;
import entity.House;
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

    public void insertHouse(Map<String,Object> param){
//        private int id;
//        private String title;
//        private String link;
//        private Double total_price;
//        private Double square_metre_price;
//        private HouseDetail houseDetail;
        House h = new House();

//        spliceInsertBatch("MyMapper.insertHouse",);
    }

    public void clearStreets(){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.update("MyMapper.clearStreets");
        }
    }
}
