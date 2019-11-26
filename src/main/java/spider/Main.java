package spider;

import com.util.SqlSessionUtil;
import com.util.UrlReader;
import dao.AreaStreetDao;
import entity.Street;
import service.AreaStreetService;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args){
//        crawlData();
        initData();
    }

    private static void crawlData() {
        UrlReader.parseListSizeConfig.put("area", Arrays.asList(0,6));
        UrlReader.step01_writeStreetUrlToFile();
        UrlReader.step02_WriteHouseDetailToFile();
    }

    public static void initData(){
        AreaStreetDao areaDao = new AreaStreetDao(SqlSessionUtil.getSessionFactory());
        AreaStreetService areaStreetService = new AreaStreetService(areaDao);
        // 录入 arae数据
        areaStreetService.initAreaData();
        System.out.println(areaStreetService.getAreaList());

        // 录入street数据
        areaStreetService.initStreetData();
        List<Street> streets = areaStreetService.getStreetList();
        System.out.println(streets);

        // 录入 house / houseDetail / houseImage 数据
        areaStreetService.insertBatchHouse();
    }
}
