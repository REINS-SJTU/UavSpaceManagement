package com.zhd.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value="u_info",autoResultMap = true)
public class UavInfo {
    private String uavId;
    private Double a; // 最大加速度
    private Integer priority; // 优先级
}
