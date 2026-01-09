package io.github.myacelw.mybatis.dynamic.draw.controller;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.draw.service.DrawBuilder;
import io.github.myacelw.mybatis.dynamic.draw.vo.DisplayMode;
import io.github.myacelw.mybatis.dynamic.draw.vo.Draw;
import io.github.myacelw.mybatis.dynamic.draw.vo.PropertyName;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 绘图Controller
 *
 * @author liuwei
 */
@RestController
@RequestMapping("/draw")
@AllArgsConstructor
public class DrawController {

    private ModelService modelService;

    /**
     * 显示模式列表
     */
    @GetMapping("/data/displayModes.json")
    public List<DisplayMode> getDisplayModes() {
        List<DisplayMode> displayModes = new ArrayList<>();
        displayModes.add(new DisplayMode("名称-类型", PropertyName.name.name(), PropertyName.comment.name(), PropertyName.name.name(), PropertyName.javaClass.name()));
        displayModes.add(new DisplayMode("名称-描述", PropertyName.name_comment.name(), null, PropertyName.name.name(), PropertyName.comment.name()));
        displayModes.add(new DisplayMode("描述-名称", PropertyName.comment.name(), PropertyName.name.name(), PropertyName.comment.name(), PropertyName.name.name()));
        displayModes.add(new DisplayMode("描述-类型", PropertyName.comment.name(), PropertyName.name.name(), PropertyName.comment.name(), PropertyName.fieldType.name()));
        displayModes.add(new DisplayMode("数据库表", PropertyName.tableName.name(), PropertyName.comment.name(), PropertyName.columnName.name(), PropertyName.columnType.name()));
        return displayModes;
    }

    @GetMapping("/data/draw.json")
    public Draw getDraw(@RequestParam(required = false) String moduleGroup,
                        @RequestParam(defaultValue = "40") int headHeight,
                        @RequestParam(defaultValue = "24") int portHeight,
                        @RequestParam(defaultValue = "220") int width) {
        List<Model> models = modelService.getAllRegisteredModels().stream()
                .peek(t -> {
                    if (!StringUtils.hasText(t.getExtPropertyValueForString(Model.EXT_PROPERTY_MODULE_GROUP))) {
                        t.putExtProperty(Model.EXT_PROPERTY_MODULE_GROUP, "其他");
                    }
                })
                .filter(t -> moduleGroup == null || "ALL".equals(moduleGroup) || Objects.equals(t.getExtPropertyValueForString(Model.EXT_PROPERTY_MODULE_GROUP), moduleGroup))
                .collect(Collectors.toList());

        DrawBuilder drawBuilder = new DrawBuilder();
        drawBuilder.setHeadHeight(headHeight);
        drawBuilder.setPortHeight(portHeight);
        drawBuilder.setWidth(width);

        return drawBuilder.build(models);
    }

    @GetMapping("/data/moduleGroups.json")
    public List<String> getModuleGroups() {
        return modelService.getAllRegisteredModels().stream()
                .peek(t -> {
                    if (!StringUtils.hasText(t.getExtPropertyValueForString(Model.EXT_PROPERTY_MODULE_GROUP))) {
                        t.putExtProperty(Model.EXT_PROPERTY_MODULE_GROUP, "其他");
                    }
                })
                .map(t -> t.getExtPropertyValueForString(Model.EXT_PROPERTY_MODULE_GROUP))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

}
