package com.zhd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhd.entity.UavPosition;
import com.zhd.entity.tmp.ObservationSelf;
import com.zhd.entity.tmp.ObservationVehicle;
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
    @Insert({
            "<script>",
            "INSERT INTO u_position (uav_id, px, py, pz, vx, vy, vz, theta, phi, ts) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.uavId}, #{item.px}, #{item.py}, #{item.pz}, #{item.vx}, #{item.vy}, #{item.vz}, #{item.theta}, #{item.phi}, #{item.ts})",
            "</foreach>",
            "</script>"
    })
    void batchInsert(List<UavPosition> uavPositions);

    @Select("SELECT * FROM (SELECT uav_id,MAX(ts) ts FROM u_position GROUP BY uav_id) p " +
            "NATURAL JOIN u_position LEFT JOIN u_shape s ON p.uav_id=s.uav_id WHERE ts>#{ts}")
    List<UavPosShape> getUavPosShapeBeforeWindow(Long ts);

    @Select("SELECT po.px AS dx,po.py AS dy,po.pz AS dz, " +
            "   po.vx AS vx,po.vy AS vy,po.vz AS vz, " +
            "   SQRT(po.vx*po.vx+po.vy*po.vy+po.vz*po.vz) AS v, i.priority AS priority" +
            " FROM (SELECT uav_id,MAX(ts) ts FROM u_position GROUP BY uav_id) p " +
            "NATURAL JOIN u_position po LEFT JOIN u_info i ON p.uav_id=i.uav_id WHERE ts>#{ts} AND p.uav_id=#{id} limit 1")
    ObservationSelf getObservationSelf(Long ts,String id);


    @Select(" SELECT po.px-#{dx} AS dx,po.py-#{dy} AS dy,po.pz-#{dz} AS dz," +
            "  po.vx AS vx,po.vy AS vy,po.vz AS vz," +
            "  SQRT(po.vx*po.vx+po.vy*po.vy+po.vz*po.vz) AS v," +
            "  i.priority AS priority" +
            " FROM (SELECT uav_id,MAX(ts) ts FROM u_position GROUP BY uav_id) p " +
            " NATURAL JOIN u_position po " +
            " LEFT JOIN u_info i on p.uav_id=i.uav_id " +
            " WHERE ts>#{ts} AND p.uav_id = ANY(string_to_array(#{ids},',')::varchar[])" +
            " ORDER BY (po.vx-#{dx})*(po.vx-#{dx})+(po.vy-#{dy})*(po.vy-#{dy})+(po.vz-#{dz})*(po.vz-#{dz}) ASC" +
            " LIMIT #{limit};")
    List<ObservationVehicle> getObservationVehicles(String ids,Long ts,int limit,double dx,double dy,double dz);
}
