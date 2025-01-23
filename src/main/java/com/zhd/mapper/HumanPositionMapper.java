package com.zhd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhd.entity.HumanPosition;
import com.zhd.entity.UavPosition;
import com.zhd.entity.tmp.ObservationHuman;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface HumanPositionMapper extends BaseMapper<HumanPosition> {
    @Insert({
            "<script>",
            "INSERT INTO h_position (human_id, px, py, pz, vx, vy, vz, ts) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.humanId}, #{item.px}, #{item.py}, #{item.pz}, #{item.vx}, #{item.vy}, #{item.vz}, #{item.ts})",
            "</foreach>",
            "</script>"
    })
    void batchInsert(List<HumanPosition> humanPositions);
    @Select(" SELECT px-#{dx} AS dx, py-#{dy} AS dy, pz-#{dz} AS dz ,0 AS fear" +
            " FROM h_position " +
            " WHERE ts>#{ts}" +
            " ORDER BY (px-#{dx})*(px-#{dx})+(py-#{dy})*(py-#{dy})+(pz-#{dz})*(pz-#{dz}) ASC " +
            " LIMIT #{limit}")
    List<ObservationHuman> getObservationHumans(Long ts,int limit,double dx,double dy,double dz);


    @Select("SELECT human_id, px, py, pz, vx, vy, vz, ts " +
            "FROM h_position " +
            "WHERE ts > #{ts} AND (human_id, ts) IN (" +
            "    SELECT human_id, MAX(ts) " +
            "    FROM h_position " +
            "    WHERE ts > #{ts} " +
            "    GROUP BY human_id" +
            ")")
    List<HumanPosition> getLatestHumanPositions(Long ts);
}
