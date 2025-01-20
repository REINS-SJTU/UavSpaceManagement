package com.zhd.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Deprecated
@Data
@AllArgsConstructor
@TableName(value="u_space_ocp",autoResultMap = true)
public class UavSpaceOcp {
    private Long uavId;
    private Integer x;
    private Integer y;
    private Integer z;
    private Integer k;
}
