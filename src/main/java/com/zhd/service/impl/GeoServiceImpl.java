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
import java.util.stream.Collectors;

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
    public String registerDevice(String uid, Double xu, Double xl, Double yu, Double yl, Double zu, Double zl) {
        UavShape uavShape = new UavShape();
        uavShape.setUavId(uid);
        uavShape.setXu(xu);
        uavShape.setXl(xl);
        uavShape.setYu(yu);
        uavShape.setYl(yl);
        uavShape.setZu(zu);
        uavShape.setZl(zl);
        uavShapeMapper.deleteByMap(Collections.singletonMap("uav_id",uid));
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
    public void batchUploadHumanPosition(List<HumanPosition> humanPositions) {
        if (humanPositions.isEmpty()) {
            return;
        }
        humanPositionMapper.batchInsert(humanPositions);
    }

    @Override
    public void batchUploadDevicePosition(List<UavPosition> devicePositions) {
        if (devicePositions.isEmpty()) {
            return;
        }
        uavPositionMapper.batchInsert(devicePositions);
    }

    @Override
    public List<DivisionPlan2> planSafeArea(Long ts) {
        // 设置维度M
        M = constantValues.M;
        OctreeSpaceEncoder.setM(M);

        // 默认输入的位置坐标单位为0.1m，速度单位0.1m/s, 角度弧度制(即180度表示为PI/2)  [存储和输出也用该单位规则]
        // 空间设置为1024dm*1024dm*1024dm，对应实际102.4m*102.4m*102.4m的空间
        double R= constantValues.R;

        List<UavPosShape> uavPosShapes = uavPositionMapper.getUavPosShapeBeforeWindow(ts);

        // 为获取obs做预计算
        OctreeSearcher octreeSearcher = new OctreeSearcher(M);
        System.out.println("========Table Small-Small Conflict (timestamp="+ts+") =======");
        for(UavPosShape uavPosShape:uavPosShapes){
            Point3D[] boundingBox = GeoUtil.recover(uavPosShape);  // 中心，小，大
            octreeSearcher.insertUav(uavPosShape.getUavId(),boundingBox);
        }

        List<UavPosition> latestUavPositions = uavPositionMapper.getLatestUavPositions(ts);
        List<HumanPosition> latestHumanPositions = humanPositionMapper.getLatestHumanPositions(ts);

        // 计算obs
        List<List<Double>> obs =new ArrayList<>();
        Map<String,Integer> id2Priority = new HashMap<>();
        for(UavPosShape uavPosShape:uavPosShapes){
            List<Double> thisObs = new ArrayList<>();

            // observation self
            UavPosition positionSelf = latestUavPositions.stream()
                    .filter(pos -> pos.getUavId().equals(uavPosShape.getUavId()))
                    .findFirst().orElse(null);
            assert positionSelf != null;
            ObservationSelf self = getObservationSelf(positionSelf);

            thisObs.add(Math.abs(self.getV())<=0.001?0.001:(self.getVx()/self.getV()));
            thisObs.add(Math.abs(self.getV())<=0.001?0.001:(self.getVy()/self.getV()));
            thisObs.add(Math.abs(self.getV())<=0.001?0.001:(self.getVz()/self.getV()));
            thisObs.add(R*self.getV());
            thisObs.add(1.0*self.getPriority());
            id2Priority.put(uavPosShape.getUavId(),self.getPriority());

            // observation vehicles
            List<ObservationVehicle> observationVehicles = getObservationVehicles(latestUavPositions, positionSelf);
            for(int i=0;i<6;i++){
                if(i<observationVehicles.size()){
                    ObservationVehicle o = observationVehicles.get(i);
                    thisObs.add(R*o.getDx());
                    thisObs.add(R*o.getDy());
                    thisObs.add(R*o.getDz());
                    thisObs.add(Math.abs(o.getV())<=0.001?0.001:(o.getVx()/o.getV()));
                    thisObs.add(Math.abs(o.getV())<=0.001?0.001:(o.getVy()/o.getV()));
                    thisObs.add(Math.abs(o.getV())<=0.001?0.001:(o.getVz()/o.getV()));
                    thisObs.add(R*o.getV());
                    thisObs.add(o.getPriority());
                }else {
                    for(int j=0;j<8;j++) thisObs.add(0.0);
                }
            }


            // observation human
            List<ObservationHuman> observationHumans = getObservationHumans(latestHumanPositions, positionSelf);
            for(ObservationHuman observationHuman:observationHumans){
                observationHuman.setFear(GeoUtil.calculateFear(self,observationHuman));
            }
            for(int i=0;i<6;i++){
                if(i<observationHumans.size()){
                    ObservationHuman o = observationHumans.get(i);
                    thisObs.add(R*o.getDx());
                    thisObs.add(R*o.getDy());
                    thisObs.add(R*o.getDz());
                    thisObs.add(o.getFear());
                }else{
                    for(int j=0;j<4;j++) thisObs.add(0.0);
                }
            }
            obs.add(thisObs);
        }


        // 预测得到独占空间
        List<Zone> zones = predict(obs);

        //冲突检测， 得到网格块-Set<uavId>存在octree里(大-大冲突)
        Octree octree = new Octree(M);
        Map<String,DivisionPlan2> mp = new HashMap<>();
        for(int i=0;i<uavPosShapes.size();i++){
            UavPosShape self = uavPosShapes.get(i);
            Zone zone = zones.get(i);
            // 把zone恢复到空间坐标系中并且包含其占用的空间
            zone=GeoUtil.recoverZone(self,zone,R);

            DivisionPlan2 dp=new DivisionPlan2(self.getUavId(),DivisionPlan2.zone2LargeArea(zone),new ArrayList<>());
            mp.put(self.getUavId(),dp);
            List<ConflictZonePairs> conflictZonePairs = octreeSearcher.insertZone(self.getUavId(), zone);
            for(ConflictZonePairs pairs:conflictZonePairs){
                Point3D[] conflict = pairs.getConflict();
                List<OctreeGrid> grids = OctreeSpaceEncoder.encodeWithBoundingBox(conflict[0], conflict[1], conflict[2]);
                if(pairs.getType()==3) {
                    // 大-大冲突
                    for (OctreeGrid grid : grids) {
                        Map<String, Object> props = new HashMap<>();
                        Set<String> s = new HashSet<>();
                        s.add(pairs.getId1());
                        s.add(pairs.getId2());
                        props.put("conflictIdSet", s);
                        octree.insertWithProperties(grid, props);
                    }
                }else if(pairs.getType()==2){
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
        List list = JsonUtil.jsonStringToList(responseStr, Zone.class);
        List<Zone>  zones= new ArrayList<>();
        for(Object o:list) zones.add((Zone) o);
        return zones;
    }

    private ObservationSelf getObservationSelf(UavPosition uavPosition) {
        ObservationSelf self = new ObservationSelf();
        self.setDx(uavPosition.getPx());
        self.setDy(uavPosition.getPy());
        self.setDz(uavPosition.getPz());
        self.setVx(uavPosition.getVx());
        self.setVy(uavPosition.getVy());
        self.setVz(uavPosition.getVz());
        self.setV(Math.sqrt(uavPosition.getVx() * uavPosition.getVx() +
                uavPosition.getVy() * uavPosition.getVy() +
                uavPosition.getVz() * uavPosition.getVz()));
        self.setPriority(1);
        // 设置优先级等其他字段
        return self;
    }

    private List<ObservationVehicle> getObservationVehicles(List<UavPosition> uavPositions, UavPosition self) {
        return uavPositions.stream()
                .filter(uav -> !uav.getUavId().equals(self.getUavId())) // 排除自身
                .map(uav -> {
                    ObservationVehicle obsVehicle = new ObservationVehicle();
                    obsVehicle.setDx(uav.getPx() - self.getPx());
                    obsVehicle.setDy(uav.getPy() - self.getPy());
                    obsVehicle.setDz(uav.getPz() - self.getPz());
                    obsVehicle.setVx(uav.getVx());
                    obsVehicle.setVy(uav.getVy());
                    obsVehicle.setVz(uav.getVz());
                    obsVehicle.setV(Math.sqrt(uav.getVx() * uav.getVx() +
                            uav.getVy() * uav.getVy() +
                            uav.getVz() * uav.getVz()));
                    obsVehicle.setPriority(1);
                    // 设置优先级等其他字段
                    return obsVehicle;
                })
                .sorted(Comparator.comparingDouble(v -> (v.getDx() * v.getDx() + v.getDy() * v.getDy() + v.getDz() * v.getDz())))
                .filter(v -> (v.getDx() * v.getDx() + v.getDy() * v.getDy() + v.getDz() * v.getDz()) < 20 * 20) // 排除距离过远的
                .limit(7) // 限制数量
                .collect(Collectors.toList());
    }

    private List<ObservationHuman> getObservationHumans(List<HumanPosition> humanPositions, UavPosition self) {
        return humanPositions.stream()
                .map(human -> {
                    ObservationHuman obsHuman = new ObservationHuman();
                    obsHuman.setDx(human.getPx() - self.getPx());
                    obsHuman.setDy(human.getPy() - self.getPy());
                    obsHuman.setDz(human.getPz() - self.getPz());
                    obsHuman.setFear(0);
                    return obsHuman;
                })
                .sorted(Comparator.comparingDouble(h -> (h.getDx() * h.getDx() + h.getDy() * h.getDy() + h.getDz() * h.getDz())))
                .filter(v -> (v.getDx() * v.getDx() + v.getDy() * v.getDy() + v.getDz() * v.getDz()) < 10 * 10) // 排除距离过远的
                .limit(6) // 限制数量
                .collect(Collectors.toList());
    }
}
