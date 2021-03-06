package com.pgmmers.radar.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.pgmmers.radar.dal.bean.ActivationQuery;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.PluginType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.vo.DataColumnInfo;
import com.pgmmers.radar.service.enums.DataType;
import com.pgmmers.radar.service.model.*;
import com.pgmmers.radar.vo.model.*;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/services/v1/activation")
@Api(value = "ActivationApi", description = "策略集管理相关操作",  tags = {"策略集API"})
public class ActivationApiController {

    @Autowired
    private ActivationService activationService;

    @Autowired
    private FieldService fieldService;
    @Autowired
    private AbstractionService abstractionService;
    @Autowired
    private PreItemService preItemService;
    @Autowired
    private RuleService ruleService;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        ActivationVO activationVO = activationService.get(id);
        if (activationVO != null) {
            result.setSuccess(true);
            result.getData().put("activation", activationVO);
        }
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody ActivationQuery query) {
        return activationService.query(query);
    }

    @GetMapping("/datacolumns/{modelId}")
    public CommonResult getDataColumns(@PathVariable Long modelId) {
        List<DataColumnInfo> list = new ArrayList<DataColumnInfo>();
        // 1、Data
        DataColumnInfo ds = new DataColumnInfo(DataType.FIELDS.getDesc(), DataType.FIELDS.getName());
        List<FieldVO> listField = fieldService.listField(modelId);
        for (FieldVO field : listField) {
            ds.addChildren(field.getLabel(), field.getFieldName(), field.getFieldType());
        }
        list.add(ds);

        // 2、PREPARE
        ds = new DataColumnInfo(DataType.PREITEMS.getDesc(), DataType.PREITEMS.getName());
        List<PreItemVO> listPreItem = preItemService.listPreItem(modelId);
        for (PreItemVO preItem : listPreItem) {
            PluginType pt = PluginType.get(preItem.getPlugin());
            if (StringUtils.isNoneBlank(pt.getType())) {
                ds.addChildren(preItem.getLabel(), preItem.getDestField(), pt.getType());
            } else {
                List<DataColumnInfo> children = new ArrayList<>();
                JSONArray array = JSONArray.parseArray(pt.getMeta());
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    children.add(new DataColumnInfo(obj.getString("title"), obj.getString("column"), obj
                            .getString("type")));
                }
                ds.addChildren(preItem.getLabel(), preItem.getDestField(), children);
            }
        }
        list.add(ds);

        // 3、ABSTRACTION
        List<AbstractionVO> listAbstract = abstractionService.listAbstraction(modelId);
        ds = new DataColumnInfo(DataType.ABSTRACTIONS.getDesc(), DataType.ABSTRACTIONS.getName());
        if (listAbstract != null) {
            for (AbstractionVO abs : listAbstract) {
                ds.addChildren(abs.getLabel(), abs.getName(), FieldType.DOUBLE.name());
            }
        }

        list.add(ds);

        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", list);
        return result;
    }

    @GetMapping("/rulecolumns/{modelId}")
	public CommonResult getRuleColumns(@PathVariable Long modelId) {
		List<DataColumnInfo> list = new ArrayList<>();
		List<ActivationVO> listActivation=activationService.listActivation(modelId);
		if(listActivation!=null){
			for(ActivationVO activation:listActivation){
				DataColumnInfo ds=new DataColumnInfo(activation.getLabel(),activation.getActivationName());
				List<RuleVO> listRule=ruleService.listRule(activation.getId());
				for(RuleVO rule:listRule){
					ds.addChildren(rule.getLabel(), rule.getName(),rule.getId()+"");
				}
				list.add(ds);
			}
		}
		
		CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", list);
        return result;
	}

	@PutMapping
    public CommonResult save(@RequestBody ActivationVO activation) {
        return activationService.save(activation);
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return activationService.delete(id);
    }

	@PostMapping("/updateOrder")
	public CommonResult updateOrder(@RequestParam Long activationId, @RequestParam String ruleOrder) {
		return activationService.updateOrder(activationId,ruleOrder);
	}   
}
