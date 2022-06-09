package org.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * @ProjectName: tibi-common
 * @Package: org.mybatis.generator.plugin
 * @ClassName: NewbatisGenerator
 * @Author: lengjx
 * @Description: 添加Swagger注解
 * @Date: 2021/3/11 16:54
 */
public class NewbatisGenerator extends CommentPlugin {

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String remark = introspectedColumn.getRemarks();
        field.addJavaDocLine("@ApiModelProperty(value =  \"" + remark + "\" )");
        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addJavaDocLine("import io.swagger.annotations.ApiModelProperty;");
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }
}
