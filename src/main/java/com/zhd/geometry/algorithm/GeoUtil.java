package com.zhd.geometry.algorithm;

import com.zhd.geometry.structure.Point3D;

import java.util.ArrayList;
import java.util.List;

public class GeoUtil {
    public static Point3D crossProduct(Point3D p1,Point3D p2){
        double x=p1.getY()*p2.getZ()-p1.getZ()*p2.getY();
        double y=p1.getZ()*p2.getX()-p1.getX()*p2.getZ();
        double z=p1.getX()*p2.getY()-p1.getY()*p2.getX();
        return new Point3D(x,y,z);
    }

    public static Point3D pointAdd(Point3D p1, Point3D p2){
        return new Point3D(p1.getX()+p2.getX(),p1.getY()+p2.getY(),p1.getZ()+p2.getZ());
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

        // 计算伴随矩阵并同时进行转置
//        double[][] adjugate = {
//                { (e * i - f * h), -(b * i - c * h), (b * f - c * e) },
//                { -(d * i - f * g), (a * i - c * g), -(a * f - c * d) },
//                { (d * h - e * g), -(a * h - b * g), (a * e - b * d) }
//        };
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
