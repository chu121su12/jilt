package org.jilt.internal;

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

        // generate a separate interface
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
    protected TypeName builderFactoryMethodReturnType() {
        return this.builderClassTypeName();
    }

    @Override
    protected MethodSpec generateSetterMethod(VariableElement attribute,
            boolean mangleTypeParameters, boolean abstractMethod) {
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
    protected void addSuperInterfaces(TypeSpec.Builder builderClassBuilder) {
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
