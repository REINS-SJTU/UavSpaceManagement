package com.zhd.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName(value="u_position",autoResultMap = true)
public class UavPosition {
    private String uavId;
    private Double px;
    private Double py;
    private Double pz;
    private Double vx;
    private Double vy;
    private Double vz;
    private Double theta;
    private Double phi;
    private Long ts;
}