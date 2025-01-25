package com.zhd.geometry.algorithm;

import com.zhd.entity.tmp.DivisionPlan2;
import com.zhd.entity.tmp.ObservationHuman;
import com.zhd.entity.tmp.ObservationSelf;
import com.zhd.entity.tmp.Zone;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.geometry.structure.Point3D;

import java.util.List;
import java.util.Map;

public class MetricsCalculator {
    public static double fear(ObservationSelf vehicle, ObservationHuman human){
        Point3D v=new Point3D(vehicle.getDx(),vehicle.getDy(),vehicle.getDz());
        Point3D h=new Point3D(human.getDx(),human.getDy(),human.getDz());
        Point3D relation = GeoUtil.pointMinus(v, h);
        double theta=Math.abs(GeoUtil.getTheta(v)-GeoUtil.getTheta(h));
        double phi=Math.abs(GeoUtil.getPhi(v)-GeoUtil.getPhi(h));
        double c = Math.PI/180;
        if(theta>Math.PI) theta=2*Math.PI-theta;
        boolean  visible=(phi<60*c) && (theta<80*c);
        double distance = GeoUtil.getLength(relation);
        boolean horizontal= Math.abs(vehicle.getVz())<=2;  // 0.2m/s ->2dm/s
        double speed = GeoUtil.getLength(new Point3D(
                vehicle.getVx(),vehicle.getVy(),vehicle.getVz()
        ));
        // 8m -> 80dm , 3m -> 30dm , 0.5m/s ->5dm/s
        return 0.25*(
                ((distance<80 && !horizontal)? 1:0) +
                        ((distance<80 && ! visible)?1:0) +
                        ((distance<30 && speed>5)?1:0) +
                        (distance<30?1:0)
        );
    }

    public static double occupancy(Map<String, DivisionPlan2> scheme,int M){
        double sum=0.0;
        int count=0;
        for(String uid:scheme.keySet()){
            DivisionPlan2 dp = scheme.get(uid);
            sum+= volume(dp,M);
            count++;
        }
        if (count==0) return  0.0;
        return sum/count/(1L<<(3*M));
    }

    public static double avgMaxReachableRate(Map<String, DivisionPlan2> scheme,Map<String,Point3D[]> id2Box,int M,double v, double t){
        double sum =0.0;
        int count=0;
        Zone maxZone=new Zone(
                new double[]{v*t,v*t},
                new double[]{v*t,v*t},
                new double[]{v*t,v*t}
        ) ;
        for(String uid:scheme.keySet()){
            DivisionPlan2 dp = scheme.get(uid);
            Zone zone = GeoUtil.recoverZone(id2Box.get(uid), maxZone, 1.0);
            double reachable= (zone.getX()[1]-zone.getX()[0])*(zone.getY()[1]-zone.getY()[0])*(zone.getZ()[1]-zone.getZ()[0]);
            sum+= volume(dp,M)/reachable;
            count++;
        }
        return count==0? 0.0: (sum/count);
    }

    public static double volume(DivisionPlan2 dp,int M){
        if(dp==null) return 0.0;
        Double[] largeArea = dp.getLargeArea();
        if(largeArea==null||largeArea.length!=6) return 0.0;
        double v=1.0;
        for(int i=0;i<3;i++){
            double a=largeArea[i+3]-largeArea[i];
            if(a<=0.000001) return 0.0; // 边长为0
            v*=a;
        }
        List<OctreeGrid> grids = dp.getExclude();
        for(OctreeGrid grid:grids){
            v-=volume(grid,M);
        }
        return v;
    }

    public static double volume(OctreeGrid grid,int M){
        double a = 1.0*(1<<(M-grid.getK()));
        return a*a*a;
    }
}
