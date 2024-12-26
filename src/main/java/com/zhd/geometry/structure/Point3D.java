package com.zhd.geometry.structure;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point3D {
    private double x;
    private double y;
    private double z;

    public static boolean smallerOrEqual(Point3D p1,Point3D p2){
        return p1.getX()<=p2.getX()
                && p1.getY()<=p2.getY()
                && p1.getZ()<=p2.getZ();
    }
}
