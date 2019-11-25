package service;

import dao.AreaStreetDao;
import entity.Area;
import entity.Street;

import java.util.List;

public class AreaStreetService {
    private AreaStreetDao areaStreetDao;

    public AreaStreetService(AreaStreetDao areaStreetDao) {
        this.areaStreetDao = areaStreetDao;
    }

    public void initAreaData(){
        areaStreetDao.getAreas();
        if(areaStreetDao.getAreas().size()!=0){
            System.out.println("数据已经存在");
        }else{
            System.out.println("录入数据");
            areaStreetDao.batchInsertAreas();
        }
    }

    public List<Area> getAreaList(){
        return areaStreetDao.getAreas();
    }

    public List<Street> getStreetList(){
        return areaStreetDao.getStreet();
    }

    public void initStreetData(){
        if(areaStreetDao.getStreet().size()!=0){
            System.out.println("数据已经存在");
        }else{
            System.out.println("录入数据");
            areaStreetDao.batchInsertStreet();
        }
    }

    public void insertBatchHouse(){
        areaStreetDao.readBigFileInsertBatch("house_detail.txt");
    }
}
