package com.zhd.entity.tmp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObservationVehicle {
    private double dx;
    private double dy;
    private double dz;
    private double vx;
    private double vy;
    private double vz;
    private double v;
    private double priority;
}
