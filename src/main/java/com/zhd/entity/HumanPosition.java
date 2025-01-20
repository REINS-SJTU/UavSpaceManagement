package com.zhd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="h_position",autoResultMap = true)
public class HumanPosition {
    private String humanId;
    private Double px;
    private Double py;
    private Double pz;
    private Double vx;
    private Double vy;
    private Double vz;
    private Long ts;
}
