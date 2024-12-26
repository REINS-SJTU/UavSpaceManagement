package com.zhd.service.impl;

import com.zhd.entity.tmp.DivisionPlan;
import com.zhd.entity.tmp.UavPosShape;
import com.zhd.geometry.algorithm.CollisionDecider;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.geometry.structure.Point3D;
import com.zhd.geometry.algorithm.GeoUtil;
import com.zhd.geometry.algorithm.OctreeSpaceEncoder;
import com.zhd.mapper.UavPositionMapper;
import com.zhd.mapper.UavShapeMapper;
import com.zhd.service.GeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GeoServiceImpl implements GeoService {

    private static final int M =10;

    @Autowired
    private UavShapeMapper uavShapeMapper;

    @Autowired
    private UavPositionMapper uavPositionMapper;

    @Autowired
    private CollisionDecider collisionDecider;

    @Override
    public void registerDevice(Double xu, Double xl, Double yu, Double yl, Double zu, Double zl) {
        uavShapeMapper.insertByBounders(xu,xl,yu,yl,zu,zl);
    }

    @Override
    public void uploadPosition(Long uid, Double x, Double y, Double z,
                               Double vx, Double vy, Double vz,
                               Double hvx, Double hvy, Double hvz,
                               Long ts) {
        uavPositionMapper.insertPosition(uid,x,y,z,vx,vy,vz,hvx,hvy,hvz,ts);
    }

    @Override
    public List<DivisionPlan> planSafeArea() {
        List<DivisionPlan> plans = new ArrayList<>();
        List<UavPosShape> uavPosShapeBeforeWindow = uavPositionMapper.getUavPosShapeBeforeWindow(1234567890L);
        for (UavPosShape uav:uavPosShapeBeforeWindow){
            System.out.println(uav);
            // 1. 线性代数运算，恢复每个点在空间中的位置
            Point3D p_ = new Point3D(uav.getX(),uav.getY(),uav.getZ());
            Point3D v_ = new Point3D(uav.getVx(), uav.getY(), uav.getZ());
            Point3D _x_ = GeoUtil.normalize(v_);
            Point3D _z_ = GeoUtil.normalize(new Point3D(uav.getHvx(), uav.getHvy(), uav.getHvz()));
            Point3D _y_ = GeoUtil.crossProduct(_z_,_x_);
            _y_=GeoUtil.normalize(_y_);
            Point3D[] reverse = GeoUtil.getReverse(_x_, _y_, _z_);
            List<Point3D> pointO = GeoUtil.str2Points(uav.getShapePoints());
            List<Point3D> pointSmall = new ArrayList<>();   // 小圈范围
            List<Point3D> pointLarge = new ArrayList<>();   // 大圈范围
            for(Point3D p:pointO){
                pointSmall.add(
                        // TODO 扩展的倍数
                        GeoUtil.pointAdd(GeoUtil.pointExtend(GeoUtil.toRotate(reverse,p),20.0),p_)
                );
                pointLarge.add(
                        // TODO 扩展的倍数
                        GeoUtil.pointAdd(GeoUtil.pointExtend(GeoUtil.toRotate(reverse,p),80.0),p_)
                );
            }

            // 2. 基于Bounding Box 获得安全区域
            Point3D[] boundingBox1 = GeoUtil.getBoundingBox(pointSmall);
            Point3D[] boundingBox2 = GeoUtil.getBoundingBox(pointLarge);
            if(boundingBox1==null||boundingBox2==null) continue;
//            System.out.println(uav.getUavId());
//            System.out.println("Bounding Box:"+boundingBox1[1]+","+boundingBox1[0]);
//            System.out.println("Bounding Box:"+boundingBox2[1]+","+boundingBox2[0]);
            List<OctreeGrid> smallArea = OctreeSpaceEncoder.encodeWithBoundingBox(p_, boundingBox1[1], boundingBox1[0]);
            List<OctreeGrid> largeArea = OctreeSpaceEncoder.encodeWithBoundingBox(p_, boundingBox2[1], boundingBox2[0]);

            plans.add(new DivisionPlan(
                    uav.getUavId(),
                    smallArea,
                    largeArea
            ));


        }

        // 3. 冲突计算  冲突划分
        return collisionDecider.divide1(plans, M);
    }
}
