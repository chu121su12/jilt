package org.jilt.internal;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.jilt.Builder;
import org.jilt.BuilderInterfaces;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.util.List;

final class FunctionalBuilderGenerator extends AbstractTypeSafeBuilderGenerator {
    public FunctionalBuilderGenerator(TypeElement targetClass, List<? extends VariableElement> attributes,
            Builder builderAnnotation, BuilderInterfaces builderInterfaces,
            ExecutableElement targetCreationMethod, Elements elements, Filer filer) {
        super(targetClass, attributes, builderAnnotation, builderInterfaces, targetCreationMethod,
                elements, filer);
    }

    @Override
    protected void generateClassesNeededByBuilder() throws Exception {
        TypeSpec.Builder outerInterfacesBuilder = TypeSpec.interfaceBuilder(this.outerInterfacesName())
                .addAnnotation(this.generatedAnnotation())
                .addModifiers(Modifier.PUBLIC);

        // generate a separate interface for each required property
        boolean hasOptionalAttribute = false;
        for (VariableElement currentAttribute : this.attributes()) {
            if (this.isOptional(currentAttribute)) {
                hasOptionalAttribute = true;
            } else {
                outerInterfacesBuilder.addType(this.functionalSetterInterface(
                        this.interfaceNameForAttribute(currentAttribute)));
            }
        }
        // if there's an optional attribute,
        // generate an interface for optional setters
        if (hasOptionalAttribute) {
            outerInterfacesBuilder.addType(this.functionalSetterInterface(
                    this.lastInterfaceName()));
        }

        JavaFile javaFile = JavaFile
                .builder(this.outerInterfacesPackage(), outerInterfacesBuilder.build())
                .build();
        javaFile.writeTo(this.filer());
    }

    @Override
    protected MethodSpec makeStaticFactoryMethod() {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder(this.builderFactoryMethodName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariables(this.builderClassTypeParameters())
                .returns(this.targetClassTypeName());

        // parameters
        boolean hasOptionalAttribute = false;
        for (VariableElement currentAttribute : this.attributes()) {
            if (this.isOptional(currentAttribute)) {
                hasOptionalAttribute = true;
            } else {
                method.addParameter(ParameterSpec
                        .builder(this.returnTypeForSetterFor(currentAttribute, false),
                                this.attributeSimpleName(currentAttribute))
                        .build());
            }
        }
        TypeName optionalsSetterType = this.innerInterfaceNamed(this.lastInterfaceName(), false);
        if (hasOptionalAttribute) {
            method.addParameter(ParameterSpec
                            .builder(ArrayTypeName.of(optionalsSetterType), "optionals")
                            .build())
                    .varargs();
        }

        // calling the setters
        method.addStatement("$1T builder = new $1T()", this.builderClassTypeName());
        for (VariableElement currentAttribute : this.attributes()) {
            if (this.isOptional(currentAttribute)) {
                continue;
            }
            method.addStatement("$N.accept(builder)", this.attributeSimpleName(currentAttribute));
        }
        if (hasOptionalAttribute) {
            method
                    .beginControlFlow("for ($T optional : optionals)", optionalsSetterType)
                    .addStatement("optional.accept(builder)")
                    .endControlFlow();
        }

        return method
                .addStatement("return builder.$N()", this.buildMethodName())
                .build();
    }

    @Override
    protected TypeName builderFactoryMethodReturnType() {
        throw new UnsupportedOperationException("FunctionalBuilderGenerator.builderFactoryMethodReturnType() should never be invoked");
    }

    @Override
    protected void addSuperInterfaces(TypeSpec.Builder builderClassBuilder) {
        // check if we have an optional property
        boolean hasOptionalAttribute = false;
        for (VariableElement currentAttribute : this.attributes()) {
            if (this.isOptional(currentAttribute)) {
                hasOptionalAttribute = true;
                break;
            }
        }
        if (!hasOptionalAttribute) {
            return;
        }

        // generate a static nested class that will contain the setters for the optional properties
        TypeSpec.Builder optionalSettersClass = TypeSpec
                .classBuilder(this.lastInterfaceName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (VariableElement currentAttribute : this.attributes()) {
            MethodSpec setterMethod = this.generateSetterMethod(currentAttribute, false, true);
            if (setterMethod != null) {
                optionalSettersClass.addMethod(setterMethod);
            }
        }
        builderClassBuilder.addType(optionalSettersClass.build());
    }

    @Override
    protected MethodSpec generateSetterMethod(VariableElement attribute,
            boolean mangleTypeParameters, boolean handleOnlyOptionalAttributes) {
        boolean attributeIsOptional = this.isOptional(attribute);
        if (attributeIsOptional != handleOnlyOptionalAttributes) {
            return null;
        }

        String fieldName = this.attributeSimpleName(attribute);
        TypeName parameterType = this.attributeType(attribute, mangleTypeParameters);
        TypeName setterInterface = this.returnTypeForSetterFor(attribute, mangleTypeParameters);
        return MethodSpec
                .methodBuilder(this.setterMethodName(attribute))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(setterInterface)
                .addParameter(this.setterParameterSpec(attribute, parameterType))
                .addStatement("return $L", TypeSpec
                        .anonymousClassBuilder("")
                        .addSuperinterface(setterInterface)
                        .addMethod(MethodSpec
                                .methodBuilder("accept")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(this.setterBuilderParameter())
                                .addStatement("builder.$1L = $1L", fieldName)
                                .build())
                        .build())
                .build();
    }

    @Override
    protected TypeName returnTypeForSetterFor(VariableElement attribute, boolean withMangledTypeParameters) {
        String returnTypeName;
        if (this.isOptional(attribute)) {
            returnTypeName = this.lastInterfaceName();
        } else {
            returnTypeName = this.interfaceNameForAttribute(attribute);
        }
        return this.innerInterfaceNamed(returnTypeName, withMangledTypeParameters);
    }

    @Override
    protected String defaultLastInterfaceName() {
        return "Optional";
    }

    private TypeSpec functionalSetterInterface(String interfaceName) {
        return TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariables(this.builderClassTypeParameters())
                .addMethod(MethodSpec.methodBuilder("accept")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(this.setterBuilderParameter())
                        .build())
                .build();
    }

    private ParameterSpec setterBuilderParameter() {
        return ParameterSpec
                .builder(this.builderClassTypeName(), "builder")
                .build();
    }
}
