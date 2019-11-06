package service;

import com.util.SqlSessionUtil;
import dao.AreaDao;

public class AreaService {
    public static void initAreaData(){
        AreaDao areaDao = new AreaDao(SqlSessionUtil.getSessionFactory());
        areaDao.clearAreas();
        System.out.println("先清空表");
        areaDao.getAreas();
        System.out.println("录入数据");
        areaDao.batchInsertAreas();
        areaDao.getAreas();
    }
}
