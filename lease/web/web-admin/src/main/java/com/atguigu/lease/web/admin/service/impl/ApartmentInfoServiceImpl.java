package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
     private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper  graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private FacilityInfoMapper  facilityInfoMapper;

    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;


    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;

    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate = apartmentSubmitVo.getId() != null;
        super.saveOrUpdate(apartmentSubmitVo);
        if (isUpdate) {
            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphQueryWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);
            //2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(ApartmentFacility::getApartmentId, apartmentSubmitVo.getId());
            apartmentFacilityService.remove(facilityQueryWrapper);

            //3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
            apartmentLabelService.remove(lambdaQueryWrapper);
            //4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> feeQueryWrapper = new LambdaQueryWrapper<>();
            feeQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(feeQueryWrapper);

        }
            //1.插入图片列表
            List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
            if (!CollectionUtils.isEmpty(graphVoList)){
                ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
                for (GraphVo graphVo : graphVoList) {
                    GraphInfo graphInfo = new GraphInfo();
                    graphInfo.setItemId(apartmentSubmitVo.getId());
                    graphInfo.setItemType(ItemType.APARTMENT);
                    graphInfo.setName(graphVo.getName());
                    graphInfo.setUrl(graphVo.getUrl());
                    graphInfoList.add(graphInfo);
                }
                graphInfoService.saveBatch(graphInfoList);
            }

            //2.插入配套列表
            List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
            if (!CollectionUtils.isEmpty(facilityInfoIdList)){
                ArrayList<ApartmentFacility> facilityList = new ArrayList<>();
                for (Long facilityId : facilityInfoIdList) {
                    ApartmentFacility apartmentFacility = new ApartmentFacility();
                    apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                    apartmentFacility.setFacilityId(facilityId);
                    facilityList.add(apartmentFacility);
                }
                apartmentFacilityService.saveBatch(facilityList);
            }

            //3.插入标签列表
            List<Long> labelIds = apartmentSubmitVo.getLabelIds();
            if (!CollectionUtils.isEmpty(labelIds)) {
                List<ApartmentLabel> apartmentLabelList = new ArrayList<>();
                for (Long labelId : labelIds) {
                    ApartmentLabel apartmentLabel = new ApartmentLabel();
                    apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                    apartmentLabel.setLabelId(labelId);
                    apartmentLabelList.add(apartmentLabel);
                }
                apartmentLabelService.saveBatch(apartmentLabelList);
            }
            //4.插入杂费列表
            List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
            if (!CollectionUtils.isEmpty(feeValueIds)) {
                ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
                for (Long feeValueId : feeValueIds) {
                    ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                    apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                    apartmentFeeValue.setFeeValueId(feeValueId);
                    apartmentFeeValueList.add(apartmentFeeValue);
                }
                apartmentFeeValueService.saveBatch(apartmentFeeValueList);
            }


    }

    @Override
    public IPage<ApartmentItemVo> pageItem(IPage<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper .pageItem(page, queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //1.查询公寓的信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);

        //2. 查询公寓的图片列表
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT,id);

        // 3.查询公寓的标签列表
        List<LabelInfo>  labelInfoList = labelInfoMapper.selectListByApartmentId(id);

        // 4.查询公寓的配套列表
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);

        // 5.查询公寓的杂费列表
         List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(id);

        //6.组装这些查询结果然后返回
         ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
            BeanUtils .copyProperties(apartmentInfo, apartmentDetailVo);
            apartmentDetailVo.setGraphVoList(graphVoList);
            apartmentDetailVo.setLabelInfoList(labelInfoList);
            apartmentDetailVo.setFacilityInfoList(facilityInfoList);
            apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        return apartmentDetailVo;
    }

    @Override
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
        roomQueryWrapper.eq(RoomInfo::getApartmentId, id);
        Long count = roomInfoMapper.selectCount(roomQueryWrapper);
         if (count > 0) {
            //终止删除，并相应提示信息
             throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }

        super.removeById(id);
        //1.删除图片列表
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemId, ItemType.APARTMENT);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);
        //2.删除配套列表
        LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        apartmentFacilityService.remove(facilityQueryWrapper);

        //3.删除标签列表
        LambdaQueryWrapper<ApartmentLabel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelService.remove(lambdaQueryWrapper);
        //4.删除杂费列表
        LambdaQueryWrapper<ApartmentFeeValue> feeQueryWrapper = new LambdaQueryWrapper<>();
        feeQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueService.remove(feeQueryWrapper);
    }
}




