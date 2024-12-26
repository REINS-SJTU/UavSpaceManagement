package com.zhd.entity.tmp;

import lombok.Data;

@Data
public class UavPosShape {
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
    private String shapePoints;
}
