package com.zhd.entity;



import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="u_shape",autoResultMap = true)
public class UavShape {
    private String uavId;
    private Double xu;
    private Double xl;
    private Double yu;
    private Double yl;
    private Double zu;
    private Double zl;
}
