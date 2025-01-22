package com.zhd.geometry.algorithm;

import com.zhd.entity.UavPosition;
import com.zhd.entity.tmp.ObservationHuman;
import com.zhd.entity.tmp.ObservationSelf;
import com.zhd.entity.tmp.UavPosShape;
import com.zhd.entity.tmp.Zone;
import com.zhd.geometry.structure.Point3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GeoUtil {



    public static double calculateFear(ObservationSelf vehicle, ObservationHuman human){
        Point3D v=new Point3D(vehicle.getDx(),vehicle.getDy(),vehicle.getDz());
        Point3D h=new Point3D(human.getDx(),human.getDy(),human.getDz());
        Point3D relation = pointMinus(v, h);
        double theta=Math.abs(getTheta(v)-getTheta(h));
        double phi=Math.abs(getPhi(v)-getPhi(h));
        double c = Math.PI/180;
        if(theta>Math.PI) theta=2*Math.PI-theta;
        boolean  visible=(phi<60*c) && (theta<80*c);
        double distance = getLength(relation);
        boolean horizontal= Math.abs(vehicle.getVz())<=0.2;
        double speed = getLength(new Point3D(
                vehicle.getVx(),vehicle.getVy(),vehicle.getVz()
        ));
        return 0.25*(
                ((distance<8 && !horizontal)? 1:0) +
                ((distance<8 && ! visible)?1:0) +
                ((distance<3 && speed>0.5)?1:0) +
                (distance<3?1:0)
        );
    }

    // 根据uav的pos和shape恢复到三维空间中的点，返回bounding box
    public static Point3D[] recover(UavPosShape uavPosShape){
        double [] xx = new double[]{uavPosShape.getXl(),uavPosShape.getXu()};
        double [] yy = new double[]{uavPosShape.getYl(),uavPosShape.getYu()};
        double [] zz = new double[]{uavPosShape.getZl(),uavPosShape.getZu()};
        Point3D pMax = null;
        Point3D pMin = null;
        for(int i=0;i<2;i++){
            for(int j=0;j<2;j++){
                for(int k=0;k<2;k++){
                    Point3D p = new Point3D(xx[i], yy[j], zz[k]);
                    double theta = getTheta(p)+uavPosShape.getTheta();
                    double phi = getPhi(p)+uavPosShape.getPhi();
                    double r = getLength(p);
                    double x = r*Math.sin(theta)*Math.cos(phi)+uavPosShape.getPx();
                    double y = r*Math.sin(theta)*Math.sin(phi)+uavPosShape.getPy();
                    double z = r*Math.cos(theta)+uavPosShape.getPz();
                    if(pMax==null) {
                        pMax = new Point3D(x,y,z);
                    }else {
                        if(x>pMax.getX()) pMax.setX(x);
                        if(y>pMax.getY()) pMax.setY(y);
                        if(z>pMax.getZ()) pMax.setZ(z);
                    }
                    if(pMin==null){
                        pMin = new Point3D(x,y,z);
                    }else{
                        if(x< pMin.getX()) pMin.setX(x);
                        if(y< pMin.getY()) pMin.setY(y);
                        if(z< pMin.getZ()) pMin.setZ(z);
                    }
                }
            }
        }

        return new Point3D[]{new Point3D(uavPosShape.getPx(),uavPosShape.getPy(),uavPosShape.getPz()),pMin,pMax};
    }

    public static Zone recoverZone(UavPosShape self,Zone zone){
        Point3D[] boundingBox = recover(self);
        return new Zone(
                new double[]{boundingBox[1].getX()-zone.getX()[0],boundingBox[2].getX()+zone.getX()[1]},
                new double[]{boundingBox[1].getY()-zone.getY()[0],boundingBox[2].getY()+zone.getY()[1]},
                new double[]{boundingBox[1].getZ()-zone.getZ()[0],boundingBox[2].getZ()+zone.getZ()[1]}

        );
    }

    public static double getTheta(Point3D vec){
        return Math.acos(vec.getZ()/getLength(vec));
    }

    public static double getPhi(Point3D vec){
        return Math.atan2(vec.getY(),vec.getX());
    }

    public static double getLength(Point3D vec){
        return Math.sqrt(vec.getX()*vec.getX()+vec.getY()*vec.getY()+vec.getZ()*vec.getZ());
    }

    public static Point3D crossProduct(Point3D p1,Point3D p2){
        double x=p1.getY()*p2.getZ()-p1.getZ()*p2.getY();
        double y=p1.getZ()*p2.getX()-p1.getX()*p2.getZ();
        double z=p1.getX()*p2.getY()-p1.getY()*p2.getX();
        return new Point3D(x,y,z);
    }

    public static Point3D pointAdd(Point3D p1, Point3D p2){
        return new Point3D(p1.getX()+p2.getX(),p1.getY()+p2.getY(),p1.getZ()+p2.getZ());
    }

    public static Point3D pointMinus(Point3D p1, Point3D p2){
        return new Point3D(p1.getX()-p2.getX(),p1.getY()-p2.getY(),p1.getZ()-p2.getZ());
    }

    public static Point3D pointExtend(Point3D p,Double length){
        Point3D e = normalize(p);
        return new Point3D(p.getX()+e.getX()*length,p.getY()+e.getY()*length,p.getZ()+e.getZ()*length);
    }

    public static Point3D[] getReverse(Point3D x,Point3D y,Point3D z){
        double a = x.getX();
        double b = y.getX();
        double c = z.getX();
        double d = x.getY();
        double e = y.getY();
        double f = z.getY();
        double g = x.getZ();
        double h = y.getZ();
        double i = z.getZ();

        // 计算行列式
        double det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);

        if (det == 0) {
            System.out.println("逆矩阵求解错误，行列式为0");
        }

        return new Point3D[]{
                new Point3D((e*i-f*h)/det,-(d*i-f*g)/det,(d*h-e*g)/det),
                new Point3D(-(b*i-c*h)/det,(a*i-c*g)/det,-(a*h-b*g)/det),
                new Point3D((b*f-c*e)/det,-(a*f-c*d)/det,(a*e-b*d)/det)
        } ;
    }

    public static Point3D toRotate(Point3D[] matrix,Point3D p){
        return new Point3D(
                matrix[0].getX()*p.getX()+matrix[1].getX()*p.getY()+matrix[2].getX()*p.getZ(),
                matrix[0].getX()*p.getY()+matrix[1].getY()*p.getY()+matrix[2].getY()*p.getZ(),
                matrix[0].getX()*p.getZ()+matrix[1].getZ()*p.getY()+matrix[2].getZ()*p.getZ()
        );
    }

    public static Point3D normalize(Point3D p){
        double l = Math.sqrt(p.getX()*p.getX()+p.getY()*p.getY()+p.getZ()*p.getZ());
        return new Point3D(
                p.getX()/l,p.getY()/l,p.getZ()/l
        );
    }

    public static List<Point3D> str2Points(String s){
        String[] split = s.split(",");
        List<Point3D> points = new ArrayList<>();
        for(String sp:split){
            String[] c = sp.split(" ");
            if(c.length<3) continue;
            points.add(
                    new Point3D(Double.parseDouble(c[0]),
                            Double.parseDouble(c[1]),
                            Double.parseDouble(c[2]))
            );
        }
        return points;
    }



    public static Point3D[] getBoundingBox(List<Point3D> points){
        if(points.isEmpty()) {
            System.out.println("点集为空，无法找Bounding Box");
            return null;
        }
        Point3D p0=points.get(0);
        Point3D pmax=new Point3D(p0.getX(),p0.getY(),p0.getZ());
        Point3D pmin=new Point3D(p0.getX(),p0.getY(),p0.getZ());
        for(Point3D p:points){
            if(pmax.getX()<p.getX()) pmax.setX(p.getX());
            if(pmax.getY()<p.getY()) pmax.setY(p.getY());
            if(pmax.getZ()<p.getZ()) pmax.setZ(p.getZ());
            if(pmin.getX()>p.getX()) pmin.setX(p.getX());
            if(pmin.getY()>p.getY()) pmin.setY(p.getY());
            if(pmin.getZ()>p.getZ()) pmin.setZ(p.getZ());

        }
        return new Point3D[]{
                pmin,pmax
        };
    }
}
