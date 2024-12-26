package com.zhd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="u_info",autoResultMap = true)
public class UavInfo {
    @TableId(type= IdType.AUTO)
    private Long uavId;
    private Double a; // 最大加速度
    private Integer priority; // 优先级
}
