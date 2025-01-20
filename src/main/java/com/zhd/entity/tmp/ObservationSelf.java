package com.zhd.entity.tmp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObservationSelf {
    private double dx;
    private double dy;
    private double dz;
    private double vx;
    private double vy;
    private double vz;
    private double v;
    private Integer priority;
}
