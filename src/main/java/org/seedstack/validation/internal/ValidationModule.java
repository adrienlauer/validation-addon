/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.validation.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Provides;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.validation.ValidationService;
import org.seedstack.validation.spi.ValidationConcern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;

@ValidationConcern
class ValidationModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationModule.class);

    private final ValidatorFactory factory;
    private final ValidationService validationService;

    private Validator validator;
    private ExecutableValidator executableValidator;

    ValidationModule(ValidatorFactory factory, ValidationService validationService) {
        this.factory = factory;
        this.validationService = validationService;
    }

    @Override
    protected void configure() {
        this.validator = factory.getValidator();
        enableValidationOnInjectionPoints();
        configureDynamicValidation();
    }

    private void enableValidationOnInjectionPoints() {
        bindListener(staticMatcher(validationService), new ProvisionListener() {

            @Override
            public <A> void onProvision(ProvisionInvocation<A> provision) {
                A injectee = provision.provision();
                validationService.staticallyHandle(injectee);
            }
        });
    }

    private void configureDynamicValidation() {
        try {
            this.executableValidator = validator.forExecutables();
        } catch (Throwable t) {
            LOGGER.info("Unable to create the dynamic validator, support for dynamic validation disabled");
            LOGGER.debug(CorePlugin.DETAILS_MESSAGE, t);
        }

        if (this.executableValidator != null) {
            bindInterceptor(Matchers.any(), dynamicMatcher(validationService), new ValidationMethodInterceptor(validationService));
            requestInjection(validationService);
            bind(ValidationService.class).toInstance(validationService);
        }
    }

    @Provides
    Validator provideValidator() {
        return validator;
    }

    @Provides
    ExecutableValidator provideExecutableValidator() {
        return executableValidator;
    }

    private Matcher<? super Binding<?>> staticMatcher(final ValidationService validationService) {
        return new AbstractMatcher<Binding<?>>() {
            @Override
            public boolean matches(Binding<?> t) {
                return validationService.candidateForStaticValidation(t.getKey().getTypeLiteral().getRawType());
            }
        };
    }

    private Matcher<Method> dynamicMatcher(final ValidationService validationService) {
        return new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method t) {
                return validationService.candidateForDynamicValidation(t);
            }
        };
    }
}
