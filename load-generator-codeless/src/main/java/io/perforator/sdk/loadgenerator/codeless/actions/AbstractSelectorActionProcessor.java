/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package io.perforator.sdk.loadgenerator.codeless.actions;

import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.codeless.config.SelectorType;
import org.openqa.selenium.By;

public abstract class AbstractSelectorActionProcessor<T extends SelectorActionConfig, V extends SelectorActionInstance<T>> extends AbstractActionProcessor<T, V>{
    public AbstractSelectorActionProcessor(String actionName) {
        super(actionName);
    }

    protected String buildRequiredStringSelectorForActionInstance(
            T actionConfig,
            String selectorFieldName,
            SelectorType selectorType,
            FormattingMap formatter
    ) {

        String targetSelector = actionConfig.getSelector();

        if(isBlank(targetSelector)){
            switch (selectorType){
                case css:
                    targetSelector = actionConfig.getCssSelector();
                    break;
                case xpath:
                    targetSelector = actionConfig.getXpathSelector();
                    break;
            }
        }

        return buildStringForActionInstance(
                selectorFieldName,
                targetSelector,
                formatter,
                true
        );
    }

    @Override
    public void validateActionConfig(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, T actionConfig) {
        super.validateActionConfig(loadGeneratorConfig, suiteConfig, actionConfig);
        if (isNotBlank(actionConfig.getCssSelector()) && isNotBlank(actionConfig.getXpathSelector())) {
            throw new RuntimeException(
                    "Only '" + getActionName()
                            + ".cssSelector' or '" +
                            getActionName()
                            + ".xpathSelector' must be required"
            );
        }

        if (isBlank(actionConfig.getCssSelector()) && isBlank(actionConfig.getXpathSelector()) && isBlank(actionConfig.getSelector())) {
            throw new RuntimeException(
                    getActionName()
                            + ".cssSelector or" +
                            getActionName()
                            + ".xpathSelector or" +
                            getActionName()
                            + " text value is required"
            );
        }
    }

    protected By getActionInstanceLocator(V actionInstance){
        SelectorType selectorType = actionInstance.getSelectorType();
        String selector = actionInstance.getSelector();
        switch (actionInstance.getSelectorType()){
            case css:
                return By.cssSelector(selector);
            case xpath:
                return By.xpath(selector);
            default:
                throw new RuntimeException("Selector type: " + selectorType + " not supported");
        }
    }

    private boolean isBlank(String str){
        return str == null || str.isBlank();
    }

    private boolean isNotBlank(String str){
        return str != null && !str.isBlank();
    }
}