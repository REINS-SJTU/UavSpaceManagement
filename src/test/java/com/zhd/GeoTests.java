package com.zhd;


import com.zhd.entity.UavPosition;
import com.zhd.entity.tmp.UavPosShape;
import com.zhd.mapper.UavPositionMapper;
import com.zhd.mapper.UavShapeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@SpringBootTest(classes = PostGisDemoApplication.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class GeoTests {

    @Autowired
    private UavPositionMapper uavPositionMapper;
    @Autowired
    private UavShapeMapper uavShapeMapper;

    @Test
    public void insertShape(){
        uavShapeMapper.insertByBounders(0.9,-1.2,0.8,-0.8,1.2,-0.4);
        List<UavPosition> uavPositions = uavPositionMapper.selectList(null);
        for(UavPosition u:uavPositions){
            System.out.println(u);
        }
    }

    @Test void insertPosition(){
        uavPositionMapper.insertPosition(100L,
                1.0,1.0,1.0,
                1.0,1.0,1.0,
                1.0,1.0,1.0,
                new Date().getTime());
    }


    @Test
    void test01(){
        List<UavPosShape> uavPosShapeBeforeWindow = uavPositionMapper.getUavPosShapeBeforeWindow(100L);
        for(UavPosShape u:uavPosShapeBeforeWindow){
            System.out.println(u);
        }
    }

}
