package com.zhd.service.impl;

import com.zhd.constant.ConstantValues;
import com.zhd.entity.HumanPosition;
import com.zhd.entity.UavPosition;
import com.zhd.entity.UavShape;
import com.zhd.entity.tmp.*;
import com.zhd.geometry.algorithm.CollisionDecider;
import com.zhd.geometry.algorithm.OctreeSearcher;
import com.zhd.geometry.structure.Octree;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.geometry.structure.Point3D;
import com.zhd.geometry.algorithm.GeoUtil;
import com.zhd.geometry.algorithm.OctreeSpaceEncoder;
import com.zhd.mapper.HumanPositionMapper;
import com.zhd.mapper.UavPositionMapper;
import com.zhd.mapper.UavShapeMapper;
import com.zhd.service.GeoService;
import com.zhd.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeoServiceImpl implements GeoService {

    private static int M =10;

    @Autowired
    private UavShapeMapper uavShapeMapper;

    @Autowired
    private UavPositionMapper uavPositionMapper;

    @Autowired
    private HumanPositionMapper humanPositionMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConstantValues constantValues;

    @Override
    public String registerDevice(Double xu, Double xl, Double yu, Double yl, Double zu, Double zl) {
        UavShape uavShape = new UavShape();
        uavShape.setXu(xu);
        uavShape.setXl(xl);
        uavShape.setYu(yu);
        uavShape.setYl(yl);
        uavShape.setZu(zu);
        uavShape.setZl(zl);
        uavShapeMapper.insert(uavShape);
        return uavShape.getUavId();
    }

    @Override
    public void uploadPosition(String uid, Double px, Double py, Double pz, Double vx, Double vy, Double vz, Double theta, Double phi, Long ts) {
        UavPosition uavPosition = new UavPosition();
        uavPosition.setUavId(uid);
        uavPosition.setPx(px);
        uavPosition.setPy(py);
        uavPosition.setPz(pz);
        uavPosition.setVx(vx);
        uavPosition.setVy(vy);
        uavPosition.setVz(vz);
        uavPosition.setTheta(theta);
        uavPosition.setPhi(phi);
        uavPosition.setTs(ts);
        uavPositionMapper.insert(uavPosition);
    }

    @Override
    public void uploadHumanPosition(String hid, Double px, Double py, Double pz, Double vx, Double vy, Double vz, Long ts) {
        HumanPosition humanPosition = new HumanPosition();
        humanPosition.setHumanId(hid);
        humanPosition.setPx(px);
        humanPosition.setPy(py);
        humanPosition.setPz(pz);
        humanPosition.setVx(vx);
        humanPosition.setVy(vy);
        humanPosition.setVz(vz);
        humanPosition.setTs(ts);
        humanPositionMapper.insert(humanPosition);
    }

    @Override
    public List<DivisionPlan2> planSafeArea(Long ts) {
        List<UavPosShape> uavPosShapes = uavPositionMapper.getUavPosShapeBeforeWindow(ts);

        // 设置维度M
        M = constantValues.M;
        OctreeSpaceEncoder.setM(M);

        // 为获取obs做预计算
        OctreeSearcher octreeSearcher = new OctreeSearcher(M);
        System.out.println("========Table Small-Small Conflict (timestamp="+ts+") =======");
        for(UavPosShape uavPosShape:uavPosShapes){
            Point3D[] boundingBox = GeoUtil.recover(uavPosShape);  // 中心，小，大
//            List<OctreeGrid> rMin = OctreeSpaceEncoder.encodeWithBoundingBox(boundingBox[0], boundingBox[2], boundingBox[1]);
            octreeSearcher.insertUav(uavPosShape.getUavId(),boundingBox);
        }

        // 计算obs
        List<List<Double>> obs =new ArrayList<>();
        Map<String,Integer> id2Priority = new HashMap<>();
        for(UavPosShape uavPosShape:uavPosShapes){
            Point3D[] boundingBox = GeoUtil.recover(uavPosShape);
            List<String> ids = octreeSearcher.findNearestKUav(boundingBox, 8);
            ids.remove(uavPosShape.getUavId());

            List<Double> thisObs = new ArrayList<>();

            // observation self
            ObservationSelf self = uavPositionMapper.getObservationSelf(ts,uavPosShape.getUavId());

            thisObs.add(self.getVx()/self.getV());
            thisObs.add(self.getVy()/self.getV());
            thisObs.add(self.getVz()/self.getV());
            thisObs.add(self.getV());
            thisObs.add(1.0*self.getPriority());
            id2Priority.put(uavPosShape.getUavId(),self.getPriority());

            // observation vehicles
            String idStr = "";
            if(ids.isEmpty()) idStr="-1024";
            else
                for(int i=0;i<ids.size();i++){
                    idStr += ids.get(i);
                    if(i!=ids.size()-1) idStr+=",";
                }

            List<ObservationVehicle> observationVehicles = uavPositionMapper.getObservationVehicles(idStr, ts,7,self.getDx(),self.getDy(),self.getDz());


            for(int i=0;i<6;i++){
                if(i<observationVehicles.size()){
                    ObservationVehicle o = observationVehicles.get(i);
                    thisObs.add(o.getDx());
                    thisObs.add(o.getDy());
                    thisObs.add(o.getDz());
                    thisObs.add(o.getVx());
                    thisObs.add(o.getVy());
                    thisObs.add(o.getVz());
                    thisObs.add(o.getV());
                    thisObs.add(o.getPriority());
                }else {
                    for(int j=0;j<8;j++) thisObs.add(0.0);
                }
            }


            // observation human
            List<ObservationHuman> observationHumans = humanPositionMapper.getObservationHumans(ts, 6, self.getDx(),self.getDy(),self.getDz());
            for(ObservationHuman observationHuman:observationHumans){
                observationHuman.setFear(GeoUtil.calculateFear(self,observationHuman));
            }
            for(int i=0;i<6;i++){
                if(i<observationHumans.size()){
                    ObservationHuman o = observationHumans.get(i);
                    thisObs.add(o.getDx());
                    thisObs.add(o.getDy());
                    thisObs.add(o.getDz());
                    thisObs.add(o.getFear());
                }else{
                    for(int j=0;j<4;j++) thisObs.add(0.0);
                }
            }
            obs.add(thisObs);
        }

//        System.out.println("=====obs=====");
//        for(List<Double> o:obs){
//            System.out.println(Arrays.toString(o.toArray()));
//        }

        // 预测得到独占空间
        List<Zone> zones = predict(obs);

        //冲突检测， 得到网格块-Set<uavId>存在octree里(大-大冲突)
        Octree octree = new Octree(M);
        Map<String,DivisionPlan2> mp = new HashMap<>();
        for(int i=0;i<uavPosShapes.size();i++){
            UavPosShape self = uavPosShapes.get(i);
            Zone zone = zones.get(i);
            // 把zone恢复到空间坐标系中并且包含其占用的空间
            zone=GeoUtil.recoverZone(self,zone);

            DivisionPlan2 dp=new DivisionPlan2(self.getUavId(),DivisionPlan2.zone2LargeArea(zone),new ArrayList<>());
            mp.put(self.getUavId(),dp);
            List<ConflictZonePairs> conflictZonePairs = octreeSearcher.insertZone(self.getUavId(), zone);
            for(ConflictZonePairs pairs:conflictZonePairs){
                Point3D[] conflict = pairs.getConflict();
                List<OctreeGrid> grids = OctreeSpaceEncoder.encodeWithBoundingBox(conflict[0], conflict[1], conflict[2]);
                if(pairs.getType()==3){
                    // 大-大冲突
                    for(OctreeGrid grid:grids){
                        Map<String,Object> props=new HashMap<>();
                        Set<String> s= new HashSet<>();
                        s.add(pairs.getId1());
                        s.add(pairs.getId2());
                        props.put("conflictIdSet",s);
                        octree.insertWithProperties(grid,props);
                    }
                    // 大-小冲突
                    DivisionPlan2 divisionPlan2 = mp.get(self.getUavId());
                    if(divisionPlan2==null){
                        divisionPlan2=new DivisionPlan2(self.getUavId(),DivisionPlan2.zone2LargeArea(zone),grids);
                    }else{
                        List<OctreeGrid> exclude = divisionPlan2.getExclude();
                        exclude.addAll(grids);
                        divisionPlan2=new DivisionPlan2(self.getUavId(),DivisionPlan2.zone2LargeArea(zone),exclude);
                    }
                    mp.put(self.getUavId(),divisionPlan2);
                }
            }
        }


        mp = CollisionDecider.decideCollisionBasedOnPriority(mp, id2Priority, octree);

        List<DivisionPlan2> result = new ArrayList<>();
        for(String key:mp.keySet()){
            result.add(mp.get(key));
        }

        return result;
    }

    public List<Zone> predict(List<List<Double>> obs){
        String url = constantValues.MAPPO_URL+"/predict";
        Map<String,Object> mp = new HashMap<>();
        mp.put("model_name","mappo");
        mp.put("obs",obs);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(mp, headers);
        String responseStr = restTemplate.postForObject(url, requestEntity, String.class);
//        System.out.println("predict response:"+responseStr);
        List list = JsonUtil.jsonStringToList(responseStr, Zone.class);
        List<Zone>  zones= new ArrayList<>();
        for(Object o:list) zones.add((Zone) o);
        return zones;
    }

//    @Override
//    public List<DivisionPlan> planSafeArea() {
//        List<DivisionPlan> plans = new ArrayList<>();
//        List<UavPosShape> uavPosShapeBeforeWindow = uavPositionMapper.getUavPosShapeBeforeWindow(1234567890L);
//        for (UavPosShape uav:uavPosShapeBeforeWindow){
//            System.out.println(uav);
//            // 1. 线性代数运算，恢复每个点在空间中的位置
//            Point3D p_ = new Point3D(uav.getX(),uav.getY(),uav.getZ());
//            Point3D v_ = new Point3D(uav.getVx(), uav.getY(), uav.getZ());
//            Point3D _x_ = GeoUtil.normalize(v_);
//            Point3D _z_ = GeoUtil.normalize(new Point3D(uav.getHvx(), uav.getHvy(), uav.getHvz()));
//            Point3D _y_ = GeoUtil.crossProduct(_z_,_x_);
//            _y_=GeoUtil.normalize(_y_);
//            Point3D[] reverse = GeoUtil.getReverse(_x_, _y_, _z_);
//            List<Point3D> pointO = GeoUtil.str2Points(uav.getShapePoints());
//            List<Point3D> pointSmall = new ArrayList<>();   // 小圈范围
//            List<Point3D> pointLarge = new ArrayList<>();   // 大圈范围
//            for(Point3D p:pointO){
//                pointSmall.add(
//                        // TODO 扩展的倍数
//                        GeoUtil.pointAdd(GeoUtil.pointExtend(GeoUtil.toRotate(reverse,p),20.0),p_)
//                );
//                pointLarge.add(
//                        // TODO 扩展的倍数
//                        GeoUtil.pointAdd(GeoUtil.pointExtend(GeoUtil.toRotate(reverse,p),80.0),p_)
//                );
//            }
//
//            // 2. 基于Bounding Box 获得安全区域
//            Point3D[] boundingBox1 = GeoUtil.getBoundingBox(pointSmall);
//            Point3D[] boundingBox2 = GeoUtil.getBoundingBox(pointLarge);
//            if(boundingBox1==null||boundingBox2==null) continue;
////            System.out.println(uav.getUavId());
////            System.out.println("Bounding Box:"+boundingBox1[1]+","+boundingBox1[0]);
////            System.out.println("Bounding Box:"+boundingBox2[1]+","+boundingBox2[0]);
//            List<OctreeGrid> smallArea = OctreeSpaceEncoder.encodeWithBoundingBox(p_, boundingBox1[1], boundingBox1[0]);
//            List<OctreeGrid> largeArea = OctreeSpaceEncoder.encodeWithBoundingBox(p_, boundingBox2[1], boundingBox2[0]);
//
//            plans.add(new DivisionPlan(
//                    uav.getUavId(),
//                    smallArea,
//                    largeArea
//            ));
//
//
//        }
//
//        // 3. 冲突计算  冲突划分
//        return collisionDecider.divide1(plans, M);
//    }
}
