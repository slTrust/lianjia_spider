package dao;

import com.util.UrlReader;
import mapper.AreaMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AreaDao {

    private final SqlSessionFactory sqlSessionFactory;

    public AreaDao(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void getAreas(){
        try (SqlSession session = sqlSessionFactory.openSession()) {
            AreaMapper mapper = session.getMapper(AreaMapper.class);
            System.out.println(mapper.getAreas());
        }
    }

    public  void batchInsertAreas(){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {

            Map<String, Object> param = new HashMap<>();
            List<Map<String,String>> res = UrlReader.getAreaUrls().stream().map(item->{
                item = item.replace("https://tj.lianjia.com/ershoufang/","");
                item = item.replace("/","");
                Map<String,String> area = new HashMap<>();
                area.put("name",item);
                area.put("code",item);
                return area;
            }).collect(Collectors.toList());
            param.put("areas", res);
            session.insert("MyMapper.batchInsertAreas", param);
        }
    }

    public void clearAreas(){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.update("MyMapper.clearAreas");
        }
    }
}
