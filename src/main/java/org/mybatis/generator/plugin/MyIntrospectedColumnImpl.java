package org.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;

public class MyIntrospectedColumnImpl extends IntrospectedColumn {

    public MyIntrospectedColumnImpl() {
    }

    public boolean isBLOBColumn() {
        return false;
    }
}
