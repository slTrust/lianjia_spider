package spider;

import com.util.UrlReader;
import dao.AreaMapper;
import entity.Area;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        String resource = "db/myBatis/config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            AreaMapper mapper = session.getMapper(AreaMapper.class);
            System.out.println(mapper.getAreas());
        }

//        UrlReader.getStreetUrlByThread();
    }
}
