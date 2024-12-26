package com.zhd.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.locationtech.jts.geom.MultiPoint;

@Data
@TableName(value="u_shape",autoResultMap = true)
public class UavShape {
    @TableId(type=IdType.AUTO)
    private Long uavId;
    private String shapePoints;
}
