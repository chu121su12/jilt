package org.jilt.internal;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import org.jilt.Builder;
import org.jilt.BuilderInterfaces;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.util.List;

final class TypeSafeBuilderGenerator extends AbstractTypeSafeBuilderGenerator {
    TypeSafeBuilderGenerator(TypeElement targetClass, List<? extends VariableElement> attributes,
            Builder builderAnnotation, BuilderInterfaces builderInterfaces, TypeElement targetFactoryClass,
            Name targetFactoryMethod, Elements elements, Filer filer) {
        super(targetClass, attributes, builderAnnotation, builderInterfaces, targetFactoryClass, targetFactoryMethod,
                elements, filer);
    }

    @Override
    protected void generateClassesNeededByBuilder() throws Exception {
        TypeSpec.Builder outerInterfacesBuilder = TypeSpec.interfaceBuilder(outerInterfacesName())
                .addAnnotation(generatedAnnotation())
                .addModifiers(Modifier.PUBLIC);

        TypeSpec.Builder optionalsInterfaceBuilder = TypeSpec.interfaceBuilder(lastInterfaceName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariables(this.builderClassTypeParameters());

        for (VariableElement currentAttribute : attributes()) {
            MethodSpec setterMethod = MethodSpec
                    .methodBuilder(builderSetterMethodName(currentAttribute))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(this.returnTypeForSetterFor(currentAttribute, true))
                    .addParameter(this.attributeType(currentAttribute), this.attributeSimpleName(currentAttribute))
                    .build();

            if (isOptional(currentAttribute)) {
                optionalsInterfaceBuilder.addMethod(setterMethod);
            } else {
                TypeSpec.Builder innerInterfaceBuilder = TypeSpec
                        .interfaceBuilder(interfaceNameForAttribute(currentAttribute))
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addTypeVariables(this.mangledBuilderClassTypeParameters());

                innerInterfaceBuilder.addMethod(setterMethod);

                outerInterfacesBuilder.addType(innerInterfaceBuilder.build());
            }
        }

        addBuildMethodToInterface(optionalsInterfaceBuilder);
        outerInterfacesBuilder.addType(optionalsInterfaceBuilder.build());

        JavaFile javaFile = JavaFile
                .builder(outerInterfacesPackage(), outerInterfacesBuilder.build())
                .build();
        javaFile.writeTo(filer());
    }

    @Override
    protected TypeName builderFactoryMethodReturnType() {
        VariableElement firstRequiredAttribute = firstRequiredAttribute();
        String returnTypeName = firstRequiredAttribute == null
                ? lastInterfaceName()
                : interfaceNameForAttribute(firstRequiredAttribute);
        return innerInterfaceNamed(returnTypeName);
    }

    @Override
    protected TypeName returnTypeForSetterFor(VariableElement attribute, boolean withMangledTypeParameters) {
        String returnTypeName;
        if (isOptional(attribute)) {
            returnTypeName = lastInterfaceName();
        } else {
            VariableElement nextRequiredAttribute = nextRequiredAttribute(attribute);
            returnTypeName = nextRequiredAttribute == null
                    ? lastInterfaceName()
                    : interfaceNameForAttribute(nextRequiredAttribute);
        }
        return this.innerInterfaceNamed(returnTypeName, withMangledTypeParameters);
    }

    @Override
    protected void addSuperInterfaces(TypeSpec.Builder builderClassBuilder) {
        for (VariableElement attribute : attributes()) {
            if (!isOptional(attribute))
                builderClassBuilder.addSuperinterface(innerInterfaceNamed(interfaceNameForAttribute(attribute)));
        }
        builderClassBuilder.addSuperinterface(innerInterfaceNamed(lastInterfaceName()));
    }

    private TypeName attributeType(VariableElement currentAttribute) {
        TypeName ret = TypeName.get(currentAttribute.asType());
        if (ret instanceof TypeVariableName) {
            // if this is a type variable, we need to mangle it
            TypeVariableName typeVariableName = (TypeVariableName) ret;
            return this.mangleTypeParameter(typeVariableName);
        }
        return ret;
    }

    private VariableElement firstRequiredAttribute() {
        VariableElement ret = null;
        if (!attributes().isEmpty()) {
            VariableElement firstAttribute = attributes().get(0);
            ret = isOptional(firstAttribute) ? nextRequiredAttribute(firstAttribute) : firstAttribute;
        }
        return ret;
    }

    private VariableElement nextRequiredAttribute(VariableElement attribute) {
        VariableElement ret = attribute;

        do {
            ret = nextAttribute(ret);
        } while (ret != null && isOptional(ret));

        return ret;
    }

    @Override
    protected String defaultLastInterfaceName() {
        return "Optionals";
    }
}
