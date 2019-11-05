package dao;

import entity.Area;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AreaMapper {
    @Select("select * from area")
    List<Area> getAreas();

}

