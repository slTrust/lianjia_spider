package spider;

import com.util.SqlSessionUtil;
import dao.AreaStreetDao;
import entity.Street;
import service.AreaStreetService;

import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args){
        AreaStreetDao areaDao = new AreaStreetDao(SqlSessionUtil.getSessionFactory());
        AreaStreetService areaStreetService = new AreaStreetService(areaDao);
        // 录入 arae数据
        areaStreetService.initAreaData();
        System.out.println(areaStreetService.getAreaList());

        // 录入street数据
        areaStreetService.initStreetData();
        List<Street> streets = areaStreetService.getStreetList();
        System.out.println(streets);
    }
}
