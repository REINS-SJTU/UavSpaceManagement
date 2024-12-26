package com.zhd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhd.entity.UavPosition;
import com.zhd.entity.tmp.UavPosShape;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Mapper
@Repository
public interface UavPositionMapper extends BaseMapper<UavPosition> {
    @Update("INSERT INTO u_position VALUES(#{uavId}," +
            "#{px},#{py},#{pz}," +
            "#{vx},#{vy},#{vz},"+
            "#{hvx},#{hvy},#{hvz}," +
            "#{ts})")
    void insertPosition(Long uavId,
                        Double px, Double py, Double pz,
                        Double vx, Double vy, Double vz,
                        Double hvx, Double hvy, Double hvz,
                        Long ts);

    @Select("SELECT * FROM (SELECT uav_id,MAX(ts) ts FROM u_position GROUP BY uav_id) p " +
            "NATURAL JOIN u_position LEFT JOIN u_shape s ON p.uav_id=s.uav_id WHERE ts>#{ts}")
    List<UavPosShape> getUavPosShapeBeforeWindow(Long ts);
}
