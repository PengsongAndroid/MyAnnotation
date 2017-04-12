package com.ps.compiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.ps.annotation.BindView;

/**
 * 使用 Google 的 auto-service 库可以自动生成 META-INF/services/javax.annotation.processing.Processor 文件
 */
@AutoService(Processor.class)
public class BindProcessor extends AbstractProcessor{

    //元素处理辅助类
    private Elements elementUtils;

    //日志辅助类
    private Messager messager;

    private Map<String, BindProxy> mProxyMap = new HashMap<String, BindProxy>();;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    /**
     * @return 指定哪些注解应该被注解处理器注册
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportType = new HashSet<String>();
        supportType.add(BindView.class.getCanonicalName());
        return supportType;
    }

    /**
     * @return 指定使用的 Java 版本。通常返回 SourceVersion.latestSupported()。
     */
    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "process...");
        //获取BindView注释的元素集合
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        if (elements == null || elements.size() < 1){
            return true;
        }
        //遍历集合
        for (Element element : elements){
            //检查是否是作用于FIELD
            if (checkElement(element)){
                VariableElement variable = (VariableElement) element;
                TypeElement typeElement = (TypeElement) element.getEnclosingElement();
                String className = typeElement.getQualifiedName().toString();
                //从缓存中取得BindProxy类,不存在则new
                BindProxy proxy = mProxyMap.get(className);
                if (proxy == null){
                    proxy = new BindProxy(elementUtils, typeElement);
                    mProxyMap.put(className, proxy);
                }
                BindView bindView = variable.getAnnotation(BindView.class);
                proxy.injectInfo.put(bindView.value(), variable);
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "error...");
            }
        }
        //遍历mProxyMap 取出所有的BindProxy类 去生成代码
        for (String key : mProxyMap.keySet()){
            BindProxy proxy = mProxyMap.get(key);
            try {
                proxy.generateCode().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean checkElement(Element element){
        if (element.getKind() != ElementKind.FIELD)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, "%s must be declared on field.", element);
            return false;
        }
        return true;
    }

}
