package com.zhd.entity.tmp;

import com.zhd.geometry.structure.OctreeGrid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DivisionPlan2 {
    private String uid;
    private Double[] largeArea;
    private List<OctreeGrid> exclude;

    public static Double[] zone2LargeArea(Zone zone){
        double[] x = zone.getX();
        double[] y = zone.getY();
        double[] z = zone.getZ();
        return new Double[]{
                Math.min(x[0],x[1]),
                Math.min(y[0],y[1]),
                Math.min(z[0],z[1]),
                Math.max(x[0],x[1]),
                Math.max(y[0],y[1]),
                Math.max(z[0],z[1])
        };
    }
}
