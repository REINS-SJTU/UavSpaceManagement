package com.zhd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhd.entity.UavShape;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UavShapeMapper extends BaseMapper<UavShape> {
}
