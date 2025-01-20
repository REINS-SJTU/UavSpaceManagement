package com.zhd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhd.entity.UavSpaceOcp;
import com.zhd.entity.tmp.ConflictResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Deprecated
@Mapper
@Repository
public interface UavSpaceOcpMapper extends BaseMapper<UavSpaceOcp> {
//    @Select("SELECT s1.x AS x,s1.y AS y,s1.z AS z,s1.k AS k,STRING_AGG(CAST(s2.uav_id AS TEXT)) AS uavIds " +
//            "FROM u_space_ocp AS s1 JOIN u_space_ocp AS s2 on s1.uav_id!=s2.uav_id " +
//            "AND (s1.x=(s1.x&s2.x) AND s1.y=(s1.y&s2.y) AND s1.z=(s1.z&s2.z))  " +
//            "GROUP BY s1.x,s1.y,s1.z,s1.k ")
//    List<ConflictResult> calculateConflict();
    @Select("WITH tmp as (SELECT s1.x AS x,s1.y AS y,s1.z AS z,s1.k AS k,STRING_AGG(CAST(s2.uav_id AS TEXT)) AS uav_ids " +
            "FROM u_space_ocp AS s1 JOIN u_space_ocp AS s2 on s1.uav_id!=s2.uav_id " +
            "AND (s1.x=(s1.x&s2.x) AND s1.y=(s1.y&s2.y) AND s1.z=(s1.z&s2.z))  " +
            "GROUP BY s1.x,s1.y,s1.z,s1.k) " +
            "SELECT uav_id")
    List<ConflictResult> calculateConflict();
}
