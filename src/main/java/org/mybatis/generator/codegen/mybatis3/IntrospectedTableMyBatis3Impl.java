package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.AnnotatedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.MixedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.model.BaseRecordGenerator;
import org.mybatis.generator.codegen.mybatis3.model.ExampleGenerator;
import org.mybatis.generator.codegen.mybatis3.model.PrimaryKeyGenerator;
import org.mybatis.generator.codegen.mybatis3.model.RecordWithBLOBsGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.internal.ObjectFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IntrospectedTableMyBatis3Impl extends IntrospectedTable {

    protected List<AbstractJavaGenerator> javaModelGenerators = new ArrayList();
    protected List<AbstractJavaGenerator> clientGenerators = new ArrayList();
    protected AbstractXmlGenerator xmlMapperGenerator;

    public IntrospectedTableMyBatis3Impl() {
        super(TargetRuntime.MYBATIS3);
    }

    public void calculateGenerators(List<String> warnings, ProgressCallback progressCallback) {
        this.calculateJavaModelGenerators(warnings, progressCallback);
        AbstractJavaClientGenerator javaClientGenerator = this.calculateClientGenerators(warnings, progressCallback);
        this.calculateXmlMapperGenerator(javaClientGenerator, warnings, progressCallback);
    }

    protected void calculateXmlMapperGenerator(AbstractJavaClientGenerator javaClientGenerator, List<String> warnings, ProgressCallback progressCallback) {
        if (javaClientGenerator == null) {
            if (this.context.getSqlMapGeneratorConfiguration() != null) {
                this.xmlMapperGenerator = new XMLMapperGenerator();
            }
        } else {
            this.xmlMapperGenerator = javaClientGenerator.getMatchedXMLGenerator();
        }

        this.initializeAbstractGenerator(this.xmlMapperGenerator, warnings, progressCallback);
    }

    protected AbstractJavaClientGenerator calculateClientGenerators(List<String> warnings, ProgressCallback progressCallback) {
        if (!this.rules.generateJavaClient()) {
            return null;
        } else {
            AbstractJavaClientGenerator javaGenerator = this.createJavaClientGenerator();
            if (javaGenerator == null) {
                return null;
            } else {
                this.initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
                this.clientGenerators.add(javaGenerator);
                return javaGenerator;
            }
        }
    }

    protected AbstractJavaClientGenerator createJavaClientGenerator() {
        if (this.context.getJavaClientGeneratorConfiguration() == null) {
            return null;
        } else {
            String type = this.context.getJavaClientGeneratorConfiguration().getConfigurationType();
            Object javaGenerator;
            if ("XMLMAPPER".equalsIgnoreCase(type)) {
                javaGenerator = new JavaMapperGenerator();
            } else if ("MIXEDMAPPER".equalsIgnoreCase(type)) {
                javaGenerator = new MixedClientGenerator();
            } else if ("ANNOTATEDMAPPER".equalsIgnoreCase(type)) {
                javaGenerator = new AnnotatedClientGenerator();
            } else if ("MAPPER".equalsIgnoreCase(type)) {
                javaGenerator = new JavaMapperGenerator();
            } else {
                javaGenerator = (AbstractJavaClientGenerator) ObjectFactory.createInternalObject(type);
            }

            return (AbstractJavaClientGenerator) javaGenerator;
        }
    }

    protected void calculateJavaModelGenerators(List<String> warnings, ProgressCallback progressCallback) {
        if (this.getRules().generateExampleClass()) {
            AbstractJavaGenerator javaGenerator = new ExampleGenerator();
            this.initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            this.javaModelGenerators.add(javaGenerator);
        }

        if (this.getRules().generatePrimaryKeyClass()) {
            AbstractJavaGenerator javaGenerator = new PrimaryKeyGenerator();
            this.initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            this.javaModelGenerators.add(javaGenerator);
        }

        if (this.getRules().generateBaseRecordClass()) {
            AbstractJavaGenerator javaGenerator = new BaseRecordGenerator();
            this.initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            this.javaModelGenerators.add(javaGenerator);
        }

        if (this.getRules().generateRecordWithBLOBsClass()) {
            AbstractJavaGenerator javaGenerator = new RecordWithBLOBsGenerator();
            this.initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            this.javaModelGenerators.add(javaGenerator);
        }

    }

    protected void initializeAbstractGenerator(AbstractGenerator abstractGenerator, List<String> warnings, ProgressCallback progressCallback) {
        if (abstractGenerator != null) {
            abstractGenerator.setContext(this.context);
            abstractGenerator.setIntrospectedTable(this);
            abstractGenerator.setProgressCallback(progressCallback);
            abstractGenerator.setWarnings(warnings);
        }
    }

    public List<GeneratedJavaFile> getGeneratedJavaFiles() {
        List<GeneratedJavaFile> answer = new ArrayList();
        Iterator var2 = this.javaModelGenerators.iterator();

        AbstractJavaGenerator javaGenerator;
        List compilationUnits;
        Iterator var5;
        CompilationUnit compilationUnit;
        GeneratedJavaFile gjf;
        while (var2.hasNext()) {
            javaGenerator = (AbstractJavaGenerator) var2.next();
            compilationUnits = javaGenerator.getCompilationUnits();
            var5 = compilationUnits.iterator();

            while (var5.hasNext()) {
                compilationUnit = (CompilationUnit) var5.next();
                gjf = new GeneratedJavaFile(compilationUnit, this.getTargetProject(this.context.getJavaModelGeneratorConfiguration().getTargetProject()), this.context.getProperty("javaFileEncoding"), this.context.getJavaFormatter());
                answer.add(gjf);
            }
        }

        var2 = this.clientGenerators.iterator();

        while (var2.hasNext()) {
            javaGenerator = (AbstractJavaGenerator) var2.next();
            compilationUnits = javaGenerator.getCompilationUnits();
            var5 = compilationUnits.iterator();

            while (var5.hasNext()) {
                compilationUnit = (CompilationUnit) var5.next();
                gjf = new GeneratedJavaFile(compilationUnit, this.getTargetProject(this.context.getJavaClientGeneratorConfiguration().getTargetProject()), this.context.getProperty("javaFileEncoding"), this.context.getJavaFormatter());
                answer.add(gjf);
            }
        }

        return answer;
    }

    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        List<GeneratedXmlFile> answer = new ArrayList();
        if (this.xmlMapperGenerator != null) {
            boolean isMergeable = false;
            String mergeable = this.context.getProperty("mergeable");
            if ("true".equals(mergeable)) {
                isMergeable = true;
            }

            Document document = this.xmlMapperGenerator.getDocument();
            GeneratedXmlFile gxf = new GeneratedXmlFile(document, this.getMyBatis3XmlMapperFileName(), this.getMyBatis3XmlMapperPackage(), this.getTargetProject(this.context.getSqlMapGeneratorConfiguration().getTargetProject()), isMergeable, this.context.getXmlFormatter());
            if (this.context.getPlugins().sqlMapGenerated(gxf, this)) {
                answer.add(gxf);
            }
        }

        return answer;
    }

    public int getGenerationSteps() {
        return this.javaModelGenerators.size() + this.clientGenerators.size() + (this.xmlMapperGenerator == null ? 0 : 1);
    }

    public boolean isJava5Targeted() {
        return true;
    }

    public boolean requiresXMLGenerator() {
        AbstractJavaClientGenerator javaClientGenerator = this.createJavaClientGenerator();
        return javaClientGenerator == null ? false : javaClientGenerator.requiresXMLGenerator();
    }

    private String getTargetProject(String targetProject) {
        String usrDir = System.getProperty("user.dir");
        String result = targetProject;
        if (null != usrDir && usrDir.trim().length() > 0) {
            result = usrDir + File.separator + targetProject;
        }
        return result;
    }
}