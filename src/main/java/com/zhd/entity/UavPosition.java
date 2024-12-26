package com.zhd.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName(value="u_position",autoResultMap = true)
public class UavPosition {
    private Long uavId;
    private Double x;
    private Double y;
    private Double z;
    private Double vx;
    private Double vy;
    private Double vz;
    private Double hvx;
    private Double hvy;
    private Double hvz;
    private Long ts;
}