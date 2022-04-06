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

public abstract class AbstractSelectorActionProcessor<T extends SelectorActionConfig, V extends SelectorActionInstance<T>> extends AbstractActionProcessor<T, V> {
    
    public AbstractSelectorActionProcessor(String actionName) {
        super(actionName);
    }

    protected SelectorType getSelectorType(
            T actionConfig,
            SelectorType defaultSelectorType
    ) {
        if (isNotBlank(actionConfig.getSelector())) {
            return defaultSelectorType;
        }

        if (isNotBlank(actionConfig.getCssSelector())) {
            return SelectorType.css;
        }

        if (isNotBlank(actionConfig.getXpathSelector())) {
            return SelectorType.xpath;
        }

        throw new RuntimeException(
                "Can't determine selector type from " + actionConfig.getActionName()
        );
    }

    protected String buildRequiredStringSelectorForActionInstance(
            T actionConfig,
            String selectorFieldName,
            FormattingMap formatter
    ) {
        return buildStringForActionInstance(
                selectorFieldName,
                getTargetSelector(actionConfig),
                formatter,
                true
        );
    }

    @Override
    public void validateActionConfig(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, T actionConfig) {
        super.validateActionConfig(loadGeneratorConfig, suiteConfig, actionConfig);
        
        if (isNotBlank(actionConfig.getCssSelector()) && isNotBlank(actionConfig.getXpathSelector())) {
            throw new RuntimeException(
                    "Action '" + actionConfig.getActionName() + "'"
                    + " should have either 'cssSelector' or 'xpathSelector' - "
                    + "it can't have both at the same time."
            );
        }

        if (isBlank(actionConfig.getCssSelector()) && isBlank(actionConfig.getXpathSelector()) && isBlank(actionConfig.getSelector())) {
            throw new RuntimeException(
                    "Action '" + actionConfig.getActionName() + "'"
                    + " should have selector specified either as inplace value, "
                    + "or as a child node 'cssSelector', "
                    + "or as a child node 'xpathSelector'."
            );
        }
    }

    protected By getActionInstanceLocator(V actionInstance) {
        SelectorType selectorType = actionInstance.getSelectorType();
        String selector = actionInstance.getSelector();
        
        switch (actionInstance.getSelectorType()) {
            case css:
                return By.cssSelector(selector);
            case xpath:
                return By.xpath(selector);
            default:
                throw new RuntimeException("Selector type: " + selectorType + " not supported");
        }
    }

    private String getTargetSelector(T actionConfig) {
        if (isNotBlank(actionConfig.getSelector())) {
            return actionConfig.getSelector();
        }
        if (isNotBlank(actionConfig.getCssSelector())) {
            return actionConfig.getCssSelector();
        }
        if (isNotBlank(actionConfig.getXpathSelector())) {
            return actionConfig.getXpathSelector();
        }

        return null;
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }
}