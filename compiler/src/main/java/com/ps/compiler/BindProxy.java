package com.ps.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Created by PS on 2017/4/1.
 */

public class BindProxy {

	public Map<Integer, VariableElement> injectInfo = new HashMap<>();

	private TypeElement element;

	private String packageName, className;

	private static final String PROXY = "$$ViewBinder";

	public BindProxy(Elements elements, TypeElement typeElement) {
		element = typeElement;
		PackageElement packageElement = elements.getPackageOf(typeElement);
		packageName = packageElement.getQualifiedName().toString();
		className = typeElement.getSimpleName() + PROXY;
	}

	public JavaFile generateCode() {

		//生成方法代码
		MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addParameter(TypeName.get(element.asType()), "host", Modifier.FINAL)
				.addParameter(TypeName.OBJECT, "obj");

		//在方法中插入一行findViewById代码,遍历所有的元素
		for (int id : injectInfo.keySet()) {
			VariableElement element = injectInfo.get(id);
			String name = element.getSimpleName().toString();
			TypeMirror type = element.asType();
			injectMethodBuilder.addStatement("host.$N = ($T)((($T) obj).findViewById($L))"
					, name, type, TypeUtil.ANDROID_ACTIVITY, id);
		}

		//生成class代码
		TypeSpec clazz = TypeSpec.classBuilder(className)
				//这里添加的接口类,并添加了泛型
				.addSuperinterface(ParameterizedTypeName.get(TypeUtil.VIEWBIND, TypeName.get(element.asType())))
				.addModifiers(Modifier.PUBLIC)
				.addMethod(injectMethodBuilder.build())
				.build();

		return JavaFile.builder(packageName, clazz).build();
	}

}
