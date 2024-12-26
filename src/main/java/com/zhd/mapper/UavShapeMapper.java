package com.zhd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhd.entity.UavShape;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UavShapeMapper extends BaseMapper<UavShape> {
    @Update("INSERT INTO u_shape(shape_points) VALUES (CONCAT(" +
            " CONCAT(#{xl},' ',#{yl},' ',#{zl},',')," +
            " CONCAT(#{xu},' ',#{yl},' ',#{zl},',')," +
            " CONCAT(#{xl},' ',#{yu},' ',#{zl},',')," +
            " CONCAT(#{xl},' ',#{yl},' ',#{zu},',')," +
            " CONCAT(#{xu},' ',#{yl},' ',#{zu},',')," +
            " CONCAT(#{xu},' ',#{yu},' ',#{zl},',')," +
            " CONCAT(#{xl},' ',#{yu},' ',#{zu},',')," +
            " CONCAT(#{xu},' ',#{yu},' ',#{zu},'')," +
            "')'))")
    void insertByBounders(Double xu,Double xl,
                          Double yu,Double yl,
                          Double zu,Double zl);
}
