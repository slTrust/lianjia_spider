package mapper;

import entity.Street;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StreetMapper {
    @Select("select * from street")
    List<Street> getStreets();
}
