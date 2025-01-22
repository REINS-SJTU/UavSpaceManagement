package com.zhd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhd.entity.HumanPosition;
import com.zhd.entity.tmp.ObservationHuman;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface HumanPositionMapper extends BaseMapper<HumanPosition> {
    @Select(" SELECT px-#{dx} AS dx, py-#{dy} AS dy, pz-#{dz} AS dz ,0 AS fear" +
            " FROM h_position " +
            " WHERE ts>#{ts}" +
            " ORDER BY (px-#{dx})*(px-#{dx})+(py-#{dy})*(py-#{dy})+(pz-#{dz})*(pz-#{dz}) ASC " +
            " LIMIT #{limit}")
    List<ObservationHuman> getObservationHumans(Long ts,int limit,double dx,double dy,double dz);
}
